package com.github.cao.awa.annuus.mixin.network.version.delegate;

import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientConnection.class)
abstract public class GenericConnectionVersionDelegateMixin implements AnnuusVersionStorage {
    @Unique
    private int annuusVersion = -1;

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
