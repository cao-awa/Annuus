package com.github.cao.awa.annuus.util.compress;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;

import java.util.function.Supplier;

public class AnnuusCompressUtil {
    public static void doCompress(ByteBuf buf, ByteBuf delegate, Supplier<InformationCompressor> compressor) {
        byte[] bytes = new byte[delegate.readableBytes()];

        delegate.readBytes(bytes);

        byte[] data = compressor.get().compress(bytes);

        buf.writeInt(data.length);

        buf.writeBytes(data);
    }

    public static PacketByteBuf doDecompress(PacketByteBuf buf, Supplier<InformationCompressor> compressor) {
        int packetSize = buf.readInt();

        byte[] data = new byte[packetSize];

        buf.readBytes(data);

        data = compressor.get().compress(data);

        return new PacketByteBuf(Unpooled.copiedBuffer(data));
    }

    public static RegistryByteBuf doDecompressRegistryBuf(RegistryByteBuf buf, Supplier<InformationCompressor> compressor) {
        return new RegistryByteBuf(doDecompress(buf, compressor), buf.getRegistryManager());
    }
}
