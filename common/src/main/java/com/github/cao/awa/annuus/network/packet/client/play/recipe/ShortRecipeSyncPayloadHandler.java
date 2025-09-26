package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ShortRecipeSyncPayloadHandler {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusShortRecipeSyncPayloadHandler");

    public static void syncRecipesFromPayload(ShortRecipeSyncPayload payload, MinecraftClient client, ClientPlayerEntity player) {
        // Convert the short recipe to the vanilla recipe.
        Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap = payload.recipes().vanillaPropertySetMap();
        CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping = payload.recipes().vanillaStonecuttingRecipeGrouping();

        LOGGER.info("Synchronized {} recipes, {} stonecutting recipes", propertySetMap.size(), stonecuttingRecipeGrouping.size());

        // Sync recipes by vanilla.
        player.networkHandler.onSynchronizeRecipes(
                new SynchronizeRecipesS2CPacket(
                        propertySetMap,
                        stonecuttingRecipeGrouping
                )
        );
    }
}
