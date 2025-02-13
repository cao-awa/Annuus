package com.github.cao.awa.annuus.network.packet.client.play.block.update;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.util.compress.AnnuusCompressUtil;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.Map;

public record CollectedBlockUpdatePayload(
        Map<Long, BlockState> committed
) implements CustomPayload {
    public static final Id<CollectedBlockUpdatePayload> IDENTIFIER = new Id<>(Identifier.of("annuus:collected_blocks"));
    public static final PacketCodec<RegistryByteBuf, CollectedBlockUpdatePayload> CODEC = PacketCodec.ofStatic(
            CollectedBlockUpdatePayload::encode,
            CollectedBlockUpdatePayload::decode
    );
    private static InformationCompressor currentCompressor = DeflateCompressor.BEST_INSTANCE;

    public static void setCurrentCompressor(InformationCompressor compressor) {
        currentCompressor = compressor;
    }

    public static CustomPayloadS2CPacket createPacket(Map<Long, BlockState> updates) {
        return new CustomPayloadS2CPacket(createData(updates));
    }

    public static CollectedBlockUpdatePayload createData(Map<Long, BlockState> updates) {
        return new CollectedBlockUpdatePayload(updates);
    }

    private static CollectedBlockUpdatePayload decode(RegistryByteBuf buf) {
        try {
            RegistryByteBuf delegate = AnnuusCompressUtil.doDecompressRegistryBuf(buf);

            int size = delegate.readInt();

            long[] positions = new long[size];
            BlockState[] states = new BlockState[size];

            for (int i = 0; i < size; i++) {
                positions[i] = delegate.readLong();
            }

            PacketCodec<ByteBuf, BlockState> blockStateCodec = PacketCodecs.entryOf(Block.STATE_IDS);
            for (int i = 0; i < size; i++) {
                states[i] = blockStateCodec.decode(delegate);
            }

            Map<Long, BlockState> committed = CollectionFactor.hashMap();
            for (int i = 0; i < size; i++) {
                committed.put(positions[i], states[i]);
            }

            return new CollectedBlockUpdatePayload(committed);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void encode(RegistryByteBuf buf, CollectedBlockUpdatePayload packet) {
        RegistryByteBuf delegate = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), buf.getRegistryManager());

        int size = packet.committed.size();

        delegate.writeInt(size);

        for (long position : packet.committed.keySet()) {
            delegate.writeLong(position);
        }

        PacketCodec<ByteBuf, BlockState> blockStateCodec = PacketCodecs.entryOf(Block.STATE_IDS);
        for (BlockState blockState : packet.committed.values()) {
            blockStateCodec.encode(delegate, blockState);
        }

        AnnuusCompressUtil.doCompress(buf, delegate, () -> currentCompressor);

        if (Annuus.enableDebugs) {
            Annuus.processedChunks += size;
            Annuus.processedBytes += buf.readableBytes();
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
