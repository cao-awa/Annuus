package com.github.cao.awa.annuus.mixin.client.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.debug.AnnuusDebugger;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationNetworkHandler.class)
public class ClientConfigurationNetworkHandlerMixin implements AnnuusVersionStorage {
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
