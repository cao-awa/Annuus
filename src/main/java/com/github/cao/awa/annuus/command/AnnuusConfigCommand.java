package com.github.cao.awa.annuus.command;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.config.AnnuusConfig;
import com.github.cao.awa.annuus.config.key.AnnuusConfigKey;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AnnuusConfigCommand {
    public static void register(MinecraftServer server) {
        register(server.getCommandManager().getDispatcher());
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("annuus")
                        .executes(context -> {
                            int enabledConfigs = Annuus.CONFIG.collectEnabled().size();

                            context.getSource().sendFeedback(
                                    () -> Text.of("Annuus '" + Annuus.VERSION + "' successfully loaded, has " + enabledConfigs + " configs enabled"),
                                    false
                            );
                            return 0;
                        })
                        .requires(context -> context.hasPermissionLevel(4))
                        .then(createStringConfigNode(AnnuusConfig.CHUNK_COMPRESS))
        );
    }

    private static <X> int changeConfigTemporary(CommandContext<ServerCommandSource> context, AnnuusConfigKey<X> key, BiFunction<CommandContext<ServerCommandSource>, String, X> argument) {
        return changeConfig(context, key, argument, true);
    }

    private static <X> int changeConfig(CommandContext<ServerCommandSource> context, AnnuusConfigKey<X> key, BiFunction<CommandContext<ServerCommandSource>, String, X> argument) {
        return changeConfig(context, key, argument, false);
    }

    private static <X> int changeConfig(CommandContext<ServerCommandSource> context, AnnuusConfigKey<X> key, BiFunction<CommandContext<ServerCommandSource>, String, X> argument, boolean temporary) {
        X value = argument.apply(context, key.name());
        Annuus.CONFIG.setConfig(key, value);
        if (temporary) {
            context.getSource().sendFeedback(
                    () -> Text.of("Config '" + key.name() + "' is '" + value + "' temporarily"),
                    true
            );
        } else {
            Annuus.PERSISTENT_CONFIG.setConfig(key, value);
            context.getSource().sendFeedback(
                    () -> Text.of("Config '" + key.name() + "' is '" + value + "' now"),
                    true
            );

            context.getSource().getServer().execute(Annuus::writeConfig);
        }
        return 0;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createBoolConfigNode(AnnuusConfigKey<Boolean> key) {
        return createConfigNode(key, BoolArgumentType::bool, BoolArgumentType::getBool);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createIntConfigNode(AnnuusConfigKey<Integer> key) {
        return createConfigNode(key, IntegerArgumentType::integer, IntegerArgumentType::getInteger);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createStringConfigNode(AnnuusConfigKey<String> key) {
        return createConfigNode(key, StringArgumentType::string, StringArgumentType::getString);
    }

    private static <X> LiteralArgumentBuilder<ServerCommandSource> createConfigNode(AnnuusConfigKey<X> key, Supplier<ArgumentType<X>> argumentType, BiFunction<CommandContext<ServerCommandSource>, String, X> argument) {
        String configName = key.name();

        return CommandManager.literal(configName)
                .executes(context -> {
                    context.getSource().sendFeedback(
                            () -> Text.of("Config '" + configName + "' is '" + Annuus.CONFIG.getConfig(key) + "' now"),
                            false
                    );
                    return 0;
                })
                .then(
                        CommandManager.argument(configName, argumentType.get())
                                .executes(context -> changeConfig(context, key, argument))
                                .then(CommandManager.literal("temporary")
                                        .executes(context -> changeConfigTemporary(context, key, argument))
                                )
                );
    }
}
