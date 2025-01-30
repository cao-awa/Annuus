package com.github.cao.awa.annuus.network.packet.client.play.chunk.data;

import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.BitSet;
import java.util.Iterator;

public class CollectedChunkDataPayloadHandler {
    public static void loadChunksFromPayload(CollectedChunkDataPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        client.executeSync(() -> {
            ClientPlayNetworkHandler networkHandler = player.networkHandler;
            ClientWorld world = networkHandler.getWorld();
            Iterator<ChunkData> chunkDataIterator = payload.chunkData().iterator();
            Iterator<LightData> lightDataIterator = payload.lightData().iterator();
            int i = 0;

            while (chunkDataIterator.hasNext()) {
                ChunkData chunkData = chunkDataIterator.next();
                LightData lightData = lightDataIterator.next();
                int x = payload.xPositions().getInt(i);
                int z = payload.zPositions().getInt(i);

                world.getChunkManager().loadChunkFromPacket(x, z, chunkData.getSectionsDataBuf(), chunkData.getHeightmap(), chunkData.getBlockEntities(x, z));
                world.enqueueChunkUpdate(() -> {
                    readLightData(world, x, z, lightData, false);
                    Manipulate.makeNonNull(world.getChunkManager().getWorldChunk(x, z, false), chunk -> {
                        scheduleRenderChunk(world, chunk, x, z);
                        client.worldRenderer.scheduleNeighborUpdates(chunk.getPos());
                    });
                });

                i++;
            }
        });
    }

    private static void scheduleRenderChunk(ClientWorld world, WorldChunk chunk, int x, int z) {
        LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
        ChunkSection[] chunkSections = chunk.getSectionArray();
        ChunkPos chunkPos = chunk.getPos();

        for (int i = 0; i < chunkSections.length; ++i) {
            ChunkSection chunkSection = chunkSections[i];
            int j = world.sectionIndexToCoord(i);
            lightingProvider.setSectionStatus(ChunkSectionPos.from(chunkPos, j), chunkSection.isEmpty());
        }

        world.scheduleChunkRenders(x - 1, world.getBottomSectionCoord(), z - 1, x + 1, world.getTopSectionCoord(), z + 1);
    }

    private static void readLightData(ClientWorld world, int x, int z, LightData data, boolean bl) {
        LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
        updateLighting(
                world,
                x,
                z,
                lightingProvider,
                LightType.SKY,
                data.getInitedSky(),
                data.getUninitedSky(),
                data.getSkyNibbles().iterator(),
                bl
        );
        updateLighting(
                world,
                x,
                z,
                lightingProvider,
                LightType.BLOCK,
                data.getInitedBlock(),
                data.getUninitedBlock(),
                data.getBlockNibbles().iterator(),
                bl
        );
        lightingProvider.setColumnEnabled(new ChunkPos(x, z), true);
    }

    private static void updateLighting(ClientWorld world, int chunkX, int chunkZ, LightingProvider provider, LightType type, BitSet inited, BitSet uninited, Iterator<byte[]> nibbles, boolean bl) {
        for (int i = 0; i < provider.getHeight(); ++i) {
            boolean initedBit = inited.get(i);
            if (initedBit || uninited.get(i)) {
                int j = provider.getBottomY() + i;
                provider.enqueueSectionData(type, ChunkSectionPos.from(chunkX, j, chunkZ), initedBit ? new ChunkNibbleArray(nibbles.next().clone()) : new ChunkNibbleArray());
                if (bl) {
                    world.scheduleBlockRenders(chunkX, j, chunkZ);
                }
            }
        }
    }
}
