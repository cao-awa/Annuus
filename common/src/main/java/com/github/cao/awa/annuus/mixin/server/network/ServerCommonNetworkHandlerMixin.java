package com.github.cao.awa.annuus.mixin.server.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerMixin implements AnnuusVersionStorage {
    @Unique
    private int annuusVersion = -1;

    @Override
    public int annuus$getAnnuusVersion() {
        return this.annuusVersion;
    }

    @Override
    public int annuus$setAnnuusVersion(int version) {
        this.annuusVersion = version;
        return version;
    }
}
