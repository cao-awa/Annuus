package com.github.cao.awa.annuus.neoforged;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.version.AnnuusVersion;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "annuus_client", dist = Dist.CLIENT)
public class NeoannuusClient {
    public NeoannuusClient(IEventBus eventBus) {
        Annuus.isServer = false;

        Neoannuus.LOGGER.info("Loading annuus neoforge client");

        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> AnnuusVersion.tryUpdateAnnuusVersion(event.getConnection()));
    }
}
