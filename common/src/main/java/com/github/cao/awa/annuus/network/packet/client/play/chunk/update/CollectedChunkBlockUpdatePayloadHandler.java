package com.github.cao.awa.annuus.network.packet.client.play.chunk.update;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

public class CollectedChunkBlockUpdatePayloadHandler {
    public static void updateBlocksFromPayload(CollectedChunkBlockUpdatePayload payload, MinecraftClient client, ClientPlayerEntity player) {
        client.executeSync(() -> {
            assert client.world != null;

            BlockPos.Mutable mutable = new BlockPos.Mutable();

            payload.details().forEach((chunkSectionPosValue, details) -> {
                ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkSectionPosValue);

                short[] positions = details.positions();
                int[] states = details.states();
                int updates = positions.length;

                for (int i = 0; i < updates; i++) {
                    short s = positions[i];
                    mutable.set(chunkSectionPos.unpackBlockX(s), chunkSectionPos.unpackBlockY(s), chunkSectionPos.unpackBlockZ(s));
                    client.world.handleBlockUpdate(mutable, Block.getStateFromRawId(states[i]), 19);
                }
            });
        });
    }
}
