package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;

public class AnnuusServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerConfigurationNetworking.registerGlobalReceiver(NoticeServerAnnuusPayload.IDENTIFIER, (packet, context) -> {
            NoticeServerAnnuusPayloadHandler.noticeAnnuusFromPayload(packet, context.server(), context.networkHandler());
        });
    }
}
