package com.github.cao.awa.annuus.mixin.chunk.sender;

import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkDataSender.class)
public class ChunkDataSenderMixin {
    @Shadow private int unacknowledgedBatches;
    @Shadow private float pending;
    @Unique
    private ServerPlayerEntity player;

    @Inject(
            method = "sendChunkBatches",
            at = @At("HEAD")
    )
    public void sendChunkBatches(ServerPlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }

    @Redirect(
            method = "sendChunkBatches",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z")
    )
    public boolean sendChunkBatches(List<WorldChunk> list) {
        // Only collect data when player installed annuus.
        if (((AnnuusVersionStorage) this.player).getAnnuusVersion() > -1) {
            System.out.println("Sending as annuus!");
            // Send chunks using the collected packet.
            if (!list.isEmpty()) {
                // Start sending.
                ServerWorld world = this.player.getServerWorld();
                ServerPlayNetworkHandler networkHandler = this.player.networkHandler;
                this.unacknowledgedBatches++;

                // Collect chunks.
                WorldChunk[] sendingChunks = list.toArray(WorldChunk[]::new);
                networkHandler.sendPacket(StartChunkSendS2CPacket.INSTANCE);
                networkHandler.sendPacket(CollectedChunkDataPayload.createPacket(sendingChunks, world.getLightingProvider()));
                networkHandler.sendPacket(new ChunkSentS2CPacket(list.size()));

                // Done sending.
                this.pending = this.pending - (float) list.size();
            }
            // Skip vanilla sending.
            return true;
        } else {
            System.out.println("Sending as Vanilla!");
        }
        // If the player doesn't install annuus, let vanilla send packets instead of annuus.
        return list.isEmpty();
    }
}
