package com.github.cao.awa.annuus.command;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.debug.AnnuusDebugger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AnnuusDebugCommand {
    public static void register(MinecraftServer server) {
        register(server.getCommandManager().getDispatcher());
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                        CommandManager.literal("annuusug").requires(
                                serverCommandSource -> serverCommandSource.hasPermissionLevel(4)
                        ).executes(context -> {
                                    AnnuusDebugger.enableDebugs = true;

                                    context.getSource().sendFeedback(() -> Text.literal("Enabled annuus debug mode"), true);

                                    return resetCounters(context);
                                }
                        ).then(
                                CommandManager.literal("resetCounters").executes(AnnuusDebugCommand::resetCounters)
                        ).then(
                                CommandManager.literal("show").executes(context -> {
                                    Annuus.LOGGER.info("[Debug] ======= Block updates");
                                    Annuus.LOGGER.info("[Debug] = Processed blocks: " + AnnuusDebugger.processedBlockUpdates);
                                    Annuus.LOGGER.info("[Debug] = Vanilla bytes: " + AnnuusDebugger.vanillaBlockUpdateBytes);
                                    Annuus.LOGGER.info("[Debug] = Annuus bytes: " + AnnuusDebugger.processedBlockUpdateBytes);
                                    Annuus.LOGGER.info("[Debug] ======= Chunks ---");
                                    Annuus.LOGGER.info("[Debug] = Processed chunks: " + AnnuusDebugger.processedChunks);
                                    Annuus.LOGGER.info("[Debug] = Processed bytes: " + AnnuusDebugger.processedChunksBytes);

                                    String time = String.valueOf(AnnuusDebugger.chunkCalculatedTimes);
                                    time = time.substring(0, Math.min(time.indexOf(".") + 3, time.length() - 1));

                                    Annuus.LOGGER.info("[Debug] = Processing time: " + time + "ms");
                                    Annuus.LOGGER.info("[Debug] =======");
                                    return 0;
                                })
                        )
                );
    }

    private static int resetCounters(CommandContext<ServerCommandSource> context) {
        AnnuusDebugger.processedChunksBytes = 0;
        AnnuusDebugger.processedChunks = 0;
        AnnuusDebugger.chunkCalculatedTimes = 0;
        AnnuusDebugger.processedBlockUpdates = 0;
        AnnuusDebugger.vanillaBlockUpdateBytes = 0;
        AnnuusDebugger.processedBlockUpdateBytes = 0;

        context.getSource().sendFeedback(() -> Text.literal("Resetting all counters"), true);

        return 0;
    }
}
