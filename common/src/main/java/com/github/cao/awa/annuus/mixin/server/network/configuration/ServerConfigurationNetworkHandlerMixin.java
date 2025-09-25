package com.github.cao.awa.annuus.mixin.server.network.configuration;

import com.github.cao.awa.annuus.server.AnnuusServer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConfigurationNetworkHandler.class)
public class ServerConfigurationNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnnuusConfigurationHandler");

    @WrapOperation(
            method = "onReady",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V"
            )
    )
    public void onReady(PlayerManager instance, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, Operation<Void> original) {
        int annuusVersion = AnnuusServer.getAnnuusVersion(connection);

        if (annuusVersion > -1) {
            LOGGER.info("Player {} joining server with Annuus protocol version {}", player.getName().getString(), annuusVersion);
        }

//        if (Annuus.enableDebugs) {
//            Annuus.processedChunks = 0;
//            Annuus.calculatedTimes = 0;
//        }

        // Connect to server.
        original.call(instance, connection, player, clientData);
    }
}
