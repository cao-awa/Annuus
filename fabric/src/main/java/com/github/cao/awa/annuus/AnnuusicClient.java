package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedChunkBlockUpdatePayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayloadHandler;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AnnuusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Annuus.isServer = false;

        ClientConfigurationConnectionEvents.START.register((handler, client) -> {
            ClientConfigurationNetworking.send(NoticeServerAnnuusPayload.createData());
        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedBlockUpdatePayload.IDENTIFIER, (packet, context) -> {
            CollectedBlockUpdatePayloadHandler.updateBlocksFromPayload(packet, context.client(), context.player());
        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedChunkBlockUpdatePayload.IDENTIFIER, (packet, context) -> {
            CollectedChunkBlockUpdatePayloadHandler.updateBlocksFromPayload(packet, context.client(), context.player());
        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedChunkDataPayload.IDENTIFIER, (packet, context) -> {
            CollectedChunkDataPayloadHandler.loadChunksFromPayload(packet, context.client(), context.player());
        });
    }
}
