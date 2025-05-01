package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

public class AnnuusicServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Annuus.isServer = true;

        ServerConfigurationNetworking.registerGlobalReceiver(NoticeServerAnnuusPayload.IDENTIFIER, (packet, context) -> {
            NoticeServerAnnuusPayloadHandler.updateAnnuusVersion(packet, context.networkHandler());
        });
    }
}
