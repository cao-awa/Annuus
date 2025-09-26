package com.github.cao.awa.annuus.version.storage;

import com.github.cao.awa.annuus.map.expire.ExpiringMap;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;

import java.util.concurrent.TimeUnit;

public class AnnuusVersionStorage {
    private final ExpiringMap<ClientConnection, Integer> versions = ExpiringMap.createFrom(
                    ClientConnection.class,
                    Integer.class
            )
            .enableExpiration()
            .setExpiration(2, TimeUnit.HOURS)
            .whenExpiration(((connection, value) -> {
                if (connection.isOpen()) {
                    noticeUpdateAnnuusVersion(connection);
                }
            }));

    public int getAnnuusVersion(ClientConnection target) {
        return this.versions.getOrDefault(target, -1);
    }

    public int setAnnuusVersion(ClientConnection target, int version) {
        this.versions.put(target, version);

        return version;
    }

    public static void noticeUpdateAnnuusVersion(ClientConnection connection) {
        connection.send(NoticeUpdateServerAnnuusPayload.createPacket());
    }

    public static void noticeUpdateAnnuusVersion(ServerCommonNetworkHandler networkHandler) {
        networkHandler.send(NoticeUpdateServerAnnuusPayload.createPacket(), null);
    }
}
