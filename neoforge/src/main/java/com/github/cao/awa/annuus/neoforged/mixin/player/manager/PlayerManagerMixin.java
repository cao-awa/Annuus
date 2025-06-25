package com.github.cao.awa.annuus.neoforged.mixin.player.manager;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusRecipes");

    @Shadow
    @Final
    private MinecraftServer server;

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;send(Lnet/minecraft/network/packet/Packet;)V",
                    ordinal = 4
            )
    )
    public void redirectSyncRecipes(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
        int annuusProtocolVersion = ((AnnuusVersionStorage) instance).getAnnuusVersion();
        if (Annuus.isServer && annuusProtocolVersion >= 4 && Annuus.CONFIG.isEnableShortRecipes()) {
            if (packet instanceof SynchronizeRecipesS2CPacket) {
                instance.send(
                        ShortRecipeSyncPayload.createPacket(
                                this.server.getRecipeManager().sortedValues().toArray(RecipeEntry[]::new)
                        )
                );
            }
        } else {
            original.call(instance, packet);
        }
    }
}
