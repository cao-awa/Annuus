package com.github.cao.awa.annuus.mixin.server;

import com.github.cao.awa.annuus.server.AnnuusServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract public class MinecraftServerMixin {
    @Shadow
    private PlayerManager playerManager;

    @Inject(
            method = "loadWorld",
            at = @At("HEAD")
    )
    public void setupServer(CallbackInfo ci) {
        AnnuusServer.setupServerPlayerManager(this.playerManager);
    }
}
