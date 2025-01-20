package com.github.cao.awa.annuus.network.packet.client.play.chunk.data;

import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.List;

public record CollectedChunkDataPayload(
        IntList xPositions,
        IntList zPositions,
        List<ChunkData> chunkData,
        List<LightData> lightData
) implements CustomPayload {
    public static final Id<CollectedChunkDataPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:collected_chunk"));
    public static final PacketCodec<RegistryByteBuf, CollectedChunkDataPayload> CODEC = PacketCodec.ofStatic(
            CollectedChunkDataPayload::encode,
            CollectedChunkDataPayload::decode
    );

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

    private static CollectedChunkDataPayload decode(RegistryByteBuf x) {
        try {
            int packetSize = x.readInt();

            byte[] data = new byte[packetSize];

            x.readBytes(data);

            data = DeflateCompressor.INSTANCE.decompress(data);

            RegistryByteBuf buf = new RegistryByteBuf(new PacketByteBuf(Unpooled.copiedBuffer(data)), x.getRegistryManager());

            int size = buf.readInt();

            IntList xPositions = new IntArrayList();
            IntList zPositions = new IntArrayList();

            for (int i = 0; i < size; i++) {
                xPositions.add(buf.readInt());
            }

            for (int i = 0; i < size; i++) {
                zPositions.add(buf.readInt());
            }

            List<ChunkData> chunkDataList = CollectionFactor.arrayList();

            for (int i = 0; i < size; i++) {
                int chunkX = xPositions.getInt(i);
                int chunkZ = zPositions.getInt(i);

                chunkDataList.add(new ChunkData(buf, chunkX, chunkZ));
            }

            List<LightData> lightDataList = CollectionFactor.arrayList();
            for (int i = 0; i < size; i++) {
                int chunkX = xPositions.getInt(i);
                int chunkZ = zPositions.getInt(i);

                lightDataList.add(new LightData(buf, chunkX, chunkZ));
            }

            return new CollectedChunkDataPayload(xPositions, zPositions, chunkDataList, lightDataList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void encode(RegistryByteBuf x, CollectedChunkDataPayload packet) {
        RegistryByteBuf buf = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), x.getRegistryManager());

        int size = packet.chunkData.size();

        buf.writeInt(size);

        for (int position : packet.xPositions) {
            buf.writeInt(position);
        }
        for (int position : packet.zPositions) {
            buf.writeInt(position);
        }
        for (ChunkData chunkData : packet.chunkData) {
            chunkData.write(buf);
        }
        for (LightData lightData : packet.lightData) {
            lightData.write(buf);
        }

        byte[] bytes = new byte[buf.readableBytes()];

        buf.readBytes(bytes);

        byte[] data = DeflateCompressor.INSTANCE.compress(bytes);
        x.writeInt(data.length);

        x.writeBytes(data);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
