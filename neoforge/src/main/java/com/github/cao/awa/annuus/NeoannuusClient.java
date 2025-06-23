package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayloadHandler;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "annuus_client", dist = {Dist.CLIENT})
public class NeoannuusClient {
    public NeoannuusClient(IEventBus eventBus) {
        Neoannuus.LOGGER.info("Loading annuus neoforge client");

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        event.getConnection().send(NoticeServerAnnuusPayload.createPacket());
    }
}
