package com.github.cao.awa.annuus.fabric;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayloadHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AnnuusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Annuus.isServer = false;

//        ClientConfigurationConnectionEvents.START.register((handler, client) -> {
//            if (client.getNetworkHandler() != null) {
//                AnnuusVersion.tryUpdateAnnuusVersion(client.getNetworkHandler().getConnection());
//            }
//        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedBlockUpdatePayload.IDENTIFIER, (packet, context) -> {
            CollectedBlockUpdatePayloadHandler.updateBlocksFromPayload(packet, context.client(), context.player());
        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedChunkBlockUpdatePayload.IDENTIFIER, (packet, context) -> {
            CollectedChunkBlockUpdatePayloadHandler.updateBlocksFromPayload(packet, context.client(), context.player());
        });

        ClientPlayNetworking.registerGlobalReceiver(CollectedChunkDataPayload.IDENTIFIER, (packet, context) -> {
            CollectedChunkDataPayloadHandler.loadChunksFromPayload(packet, context.client(), context.player());
        });

        ClientPlayNetworking.registerGlobalReceiver(ShortRecipeSyncPayload.IDENTIFIER, (packet, context) -> {
            ShortRecipeSyncPayloadHandler.syncRecipesFromPayload(packet, context.client(), context.player());
        });

        ClientPlayNetworking.registerGlobalReceiver(NoticeUpdateServerAnnuusPayload.IDENTIFIER, (packet, context) -> {
            NoticeUpdateServerAnnuusPayloadHandler.tryUpdateAnnuusVersion(packet, context.client(), context.player());
        });
    }
}
