package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.command.AnnuusDebugCommand;
import com.github.cao.awa.annuus.command.AnnuusConfigCommand;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

public class AnnuusicServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerConfigurationNetworking.registerGlobalReceiver(NoticeServerAnnuusPayload.IDENTIFIER, (packet, context) -> {
            NoticeServerAnnuusPayloadHandler.updateAnnuusVersion(packet, context.networkHandler());
        });
    }
}
