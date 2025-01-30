package com.github.cao.awa.annuus.command;

import com.github.cao.awa.annuus.Annuus;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class AnnuusDebugCommand {
    public static void register(MinecraftServer server) {
        register(server.getCommandManager().getDispatcher());
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                        CommandManager.literal("annuusug").requires(
                                serverCommandSource -> serverCommandSource.hasPermissionLevel(4)
                        ).executes(context -> {
                                    Annuus.enableDebugs = true;
                                    return resetCounters(context);
                                }
                        ).then(
                                CommandManager.literal("resetCounters").executes(AnnuusDebugCommand::resetCounters)
                        ).then(
                                CommandManager.literal("show").executes(context -> {
                                    System.out.println("--- Chunk ---");
                                    System.out.println("Processed chunks: " + Annuus.processedChunks);
                                    System.out.println("Processed bytes: " + Annuus.processedBytes);

                                    String time = String.valueOf(Annuus.calculatedTimes);
                                    time = time.substring(0, Math.min(time.indexOf(".") + 3, time.length() - 1));

                                    System.out.println("Processing time: " + time + "ms");
                                    return 0;
                                })
                        )
                );
    }

    private static int resetCounters(CommandContext<ServerCommandSource> context) {
        Annuus.processedBytes = 0;
        Annuus.processedChunks = 0;
        Annuus.calculatedTimes = 0;

        return 0;
    }
}
