package com.github.cao.awa.annuus.chunk.data;

import com.github.cao.awa.annuus.information.compressor.bzip2.Bzip2Compressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.information.compressor.lzma.LZMACompressor;
import com.github.cao.awa.annuus.util.io.IOUtil;
import com.github.cao.awa.sinuatum.util.time.TimeUtil;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AnnuusChunkData {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusChunkData");
    private static final PacketCodec<ByteBuf, Map<Heightmap.Type, long[]>> HEIGHTMAPS_PACKET_CODEC;
    private static final int MAX_SECTIONS_DATA_SIZE = 2097152;
    private final Map<Heightmap.Type, long[]> heightmap;
    private final byte[] sectionsData;
    private final List<BlockEntityData> blockEntities;

    public AnnuusChunkData(WorldChunk chunk) {
        this.heightmap = chunk.getHeightmaps().stream().filter((entryx) -> entryx.getKey().shouldSendToClient()).collect(Collectors.toMap(Map.Entry::getKey, (entryx) -> entryx.getValue().asLongArray().clone()));
        this.sectionsData = new byte[getSectionsPacketSize(chunk)];
        writeSections(new PacketByteBuf(this.getWritableSectionsDataBuf()), chunk);
        this.blockEntities = Lists.newArrayList();

        for(Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            this.blockEntities.add(BlockEntityData.of(entry.getValue()));
        }
    }

    public AnnuusChunkData(RegistryByteBuf buf, int x, int z) {
        this.heightmap = HEIGHTMAPS_PACKET_CODEC.decode(buf);
        int i = buf.readVarInt();
        if (i > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        } else {
            this.sectionsData = new byte[i];
            buf.readBytes(this.sectionsData);
            this.blockEntities = BlockEntityData.LIST_PACKET_CODEC.decode(buf);
        }
    }

    public void write(RegistryByteBuf buf) {
        StringBuilder builder = new StringBuilder();

        try {
            Random random = new Random();
            File file = new File("test-data/" + random.nextInt() + ".dat");
            file.getParentFile().mkdirs();
            file.createNewFile();
            IOUtil.write(new FileOutputStream(file), this.sectionsData);
//            builder.append("--------");
//            builder.append("Vanilla: " + this.sectionsData.length);
//            builder.append("\n");
//
//            long start = System.currentTimeMillis();
//            byte[] compressedByDeflate = DeflateCompressor.BEST_INSTANCE.compress(this.sectionsData);
//            builder.append("Deflate done in " + (TimeUtil.millions() - start) + " ms");
//            builder.append("Deflate: " + compressedByDeflate.length);
//            builder.append(Arrays.equals(DeflateCompressor.BEST_INSTANCE.decompress(compressedByDeflate), this.sectionsData) ? "correct" : "error");
//            builder.append("\n");
//            LOGGER.info("Deflate done");
//
//            start = System.currentTimeMillis();
//            byte[] compressedBzip2 = Bzip2Compressor.INSTANCE.compress(this.sectionsData);
//            builder.append("Bzip2 done in " + (TimeUtil.millions() - start) + " ms");
//            builder.append("Bzip2: " + compressedBzip2.length);
//            builder.append(Arrays.equals(Bzip2Compressor.INSTANCE.decompress(compressedBzip2), this.sectionsData) ? "correct" : "error");
//            LOGGER.info("Bzip2 decompress done");
//            builder.append("\n");
//
//            start = System.currentTimeMillis();
//            byte[] compressedByLZMA = LZMACompressor.INSTANCE.compress(this.sectionsData);
//            builder.append("LZMA done in " + (TimeUtil.millions() - start) + " ms");
//            builder.append("LZMA: " + compressedByLZMA.length);
//            builder.append(Arrays.equals(LZMACompressor.INSTANCE.decompress(compressedByLZMA), this.sectionsData) ? "correct" : "error");
//            builder.append("\n");
//            LOGGER.info("LZMA done");
//
//            LOGGER.info(builder.toString());
        } catch (Exception e) {
            LOGGER.info("wtf?");

            e.printStackTrace();
        }
        LOGGER.info("awa");

        HEIGHTMAPS_PACKET_CODEC.encode(buf, this.heightmap);
        buf.writeVarInt(this.sectionsData.length);
        buf.writeBytes(this.sectionsData);
        BlockEntityData.LIST_PACKET_CODEC.encode(buf, this.blockEntities);
    }

    private static int getSectionsPacketSize(WorldChunk chunk) {
        int i = 0;

        for(ChunkSection chunkSection : chunk.getSectionArray()) {
            i += chunkSection.getPacketSize();
        }

        return i;
    }

    private ByteBuf getWritableSectionsDataBuf() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(this.sectionsData);
        byteBuf.writerIndex(0);
        return byteBuf;
    }

    public static void writeSections(PacketByteBuf buf, WorldChunk chunk) {
        for(ChunkSection chunkSection : chunk.getSectionArray()) {
            chunkSection.toPacket(buf);
        }

        if (buf.writerIndex() != buf.capacity()) {
            int var10002 = buf.capacity();
            throw new IllegalStateException("Didn't fill chunk buffer: expected " + var10002 + " bytes, got " + buf.writerIndex());
        }
    }

    public Consumer<BlockEntityVisitor> getBlockEntities(int x, int z) {
        return (visitor) -> this.iterateBlockEntities(visitor, x, z);
    }

    private void iterateBlockEntities(BlockEntityVisitor consumer, int x, int z) {
        int i = 16 * x;
        int j = 16 * z;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(BlockEntityData blockEntityData : this.blockEntities) {
            int k = i + ChunkSectionPos.getLocalCoord(blockEntityData.localXz >> 4);
            int l = j + ChunkSectionPos.getLocalCoord(blockEntityData.localXz);
            mutable.set(k, blockEntityData.y, l);
            consumer.accept(mutable, blockEntityData.type, blockEntityData.nbt);
        }

    }

    public PacketByteBuf getSectionsDataBuf() {
        return new PacketByteBuf(Unpooled.wrappedBuffer(this.sectionsData));
    }

    public Map<Heightmap.Type, long[]> getHeightmap() {
        return this.heightmap;
    }

    static {
        HEIGHTMAPS_PACKET_CODEC = PacketCodecs.map((size) -> new EnumMap<>(Heightmap.Type.class), Heightmap.Type.PACKET_CODEC, PacketCodecs.LONG_ARRAY);
    }

    static class BlockEntityData {
        public static final PacketCodec<RegistryByteBuf, BlockEntityData> PACKET_CODEC = PacketCodec.of(BlockEntityData::write, BlockEntityData::new);
        public static final PacketCodec<RegistryByteBuf, List<BlockEntityData>> LIST_PACKET_CODEC;
        final int localXz;
        final int y;
        final BlockEntityType<?> type;
        @Nullable
        final NbtCompound nbt;

        private BlockEntityData(int localXz, int y, BlockEntityType<?> type, @Nullable NbtCompound nbt) {
            this.localXz = localXz;
            this.y = y;
            this.type = type;
            this.nbt = nbt;
        }

        private BlockEntityData(RegistryByteBuf buf) {
            this.localXz = buf.readByte();
            this.y = buf.readShort();
            this.type = PacketCodecs.registryValue(RegistryKeys.BLOCK_ENTITY_TYPE).decode(buf);
            this.nbt = buf.readNbt();
        }

        private void write(RegistryByteBuf buf) {
            buf.writeByte(this.localXz);
            buf.writeShort(this.y);
            PacketCodecs.registryValue(RegistryKeys.BLOCK_ENTITY_TYPE).encode(buf, this.type);
            buf.writeNbt(this.nbt);
        }

        static BlockEntityData of(BlockEntity blockEntity) {
            NbtCompound nbtCompound = blockEntity.toInitialChunkDataNbt(blockEntity.getWorld().getRegistryManager());
            BlockPos blockPos = blockEntity.getPos();
            int i = ChunkSectionPos.getLocalCoord(blockPos.getX()) << 4 | ChunkSectionPos.getLocalCoord(blockPos.getZ());
            return new BlockEntityData(i, blockPos.getY(), blockEntity.getType(), nbtCompound.isEmpty() ? null : nbtCompound);
        }

        static {
            LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toList());
        }
    }

    @FunctionalInterface
    public interface BlockEntityVisitor {
        void accept(BlockPos pos, BlockEntityType<?> type, @Nullable NbtCompound nbt);
    }
}
