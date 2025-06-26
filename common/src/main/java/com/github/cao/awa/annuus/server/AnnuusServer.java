package com.github.cao.awa.annuus.server;

import com.github.cao.awa.annuus.mixin.network.handler.ServerCommonNetworkHandlerAccessor;
import com.github.cao.awa.annuus.version.storage.AnnuusVersionStorageMap;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class AnnuusServer {
    private static PlayerManager playerManager;
    private static final AnnuusVersionStorageMap annuusVersions = new AnnuusVersionStorageMap();

    public static void setupServerPlayerManager(@NotNull PlayerManager playerManager) {
        AnnuusServer.playerManager = playerManager;
    }

    public static int getAnnuusVersion(@NotNull Object target) {
        return AnnuusServer.annuusVersions.getAnnuusVersion(convertToStandardObject(target));
    }

    public static int setAnnuusVersion(@NotNull Object target, int version) {
        return AnnuusServer.annuusVersions.setAnnuusVersion(convertToStandardObject(target), version);
    }

    private static Object convertToStandardObject(Object target) {
        return switch (target) {
            case ServerCommonNetworkHandler handler -> ((ServerCommonNetworkHandlerAccessor) handler).getConnection();
            case ServerPlayerEntity player -> ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getConnection();
            case ClientConnection connection -> connection;
            default -> throw new UnsupportedOperationException("Cannot convert object '" + target + "' to the standard key");
        };
    }
}
