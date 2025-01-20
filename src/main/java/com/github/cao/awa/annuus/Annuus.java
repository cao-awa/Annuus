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

public class Annuus implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Annuus");
    public static final String VERSION = "1.0.1";
    public static final int PROTOCOL_VERSION_ID = 0;
    public static final AnnuusConfig CONFIG = new AnnuusConfig();
    public static final AnnuusConfig PERSISTENT_CONFIG = new AnnuusConfig();
    public static Set<String> LOADED_MODS = CollectionFactor.hashSet();
    public static String loadingPlatform = "fabric";
    public static long processedChunks = 0;
    public static long processedBytes = 0;
    public static double calculatedTimes = 0D;
    public static boolean enableDebugs = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Annuus '{}' loading on platform '{}'", VERSION, loadingPlatform);
        CONFIG.load();
        PERSISTENT_CONFIG.copyFrom(CONFIG);
        writeConfig();
        CONFIG.print();

        PayloadTypeRegistryImpl.PLAY_S2C.register(CollectedChunkDataPayload.IDENTIFIER, CollectedChunkDataPayload.CODEC);
        PayloadTypeRegistryImpl.CONFIGURATION_C2S.register(NoticeServerAnnuusPayload.IDENTIFIER, NoticeServerAnnuusPayload.CODEC);
    }

    public static void writeConfig() {
        PERSISTENT_CONFIG.write();
    }
}
