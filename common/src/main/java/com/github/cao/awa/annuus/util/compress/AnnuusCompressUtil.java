package com.github.cao.awa.annuus.util.compress;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.InformationCompressors;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;

import java.util.Arrays;
import java.util.function.Supplier;

public class AnnuusCompressUtil {
    public static void doCompress(PacketByteBuf buf, PacketByteBuf delegate, Supplier<InformationCompressor> compressorSupplier) {
        byte[] bytes = new byte[delegate.readableBytes()];

        delegate.readBytes(bytes);

        InformationCompressor compressor = compressorSupplier.get();
        buf.writeVarInt(compressor.getId());

        byte[] data = compressor.compress(bytes);

        buf.writeVarInt(data.length);
        buf.writeBytes(data);
    }

    public static PacketByteBuf doDecompress(PacketByteBuf buf) {
        int compressorId = buf.readVarInt();
        int packetSize = buf.readVarInt();

        byte[] data = new byte[packetSize];

        buf.readBytes(data);

        data = InformationCompressors.getCompressor(compressorId).decompress(data);

        return new PacketByteBuf(Unpooled.wrappedBuffer(data));
    }

    public static RegistryByteBuf doDecompressRegistryBuf(RegistryByteBuf buf) {
        return new RegistryByteBuf(doDecompress(buf), buf.getRegistryManager());
    }
}
