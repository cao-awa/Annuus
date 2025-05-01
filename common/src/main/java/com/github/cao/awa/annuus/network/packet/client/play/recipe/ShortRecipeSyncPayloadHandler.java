package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipeEntry;

import java.util.Collection;

public class ShortRecipeSyncPayloadHandler {
    public static void syncRecipesFromPayload(ShortRecipeSyncPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        client.executeSync(() -> {
            // Convert the short recipe to the vanilla recipe.
            Collection<RecipeEntry<?>> recipes = CollectionFactor.arrayList(payload.recipes().toVanilla());

            // Sync recipes by vanilla.
            player.networkHandler.onSynchronizeRecipes(
                    new SynchronizeRecipesS2CPacket(
                            recipes
                    )
            );
        });
    }
}
