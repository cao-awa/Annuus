package com.github.cao.awa.annuus.mixin.player.manager;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.bzip2.Bzip2Compressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.information.compressor.lzma.LZMACompressor;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.recipe.ShortRecipeSyncPayload;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import com.github.cao.awa.annuus.server.AnnuusServer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnnuusPlayerManager");

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
        int annuusProtocolVersion = AnnuusServer.getAnnuusVersion(instance);
        boolean canUseShortRecipes = annuusProtocolVersion >= 4 && Annuus.CONFIG.isEnableShortRecipes();
        if (packet instanceof SynchronizeRecipesS2CPacket source && canUseShortRecipes) {
            ServerRecipeManager recipeManager = this.server.getRecipeManager();

            ShortRecipeSyncPayload payload = ShortRecipeSyncPayload.createData(
                    recipeManager.getPropertySets(),
                    recipeManager.getStonecutterRecipeForSync()
            );

            InformationCompressor informationCompressor = CollectedChunkBlockUpdatePayload.getCurrentCompressor();

            // TODO Generic version check system
            if (CollectedChunkDataPayload.getCurrentCompressor() instanceof Bzip2Compressor || CollectedChunkDataPayload.getCurrentCompressor() instanceof LZMACompressor) {
                // Older Annuus cannot use Bzip2 and LZMA compress, send by vanilla.
                if (AnnuusServer.getAnnuusVersion(this) < 5) {
                    ShortRecipeSyncPayload.setCurrentCompressor(DeflateCompressor.BEST_INSTANCE);
                }
            }

            CustomPayloadS2CPacket shortRecipeSyncPayloadPacket = ShortRecipeSyncPayload.createPacket(payload);

            instance.sendPacket(shortRecipeSyncPayloadPacket);

            ShortRecipeSyncPayload.setCurrentCompressor(informationCompressor);
        } else {
            original.call(instance, packet);
        }
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void requireClientAnnuusVersion(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        connection.send(NoticeUpdateServerAnnuusPayload.createPacket());
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At("HEAD")
    )
    public void onReady(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        int annuusVersion = AnnuusServer.getAnnuusVersion(connection);

        if (annuusVersion > -1) {
            LOGGER.info("Player {} joining server with Annuus protocol version {}", player.getName().getString(), annuusVersion);
        }
    }
}

