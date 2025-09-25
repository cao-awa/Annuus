package com.github.cao.awa.annuus.fabric;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class AnnuusFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Annuus.isServer = true;

        ServerPlayNetworking.registerGlobalReceiver(NoticeServerAnnuusPayload.IDENTIFIER, (packet, context) -> {
            NoticeServerAnnuusPayloadHandler.updateAnnuusVersion(packet, context.player().networkHandler);
        });
    }
}
