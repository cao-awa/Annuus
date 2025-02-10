package com.github.cao.awa.annuus.network.packet.client.play.block.update;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.util.math.BlockPos;

public class CollectedBlockUpdatePayloadHandler {
    public static void updateBlocksFromPayload(CollectedBlockUpdatePayload payload, MinecraftClient client, ClientPlayerEntity player) {
        client.executeSync(() -> {
            assert client.world != null;

            payload.committed().forEach((position, blockState) -> {
                client.world.handleBlockUpdate(BlockPos.fromLong(position), blockState, 19);
            });
        });
    }
}
