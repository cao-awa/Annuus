package com.github.cao.awa.annuus.mixin.chunk.sender;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkDataSender.class)
public class ChunkDataSenderMixin {
    @Shadow
    private int unacknowledgedBatches;
    @Shadow
    private float pending;
    @Unique
    private ServerPlayerEntity player;
    @Unique
    private static long start = 0;

    @Inject(
            method = "sendChunkBatches",
            at = @At("HEAD")
    )
    public void sendChunkBatches(ServerPlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }

    @WrapOperation(
            method = "sendChunkBatches",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z")
    )
    public boolean sendChunkBatches(List<WorldChunk> list, Operation<Boolean> original) {
        // Only collect data when player installed annuus.
        if (Annuus.isServer && ((AnnuusVersionStorage) this.player).getAnnuusVersion() >= 3 && Annuus.CONFIG.isEnableChunkCompress()) {
            // Send chunks using the collected packet.
            if (!list.isEmpty()) {
                // Start sending.
                ServerWorld world = this.player.getServerWorld();
                ServerPlayNetworkHandler networkHandler = this.player.networkHandler;
                this.unacknowledgedBatches++;

                // Collect chunks.
                WorldChunk[] sendingChunks = list.toArray(WorldChunk[]::new);
                networkHandler.send(StartChunkSendS2CPacket.INSTANCE, null);

                long start = System.nanoTime();
                networkHandler.send(CollectedChunkDataPayload.createPacket(sendingChunks, world.getLightingProvider()), null);
                if (Annuus.enableDebugs) {
                    Annuus.calculatedTimes += (System.nanoTime() - start) / 1000000D;
                    Annuus.processedChunks += sendingChunks.length;
                }
                networkHandler.send(new ChunkSentS2CPacket(list.size()), null);

                // Done sending.
                this.pending = this.pending - (float) list.size();
            }

            // Skip vanilla sending.
            return true;
        }
        // If the player doesn't install annuus, let vanilla send packets instead of annuus.
        return original.call(list);
    }

    @Inject(
            method = "sendChunkData",
            at = @At("HEAD")
    )
    private static void startSendOneChunk(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk, CallbackInfo ci) {
        if (Annuus.enableDebugs) {
            start = System.nanoTime();
            Annuus.processedChunks++;
        }
    }

    @Inject(
            method = "sendChunkData",
            at = @At("RETURN")
    )
    private static void doneSendOneChunk(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk, CallbackInfo ci) {
        if (Annuus.enableDebugs) {
            Annuus.calculatedTimes += (System.nanoTime() - start) / 1000000D;
        }
    }
}
