package com.github.cao.awa.annuus.mixin.network;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.network.packet.client.play.block.update.CollectedChunkBlockUpdatePayload;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public class ChunkDeltaUpdateS2CPacketMixin {
    @Inject(
            method = "write",
            at = @At("RETURN")
    )
    public void encode(PacketByteBuf buf, CallbackInfo ci) {
        if (Annuus.enableDebugs) {
            Annuus.vanillaBlockUpdateBytes += buf.readableBytes();
        }
    }
}
