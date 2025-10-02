package com.github.cao.awa.annuus.mixin.server;

import com.github.cao.awa.annuus.server.AnnuusServer;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.util.ApiServices;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
abstract public class MinecraftServerMixin {
    @Shadow
    private PlayerManager playerManager;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    public void setupServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, ChunkLoadProgress chunkLoadProgress, CallbackInfo ci) {
        AnnuusServer.setupServerPlayerManager(this.playerManager);
    }
}
