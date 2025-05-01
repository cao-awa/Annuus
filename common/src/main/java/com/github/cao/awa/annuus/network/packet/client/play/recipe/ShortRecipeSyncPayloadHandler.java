package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;

public class ShortRecipeSyncPayloadHandler {
    public static void syncRecipesFromPayload(ShortRecipeSyncPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        client.executeSync(() -> {
            // Sync recipes by vanilla.
            player.networkHandler.onSynchronizeRecipes(
                    new SynchronizeRecipesS2CPacket(
                            CollectionFactor.arrayList(payload.recipes())
                    )
            );
        });
    }
}
