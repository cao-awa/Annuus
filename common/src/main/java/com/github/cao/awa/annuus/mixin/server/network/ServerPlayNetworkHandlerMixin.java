package com.github.cao.awa.annuus.mixin.server.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
abstract public class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnnuusPlayHandler");

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initAnnuusVersion(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        AnnuusVersionStorage versionStorage = (AnnuusVersionStorage) this;

        // Setting annuus version.
        versionStorage.setAnnuusVersion(((AnnuusVersionStorage) player).getAnnuusVersion());

        if (versionStorage.getAnnuusVersion() > -1) {
            LOGGER.info("Player {} updating Annuus protocol version {}", player.getName().getString(), versionStorage.getAnnuusVersion());

            ((AnnuusVersionStorage) this.connection).setAnnuusVersion(versionStorage.getAnnuusVersion());
        }
    }
}
