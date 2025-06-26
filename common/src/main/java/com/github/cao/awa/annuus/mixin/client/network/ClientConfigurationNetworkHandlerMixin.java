package com.github.cao.awa.annuus.mixin.client.network;

import com.github.cao.awa.annuus.debug.AnnuusDebugger;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationNetworkHandler.class)
public class ClientConfigurationNetworkHandlerMixin {
    @Inject(
            method = "onReady",
            at = @At(
                    value = "HEAD"
            )
    )
    public void onReady(ReadyS2CPacket packet, CallbackInfo ci) {
        AnnuusDebugger.processedChunksBytes = 0;
        AnnuusDebugger.processedChunks = 0;
    }
}
