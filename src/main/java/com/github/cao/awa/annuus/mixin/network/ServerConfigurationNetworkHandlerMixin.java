package com.github.cao.awa.annuus.mixin.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerConfigurationNetworkHandler.class)
public class ServerConfigurationNetworkHandlerMixin implements AnnuusVersionStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnnuusConfigurationHandler");
    @Unique
    private int annuusVersion = -1;

    @Unique
    @Override
    public int annuus$getAnnuusVersion() {
        return this.annuusVersion;
    }

    @Unique
    @Override
    public int annuus$setAnnuusVersion(int version) {
        return this.annuusVersion = version;
    }

    @Redirect(
            method = "onReady",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V"
            )
    )
    public void onReady(PlayerManager instance, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        // Setting annuus version.
        ((AnnuusVersionStorage) player).setAnnuusVersion(this.annuusVersion);

        if (this.annuusVersion > -1) {
            LOGGER.info("Player {} joining server with Annuus protocol version {}", player.getName().getString(), this.annuusVersion);
        }

        Annuus.processedChunks = 0;
        Annuus.calculatedTimes = 0;

        // Connect to server.
        instance.onPlayerConnect(connection, player, clientData);
    }
}
