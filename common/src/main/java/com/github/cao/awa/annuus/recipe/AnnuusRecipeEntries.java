package com.github.cao.awa.annuus.recipe;

import com.github.cao.awa.annuus.mixin.registry.RegistryKeyAccessor;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class AnnuusRecipeEntries {
    private final Map<String, Integer> replacements;
    private final Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap;
    private final CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping;

    public AnnuusRecipeEntries(
            Map<String, Integer> replacements,
            Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap,
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping
    ) {
        this.replacements = replacements;
        this.propertySetMap = propertySetMap;
        this.stonecuttingRecipeGrouping = stonecuttingRecipeGrouping;
    }

    public static AnnuusRecipeEntries create(
            Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap,
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping
    ) {
        Map<String, Integer> replacements = CollectionFactor.hashMap();

        return new AnnuusRecipeEntries(replacements, propertySetMap, stonecuttingRecipeGrouping);
    }

    public static void encode(RegistryByteBuf delegate, AnnuusRecipeEntries entries) {
        AnnuusRecipeReplacementV1.encode(entries.replacements, entries.propertySetMap, entries.stonecuttingRecipeGrouping, delegate);
    }

    public static AnnuusRecipeEntries decode(RegistryByteBuf delegate) {
        return AnnuusRecipeReplacementV1.decode(delegate);
    }

    public Map<RegistryKey<RecipePropertySet>, RecipePropertySet> vanillaPropertySetMap() {
        return this.propertySetMap;
    }

    public CuttingRecipeDisplay.Grouping<StonecuttingRecipe> vanillaStonecuttingRecipeGrouping() {
        return this.stonecuttingRecipeGrouping;
    }
}
