package com.github.cao.awa.annuus.neoforged;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayloadHandler;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayloadHandler;
import com.github.cao.awa.annuus.version.AnnuusVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "annuus_client", dist = Dist.CLIENT)
public class AnnuusNeoForgedClient {
    public AnnuusNeoForgedClient(IEventBus eventBus) {
        Annuus.isServer = false;

        AnnuusNeoForged.LOGGER.info("Loading annuus neoforge client");

        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> AnnuusVersion.tryUpdateAnnuusVersion(event.getConnection()));

        PayloadRegistrar payloadRegistrar = new PayloadRegistrar("1");

        payloadRegistrar.playToClient(CollectedBlockUpdatePayload.IDENTIFIER, CollectedBlockUpdatePayload.CODEC, (payload, context) -> {
            CollectedBlockUpdatePayloadHandler.updateBlocksFromPayload(payload, MinecraftClient.getInstance(), (ClientPlayerEntity) context.player());
        });

        payloadRegistrar.playToClient(CollectedChunkBlockUpdatePayload.IDENTIFIER, CollectedChunkBlockUpdatePayload.CODEC, (payload, context) -> {
            CollectedChunkBlockUpdatePayloadHandler.updateBlocksFromPayload(payload, MinecraftClient.getInstance(), (ClientPlayerEntity) context.player());
        });

        payloadRegistrar.playToClient(CollectedChunkDataPayload.IDENTIFIER, CollectedChunkDataPayload.CODEC, (payload, context) -> {
            CollectedChunkDataPayloadHandler.loadChunksFromPayload(payload, MinecraftClient.getInstance(), (ClientPlayerEntity) context.player());
        });

        payloadRegistrar.playToClient(ShortRecipeSyncPayload.IDENTIFIER, ShortRecipeSyncPayload.CODEC, (payload, context) -> {
            ShortRecipeSyncPayloadHandler.syncRecipesFromPayload(payload, MinecraftClient.getInstance(), (ClientPlayerEntity) context.player());
        });

        payloadRegistrar.playToClient(NoticeUpdateServerAnnuusPayload.IDENTIFIER, NoticeUpdateServerAnnuusPayload.CODEC, (payload, context) -> {
            NoticeUpdateServerAnnuusPayloadHandler.tryUpdateAnnuusVersion(payload, MinecraftClient.getInstance(), (ClientPlayerEntity) context.player());
        });
    }
}
