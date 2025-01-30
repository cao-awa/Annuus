package com.github.cao.awa.annuus.mixin.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin implements AnnuusVersionStorage {
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
}
