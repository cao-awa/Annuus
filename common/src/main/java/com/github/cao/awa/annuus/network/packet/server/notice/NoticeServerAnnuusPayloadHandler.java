package com.github.cao.awa.annuus.network.packet.server.notice;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.server.AnnuusServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class NoticeServerAnnuusPayloadHandler {
    public static void updateAnnuusVersion(NoticeServerAnnuusPayload payload, ServerPlayNetworkHandler networkHandler) {
        AnnuusServer.setAnnuusVersion(networkHandler, payload.versionId());

        logPlayerAnnuusVersion(payload, networkHandler.player);
    }

    public static void updateAnnuusVersionDuringPlay(NoticeServerAnnuusPayload payload, ServerPlayerEntity player) {
        AnnuusServer.setAnnuusVersion(player.networkHandler, payload.versionId());

        logPlayerAnnuusVersion(payload, player);
    }

    private static void logPlayerAnnuusVersion(NoticeServerAnnuusPayload payload, ServerPlayerEntity player) {
        if (payload.versionId() > -1) {
            Annuus.LOGGER.info("Player {} updated Annuus protocol version {} (server version is {})", player.getName().getString(), payload.versionId(), Annuus.PROTOCOL_VERSION_ID);
        }
    }
}
