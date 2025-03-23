package com.github.cao.awa.annuus.mixin.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedBlockUpdatePayload;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedChunkBlockUpdatePayload;
import com.github.cao.awa.annuus.update.ChunkBlockUpdateDetails;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements AnnuusVersionStorage {
    @Shadow
    public abstract void send(Packet<?> packet, PacketCallbacks callbacks);
    @Unique
    private int annuusVersion = -1;
    @Unique
    private Map<Long, BlockState> updates = new Long2ObjectRBTreeMap<>();
    @Unique
    private Map<Long, ChunkBlockUpdateDetails> chunkUpdates = new Long2ObjectRBTreeMap<>();

    @Unique
    @Override
    public int annuus$getAnnuusVersion() {
        return this.annuusVersion;
    }

    @Unique
    @Override
    public int annuus$setAnnuusVersion(int version) {
        return this.annuusVersion = version;
    }

    @Inject(
            method = "send",
            at = @At("HEAD"),
            cancellable = true
    )
    public void collectBlockUpdate(Packet<?> packet, CallbackInfo ci) {
        if (this.annuusVersion >= 3 && Annuus.CONFIG.isEnableBlockUpdatesCompress()) {
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
            method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J",
                    shift = At.Shift.AFTER
            )
    )
    public void sendCollectedBlockUpdates(CallbackInfo ci) {
        if (!this.updates.isEmpty()) {
            send(CollectedBlockUpdatePayload.createPacket(new Long2ObjectRBTreeMap<>(this.updates)), null);

            this.updates.clear();
        }

        if (!this.chunkUpdates.isEmpty()) {
            send(CollectedChunkBlockUpdatePayload.createPacket(new Long2ObjectRBTreeMap<>(this.chunkUpdates)), null);

            this.chunkUpdates.clear();
        }
    }
}
