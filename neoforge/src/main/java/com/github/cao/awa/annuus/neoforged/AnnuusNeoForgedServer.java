package com.github.cao.awa.annuus.neoforged;

import com.github.cao.awa.annuus.Annuus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = "annuus_server", dist = {Dist.DEDICATED_SERVER})
public class AnnuusNeoForgedServer {
    public AnnuusNeoForgedServer(IEventBus eventBus) {
        Annuus.isServer = true;

        AnnuusNeoForged.LOGGER.info("Loading annuus neoforge server");
    }
}
