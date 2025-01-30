package com.github.cao.awa.annuus.network.packet.server.notice;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;

public class NoticeServerAnnuusPayloadHandler {
    public static void updateAnnuusVersion(NoticeServerAnnuusPayload payload, ServerConfigurationNetworkHandler networkHandler) {
        ((AnnuusVersionStorage) networkHandler).setAnnuusVersion(payload.versionId());
    }

    public static void updateAnnuusVersionDuringPlay(NoticeServerAnnuusPayload payload, PlayerEntity player) {
        ((AnnuusVersionStorage) player).setAnnuusVersion(payload.versionId());

        if (payload.versionId() > -1) {
            Annuus.LOGGER.info("Player {} updated Annuus protocol version {}", player.getName().getString(), payload.versionId());
        }
    }
}
