package com.github.cao.awa.annuus.version;

import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;

public class AnnuusVersion {
    public static void tryUpdateAnnuusVersion(ClientConnection connection) {
        connection.send(NoticeServerAnnuusPayload.createPacket());
    }

    public static void tryUpdateAnnuusVersion(ClientPlayNetworkHandler networkHandler) {
        networkHandler.sendPacket(NoticeServerAnnuusPayload.createPacket());
    }
}
