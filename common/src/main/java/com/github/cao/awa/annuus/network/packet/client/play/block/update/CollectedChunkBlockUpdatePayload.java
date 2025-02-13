package com.github.cao.awa.annuus.network.packet.client.play.block.update;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.update.ChunkBlockUpdateDetails;
import com.github.cao.awa.annuus.util.compress.AnnuusCompressUtil;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public record CollectedChunkBlockUpdatePayload(
        Map<Long, ChunkBlockUpdateDetails> details
) implements CustomPayload {
    public static final Id<CollectedChunkBlockUpdatePayload> IDENTIFIER = new Id<>(Identifier.of("annuus:collected_chunk_blocks"));
    public static final PacketCodec<RegistryByteBuf, CollectedChunkBlockUpdatePayload> CODEC = PacketCodec.ofStatic(
            CollectedChunkBlockUpdatePayload::encode,
            CollectedChunkBlockUpdatePayload::decode
    );
    private static InformationCompressor currentCompressor = DeflateCompressor.BEST_INSTANCE;

    public static void setCurrentCompressor(InformationCompressor compressor) {
        currentCompressor = compressor;
    }

    public static CustomPayloadS2CPacket createPacket(ChunkSectionPos sectionPos, short[] positions, BlockState[] updates) {
        Map<Long, ChunkBlockUpdateDetails> details = new Long2ObjectRBTreeMap<>();
        details.put(
                sectionPos.asLong(),
                new ChunkBlockUpdateDetails(
                        positions,
                        Arrays.stream(updates).mapToInt(Block::getRawIdFromState).toArray()
                )
        );
        return createPacket(details);
    }

    public static CustomPayloadS2CPacket createPacket(Map<Long, ChunkBlockUpdateDetails> details) {
        return new CustomPayloadS2CPacket(createData(details));
    }

    public static CollectedChunkBlockUpdatePayload createData(ChunkSectionPos sectionPos, short[] positions, BlockState[] updates) {
        Map<Long, ChunkBlockUpdateDetails> details = new Long2ObjectRBTreeMap<>();
        details.put(
                sectionPos.asLong(),
                new ChunkBlockUpdateDetails(
                        positions,
                        Arrays.stream(updates).mapToInt(Block::getRawIdFromState).toArray()
                )
        );
        return new CollectedChunkBlockUpdatePayload(details);
    }

    public static CollectedChunkBlockUpdatePayload createData(Map<Long, ChunkBlockUpdateDetails> details) {
        return new CollectedChunkBlockUpdatePayload(details);
    }

    private static CollectedChunkBlockUpdatePayload decode(PacketByteBuf buf) {
        try {
            PacketByteBuf delegate = AnnuusCompressUtil.doDecompress(buf, () -> currentCompressor);

            int chunks = delegate.readInt();

            long[] chunkPositions = new long[chunks];

            for (int i = 0; i < chunks; i++) {
                chunkPositions[i] = delegate.readLong();
            }

            int[] updates = new int[chunks];
            for (int i = 0; i < chunks; i++) {
                updates[i] = delegate.readInt();
            }

            short[][] updatePositions = new short[chunks][];
            for (int i = 0; i < chunks; i++) {
                updatePositions[i] = new short[updates[i]];
                for (int posIndex = 0; posIndex < updates[i]; posIndex++) {
                    updatePositions[i][posIndex] = delegate.readShort();
                }
            }

            int[][] updateStates = new int[chunks][];
            for (int i = 0; i < chunks; i++) {
                updateStates[i] = new int[updates[i]];
                for (int stateIndex = 0; stateIndex < updates[i]; stateIndex++) {
                    updateStates[i][stateIndex] = delegate.readInt();
                }
            }

            Map<Long, ChunkBlockUpdateDetails> details = new Long2ObjectRBTreeMap<>();

            int i = 0;
            for (long chunkPosition : chunkPositions) {
                details.put(
                        chunkPosition,
                        new ChunkBlockUpdateDetails(
                                updatePositions[i],
                                updateStates[i]
                        )
                );

                i++;
            }

            return createData(details);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void encode(PacketByteBuf buf, CollectedChunkBlockUpdatePayload packet) {
        PacketByteBuf delegate = new PacketByteBuf(Unpooled.buffer());

        Set<Long> chunks = packet.details.keySet();

        int size = chunks.size();

        delegate.writeInt(size);

        for (long chunkPos : chunks) {
            delegate.writeLong(chunkPos);
        }

        Collection<ChunkBlockUpdateDetails> updateDetails = packet.details.values();

        for (ChunkBlockUpdateDetails updateDetail : updateDetails) {
            delegate.writeInt(updateDetail.positions().length);

            if (Annuus.enableDebugs) {
                Annuus.processedBlockUpdates += updateDetail.positions().length;
            }
        }

        for (ChunkBlockUpdateDetails updateDetail : updateDetails) {
            for (short position : updateDetail.positions()) {
                delegate.writeShort(position);
            }
        }

        for (ChunkBlockUpdateDetails updateDetail : updateDetails) {
            for (int states : updateDetail.states()) {
                delegate.writeInt(states);
            }
        }

        AnnuusCompressUtil.doCompress(buf, delegate, () -> currentCompressor);

        if (Annuus.enableDebugs) {
            Annuus.processedBlockUpdateBytes += buf.readableBytes();
        }
    }
    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
