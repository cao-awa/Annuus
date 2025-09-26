package com.github.cao.awa.annuus.network.packet.client.update;

import com.github.cao.awa.annuus.version.AnnuusVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoticeUpdateServerAnnuusPayloadHandler {
    private static final Logger LOGGER = LogManager.getLogger("NoticeUpdateServerAnnuusPayloadHandler");

    public static void tryUpdateAnnuusVersion(NoticeUpdateServerAnnuusPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        assert client != null;
        if (payload.needUpdate()) {
            LOGGER.info("Server required update client annuus version");
            AnnuusVersion.tryUpdateAnnuusVersion(player.networkHandler);
        }
    }
}
