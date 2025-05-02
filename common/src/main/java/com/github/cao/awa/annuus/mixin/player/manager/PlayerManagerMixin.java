package com.github.cao.awa.annuus.mixin.player.manager;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
                    ordinal = 4
            )
    )
    public void redirectSyncRecipes(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
        int annuusProtocolVersion = ((AnnuusVersionStorage) instance).getAnnuusVersion();
        if (Annuus.isServer && annuusProtocolVersion >= 4 && Annuus.CONFIG.isEnableShortRecipes()) {
            if (packet instanceof SynchronizeRecipesS2CPacket source) {
                RegistryByteBuf buf = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), this.server.getRegistryManager());
                SynchronizeRecipesS2CPacket.CODEC.encode(buf, source);
                LOGGER.info("Source recipes size: {} bytes", buf.readableBytes());

                instance.sendPacket(
                        ShortRecipeSyncPayload.createPacket(
                                this.server.getRecipeManager().getPropertySets(),
                                this.server.getRecipeManager().getStonecutterRecipeForSync()
                        )
                );
            }
        } else {
            original.call(instance, packet);
        }
    }
}
