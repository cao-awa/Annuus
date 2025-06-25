package com.github.cao.awa.annuus.mixin.network.version.delegate;

import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityVersionDelegateMixin implements AnnuusVersionStorage {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Override
    public int annuus$getAnnuusVersion() {
        return ((AnnuusVersionStorage) this.networkHandler).getAnnuusVersion();
    }

    @Override
    public int annuus$setAnnuusVersion(int version) {
        return ((AnnuusVersionStorage) this.networkHandler).setAnnuusVersion(version);
    }
}
