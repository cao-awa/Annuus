package com.github.cao.awa.annuus;

import com.github.cao.awa.annuus.command.AnnuusConfigCommand;
import com.github.cao.awa.annuus.command.AnnuusDebugCommand;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayloadHandler;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.server.notice.NoticeServerAnnuusPayloadHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("annuus")
public class Neoannuus {
    public static final Logger LOGGER = LogManager.getLogger("Neoannuus");

    public Neoannuus(IEventBus eventBus) {
        LOGGER.info("Loading annuus using neoforge bootstrap");

        Annuus.loadingPlatform = "neoforge";

        Annuus.onInitialize();

        // Sets the current network version
        PayloadRegistrar registrar = new PayloadRegistrar("1");

//        PayloadTypeRegistryImpl.PLAY_S2C.register(CollectedChunkDataPayload.IDENTIFIER, CollectedChunkDataPayload.CODEC);
//        PayloadTypeRegistryImpl.CONFIGURATION_C2S.register(NoticeServerAnnuusPayload.IDENTIFIER, NoticeServerAnnuusPayload.CODEC);

        registrar.playToClient(CollectedChunkDataPayload.IDENTIFIER, CollectedChunkDataPayload.CODEC, (payload, context) -> {
            CollectedChunkDataPayloadHandler.loadChunksFromPayload(payload, MinecraftClient.getInstance(), (ClientPlayerEntity) context.player());
        });

        registrar.playToServer(NoticeServerAnnuusPayload.IDENTIFIER, NoticeServerAnnuusPayload.CODEC, (payload, context) -> {
            NoticeServerAnnuusPayloadHandler.updateAnnuusVersionDuringPlay(payload, context.player());
        });

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommand(RegisterCommandsEvent event) {
        CommandDispatcher<ServerCommandSource> dispatcher = event.getDispatcher();

        Neoannuus.LOGGER.info("Registering annuus commands");
        AnnuusDebugCommand.register(dispatcher);
        AnnuusConfigCommand.register(dispatcher);
    }
}
