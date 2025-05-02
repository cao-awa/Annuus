package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;

import java.util.Map;

public class ShortRecipeSyncPayloadHandler {
    public static void syncRecipesFromPayload(ShortRecipeSyncPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        client.executeSync(() -> {
            // Convert the short recipe to the vanilla recipe.
            Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap = payload.recipes().vanillaPropertySetMap();
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping = payload.recipes().vanillaStonecuttingRecipeGrouping();

            // Sync recipes by vanilla.
            player.networkHandler.onSynchronizeRecipes(
                    new SynchronizeRecipesS2CPacket(
                            propertySetMap,
                            stonecuttingRecipeGrouping
                    )
            );
        });
    }
}
