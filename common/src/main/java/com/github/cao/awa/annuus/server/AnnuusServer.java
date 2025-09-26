package com.github.cao.awa.annuus.server;

import com.github.cao.awa.annuus.mixin.server.network.handler.ServerCommonNetworkHandlerAccessor;
import com.github.cao.awa.annuus.version.storage.AnnuusVersionStorage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class AnnuusServer {
    private static PlayerManager playerManager;
    private static final AnnuusVersionStorage annuusVersions = new AnnuusVersionStorage();

    public static void setupServerPlayerManager(@NotNull PlayerManager playerManager) {
        AnnuusServer.playerManager = playerManager;
    }

    public static int getAnnuusVersion(@NotNull Object target) {
        return AnnuusServer.annuusVersions.getAnnuusVersion(convertToStandardConnection(target));
    }

    public static int setAnnuusVersion(@NotNull Object target, int version) {
        return AnnuusServer.annuusVersions.setAnnuusVersion(convertToStandardConnection(target), version);
    }

    private static ClientConnection convertToStandardConnection(Object target) {
        return switch (target) {
            case ServerCommonNetworkHandler handler -> ((ServerCommonNetworkHandlerAccessor) handler).getConnection();
            case ServerPlayerEntity player -> ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getConnection();
            case ClientConnection connection -> connection;
            default -> throw new UnsupportedOperationException("Cannot convert object '" + target + "' to the standard key");
        };
    }
}
