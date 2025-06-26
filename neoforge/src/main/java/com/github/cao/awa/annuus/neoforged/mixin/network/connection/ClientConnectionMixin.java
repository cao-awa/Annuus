package com.github.cao.awa.annuus.neoforged.mixin.network.connection;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.mixin.network.packet.ChunkDeltaUpdateS2CPacketAccessor;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.chunk.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.server.AnnuusServer;
import com.github.cao.awa.annuus.update.ChunkBlockUpdateDetails;
import io.netty.channel.ChannelFutureListener;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;

@Mixin(ClientConnection.class)
abstract public class ClientConnectionMixin {
    @Shadow protected abstract void sendImmediately(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush);

    @Unique
    private Map<Long, BlockState> updates = new Long2ObjectRBTreeMap<>();
    @Unique
    private Map<Long, ChunkBlockUpdateDetails> chunkUpdates = new Long2ObjectRBTreeMap<>();

    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void collectBlockUpdate(Packet<?> packet, ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
        if (Annuus.isServer && AnnuusServer.getAnnuusVersion(this) >= 3 && Annuus.CONFIG.isEnableBlockUpdatesCompress()) {
            boolean shouldCancel = false;

            if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
                this.updates.put(blockUpdatePacket.getPos().asLong(), blockUpdatePacket.getState());

                shouldCancel = true;
            }

            if (packet instanceof ChunkDeltaUpdateS2CPacket chunkDeltaUpdatePacket) {
                ChunkDeltaUpdateS2CPacketAccessor accessor = (ChunkDeltaUpdateS2CPacketAccessor) chunkDeltaUpdatePacket;

                this.chunkUpdates.put(
                        accessor.getChunkSectionpos().asLong(),
                        new ChunkBlockUpdateDetails(
                                accessor.getPosArray(),
                                Arrays.stream(accessor.getStateArray()).mapToInt(Block::getRawIdFromState).toArray()
                        )
                );

                shouldCancel = true;
            }

            if (shouldCancel) {
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void sendCollectedBlockUpdates(CallbackInfo ci) {
        if (!this.updates.isEmpty()) {
            sendImmediately(CollectedBlockUpdatePayload.createPacket(new Long2ObjectRBTreeMap<>(this.updates)), null, true);

            this.updates.clear();
        }

        if (!this.chunkUpdates.isEmpty()) {
            sendImmediately(CollectedChunkBlockUpdatePayload.createPacket(new Long2ObjectRBTreeMap<>(this.chunkUpdates)), null, true);

            this.chunkUpdates.clear();
        }
    }
}
