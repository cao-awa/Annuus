package com.github.cao.awa.annuus.mixin.client.network.packet;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public interface ChunkDeltaUpdateS2CPacketAccessor {
    @Accessor("positions")
    short[] getPosArray();

    @Accessor("blockStates")
    BlockState[] getStateArray();

    @Accessor("sectionPos")
    ChunkSectionPos getChunkSectionpos();
}
