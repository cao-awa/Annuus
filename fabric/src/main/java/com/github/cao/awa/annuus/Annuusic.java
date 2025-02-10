package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.command.AnnuusConfigCommand;
import com.github.cao.awa.annuus.command.AnnuusDebugCommand;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class Annuusic implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Annuusic");

    @Override
    public void onInitialize() {
        LOGGER.info("Loading annuus using fabric bootstrap");

        Annuus.loadingPlatform = "fabric";

        Annuus.onInitialize();

        PayloadTypeRegistryImpl.PLAY_S2C.register(CollectedChunkDataPayload.IDENTIFIER, CollectedChunkDataPayload.CODEC);
        PayloadTypeRegistryImpl.PLAY_S2C.register(CollectedBlockUpdatePayload.IDENTIFIER, CollectedBlockUpdatePayload.CODEC);
        PayloadTypeRegistryImpl.PLAY_S2C.register(CollectedChunkBlockUpdatePayload.IDENTIFIER, CollectedChunkBlockUpdatePayload.CODEC);
        PayloadTypeRegistryImpl.CONFIGURATION_C2S.register(NoticeServerAnnuusPayload.IDENTIFIER, NoticeServerAnnuusPayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Annuusic.LOGGER.info("Registering commands");
            AnnuusDebugCommand.register(server);
            AnnuusConfigCommand.register(server);
        });
    }
}
