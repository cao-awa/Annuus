package com.github.cao.awa.annuus.mixin.player;

import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements AnnuusVersionStorage {
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
