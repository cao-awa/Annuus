package com.github.cao.awa.annuus.mixin.network.version.delegate;

import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommonNetworkHandler.class)
public class ServerCommonNetworkHandlerVersionDelegateMixin implements AnnuusVersionStorage {
    @Shadow @Final protected ClientConnection connection;

    @Override
    public int annuus$getAnnuusVersion() {
        return ((AnnuusVersionStorage) this.connection).getAnnuusVersion();
    }

    @Override
    public int annuus$setAnnuusVersion(int version) {
        return ((AnnuusVersionStorage) this.connection).setAnnuusVersion(version);
    }
}
