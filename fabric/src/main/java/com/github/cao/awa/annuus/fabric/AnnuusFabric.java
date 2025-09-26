package com.github.cao.awa.annuus.fabric;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.command.AnnuusConfigCommand;
import com.github.cao.awa.annuus.command.AnnuusDebugCommand;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnnuusFabric implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("AnnuusFabric");

    @Override
    public void onInitialize() {
        LOGGER.info("Loading annuus using fabric bootstrap");

        Annuus.loadingPlatform = "fabric";

        Annuus.onInitialize();

        PayloadTypeRegistry.playS2C().register(CollectedChunkDataPayload.IDENTIFIER, CollectedChunkDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CollectedBlockUpdatePayload.IDENTIFIER, CollectedBlockUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CollectedChunkBlockUpdatePayload.IDENTIFIER, CollectedChunkBlockUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShortRecipeSyncPayload.IDENTIFIER, ShortRecipeSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NoticeUpdateServerAnnuusPayload.IDENTIFIER, NoticeUpdateServerAnnuusPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NoticeServerAnnuusPayload.IDENTIFIER, NoticeServerAnnuusPayload.CODEC);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            AnnuusFabric.LOGGER.info("Registering commands");
            AnnuusDebugCommand.register(server);
            AnnuusConfigCommand.register(server);
        });
    }
}
