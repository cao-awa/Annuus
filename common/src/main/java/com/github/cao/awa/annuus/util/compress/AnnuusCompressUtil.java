package com.github.cao.awa.annuus.util.compress;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;

public class AnnuusCompressUtil {
    public static void doCompress(ByteBuf buf, ByteBuf delegate) {
        byte[] bytes = new byte[delegate.readableBytes()];

        delegate.readBytes(bytes);

        byte[] data;
        if (Annuus.CONFIG.isChunkBestCompress()) {
            data = DeflateCompressor.BEST_INSTANCE.compress(bytes);
        } else {
            data = DeflateCompressor.FASTEST_INSTANCE.compress(bytes);
        }

        buf.writeInt(data.length);

        buf.writeBytes(data);
    }

    public static PacketByteBuf doDecompress(PacketByteBuf buf) {
        int packetSize = buf.readInt();

        byte[] data = new byte[packetSize];

        buf.readBytes(data);

        if (Annuus.CONFIG.isChunkBestCompress()) {
            data = DeflateCompressor.BEST_INSTANCE.decompress(data);
        } else {
            data = DeflateCompressor.FASTEST_INSTANCE.decompress(data);
        }

        return new PacketByteBuf(Unpooled.copiedBuffer(data));
    }

    public static RegistryByteBuf doDecompressRegistryBuf(RegistryByteBuf buf) {
        return new RegistryByteBuf(doDecompress(buf), buf.getRegistryManager());
    }
}
