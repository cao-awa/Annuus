package com.github.cao.awa.annuus.mixin.chunk;

import com.github.cao.awa.annuus.Annuus;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.handler.DecoderHandler;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.SocketAddress;

@Mixin(DecoderHandler.class)
public class DecoderHandlerMixin {

}
