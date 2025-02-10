package com.github.cao.awa.annuus.mixin.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnnuusPlayHandler");

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initAnnuusVersion(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        AnnuusVersionStorage versionStorage = ((AnnuusVersionStorage) this);

        // Setting annuus version.
        versionStorage.setAnnuusVersion(((AnnuusVersionStorage) player).getAnnuusVersion());

        if (versionStorage.getAnnuusVersion() > -1) {
            LOGGER.info("Player {} updating Annuus protocol version {}", player.getName().getString(), versionStorage.getAnnuusVersion());
        }
    }
}
