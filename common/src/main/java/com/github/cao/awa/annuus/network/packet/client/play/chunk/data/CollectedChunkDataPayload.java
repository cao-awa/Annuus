package com.github.cao.awa.annuus.network.packet.client.play.chunk.data;

import com.github.cao.awa.annuus.debug.AnnuusDebugger;
import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.util.compress.AnnuusCompressUtil;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public record CollectedChunkDataPayload(
        IntList xPositions,
        IntList zPositions,
        List<ChunkData> chunkData,
        List<LightData> lightData
) implements CustomPayload {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusCollectdChunkDataPayload");
    public static final Id<CollectedChunkDataPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:collected_chunk"));
    public static final PacketCodec<RegistryByteBuf, CollectedChunkDataPayload> CODEC = PacketCodec.ofStatic(
            CollectedChunkDataPayload::encode,
            CollectedChunkDataPayload::decode
    );
    private static InformationCompressor currentCompressor = DeflateCompressor.BEST_INSTANCE;

    public static void setCurrentCompressor(InformationCompressor compressor) {
        currentCompressor = compressor;
    }

    public static CustomPayloadS2CPacket createPacket(WorldChunk[] chunks, LightingProvider lightProvider) {
        return new CustomPayloadS2CPacket(createData(chunks, lightProvider));
    }

    public static CollectedChunkDataPayload createData(WorldChunk[] chunks, LightingProvider lightProvider) {
        IntList xPositions = new IntArrayList();
        IntList zPositions = new IntArrayList();
        List<ChunkData> chunksData = CollectionFactor.arrayList();
        List<LightData> lightsData = CollectionFactor.arrayList();

        for (WorldChunk chunk : chunks) {
            ChunkPos chunkPos = chunk.getPos();
            xPositions.add(chunkPos.x);
            zPositions.add(chunkPos.z);

            chunksData.add(new ChunkData(chunk));
            lightsData.add(new LightData(chunkPos, lightProvider, null, null));
        }

        return new CollectedChunkDataPayload(
                xPositions,
                zPositions,
                chunksData,
                lightsData
        );
    }

    private static CollectedChunkDataPayload decode(RegistryByteBuf buf) {
        try {
            RegistryByteBuf delegate = AnnuusCompressUtil.decompressRegistryBuf(buf);

            int size = delegate.readVarInt();

            IntList xPositions = new IntArrayList();
            IntList zPositions = new IntArrayList();

            for (int i = 0; i < size; i++) {
                xPositions.add(delegate.readVarInt());
            }

            for (int i = 0; i < size; i++) {
                zPositions.add(delegate.readVarInt());
            }

            List<ChunkData> chunkDataList = CollectionFactor.arrayList();

            for (int i = 0; i < size; i++) {
                int chunkX = xPositions.getInt(i);
                int chunkZ = zPositions.getInt(i);

                chunkDataList.add(new ChunkData(delegate, chunkX, chunkZ));
            }

            List<LightData> lightDataList = CollectionFactor.arrayList();
            for (int i = 0; i < size; i++) {
                int chunkX = xPositions.getInt(i);
                int chunkZ = zPositions.getInt(i);

                lightDataList.add(new LightData(delegate, chunkX, chunkZ));
            }

            return new CollectedChunkDataPayload(xPositions, zPositions, chunkDataList, lightDataList);
        } catch (Exception e) {
            LOGGER.error(e);
            throw e;
        }
    }

    private static void encode(RegistryByteBuf buf, CollectedChunkDataPayload packet) {
        RegistryByteBuf delegate = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), buf.getRegistryManager());

        int size = packet.chunkData.size();

        delegate.writeVarInt(size);

        for (int position : packet.xPositions) {
            delegate.writeVarInt(position);
        }
        for (int position : packet.zPositions) {
            delegate.writeVarInt(position);
        }
        for (ChunkData chunkData : packet.chunkData) {
            chunkData.write(delegate);
        }
        for (LightData lightData : packet.lightData) {
            lightData.write(delegate);
        }

        AnnuusCompressUtil.doCompress(buf, delegate, () -> currentCompressor);

        if (AnnuusDebugger.enableDebugs) {
            AnnuusDebugger.processedChunks += size;
            AnnuusDebugger.processedChunksBytes += buf.readableBytes();
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
