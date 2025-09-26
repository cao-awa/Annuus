package com.github.cao.awa.annuus.neoforged;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "annuus_server", dist = {Dist.DEDICATED_SERVER})
public class AnnuusNeoForgedServer {
    public AnnuusNeoForgedServer(IEventBus eventBus) {
        Annuus.isServer = true;

        AnnuusNeoForged.LOGGER.info("Loading annuus neoforge server");
    }
}
