package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayloadHandler;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AnnuusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationConnectionEvents.START.register((handler, client) -> {
            handler.sendPacket(NoticeServerAnnuusPayload.createPacket());
        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedChunkDataPayload.IDENTIFIER, (packet, context) -> {
            CollectedChunkDataPayloadHandler.loadChunksFromPayload(packet, context.client(), context.player());
        });
    }
}
