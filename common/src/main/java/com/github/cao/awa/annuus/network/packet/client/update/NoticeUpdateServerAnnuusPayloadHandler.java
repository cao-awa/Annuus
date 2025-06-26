package com.github.cao.awa.annuus.network.packet.client.update;

import com.github.cao.awa.annuus.version.AnnuusVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class NoticeUpdateServerAnnuusPayloadHandler {
    public static void tryUpdateAnnuusVersion(NoticeUpdateServerAnnuusPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        assert client != null;
        if (payload.needUpdate()) {
            System.out.println("Server required update client annuus version");
            AnnuusVersion.tryUpdateAnnuusVersion(player.networkHandler);
        }
    }
}
