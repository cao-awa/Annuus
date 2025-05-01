package com.github.cao.awa.annuus;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = "annuus_server", dist = {Dist.DEDICATED_SERVER})
public class NeoannuusServer {
    public NeoannuusServer(IEventBus eventBus) {
        Annuus.isServer = true;

        Neoannuus.LOGGER.info("Loading annuus neoforge server");
    }
}
