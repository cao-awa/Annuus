package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.command.AnnuusDebugCommand;
import com.github.cao.awa.annuus.command.AnnuusConfigCommand;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "annuus_server", dist = {Dist.DEDICATED_SERVER})
public class NeoannuusServer {
    public NeoannuusServer(IEventBus eventBus) {
        Neoannuus.LOGGER.info("Loading annuus neoforge server");
    }
}
