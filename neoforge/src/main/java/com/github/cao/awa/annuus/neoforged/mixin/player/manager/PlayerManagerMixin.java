package com.github.cao.awa.annuus.neoforged.mixin.player.manager;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.debug.AnnuusDebugger;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import com.github.cao.awa.annuus.server.AnnuusServer;
import com.github.cao.awa.annuus.version.AnnuusVersion;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
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
        if (packet instanceof SynchronizeRecipesS2CPacket source) {
            int annuusProtocolVersion = AnnuusServer.getAnnuusVersion(instance);
            if (annuusProtocolVersion >= 4 && Annuus.CONFIG.isEnableShortRecipes()) {
                ServerRecipeManager recipeManager = this.server.getRecipeManager();

                ShortRecipeSyncPayload payload = ShortRecipeSyncPayload.createData(
                        recipeManager.getPropertySets(),
                        recipeManager.getStonecutterRecipeForSync()
                );

                CustomPayloadS2CPacket shortRecipeSyncPayloadPacket = ShortRecipeSyncPayload.createPacket(payload);

                instance.send(shortRecipeSyncPayloadPacket);

                if (AnnuusDebugger.enableDebugs) {
                    ShortRecipeSyncPayload.testEncode(source, this.server.getRegistryManager(), payload);
                }
            }
        } else {
            original.call(instance, packet);
        }
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At("RETURN")
    )
    public void requireClientAnnuusVersion(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        connection.send(NoticeUpdateServerAnnuusPayload.createPacket());
    }
}

