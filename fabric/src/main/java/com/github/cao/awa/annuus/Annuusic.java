package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.config.AnnuusConfig;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.fabricmc.api.ModInitializer;
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
        PayloadTypeRegistryImpl.CONFIGURATION_C2S.register(NoticeServerAnnuusPayload.IDENTIFIER, NoticeServerAnnuusPayload.CODEC);
    }
}
