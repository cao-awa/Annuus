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
        delegate.writeMap(
                entries.replacements,
                PacketByteBuf::writeString,
                PacketByteBuf::writeInt
        );

        delegate.writeVarInt(entries.propertySetMap.size());

        entries.propertySetMap.forEach((key, property) -> {
            delegate.writeIdentifier(key.getRegistry());
            delegate.writeIdentifier(key.getValue());

            RecipePropertySet.PACKET_CODEC.encode(delegate, property);
        });

        List<CuttingRecipeDisplay.GroupEntry<StonecuttingRecipe>> stoneCuttingEntries = entries.stonecuttingRecipeGrouping.entries();

        delegate.writeVarInt(stoneCuttingEntries.size());

        for (CuttingRecipeDisplay.GroupEntry<StonecuttingRecipe> stoneCuttingEntry : stoneCuttingEntries) {
            CuttingRecipeDisplay.GroupEntry.codec().encode(delegate, (CuttingRecipeDisplay.GroupEntry) stoneCuttingEntry);
        }

    }

    public static AnnuusRecipeEntries decode(RegistryByteBuf delegate) {
        Map<String, Integer> replacements = delegate.readMap(
                PacketByteBuf::readString,
                PacketByteBuf::readInt
        );

        int propertiesSize = delegate.readVarInt();

        Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap = CollectionFactor.hashMap();

        for (int i = 0; i < propertiesSize; i++) {
            Identifier registry = delegate.readIdentifier();
            Identifier value = delegate.readIdentifier();

            propertySetMap.put(
                    RegistryKeyAccessor.ofIdentifier(registry, value),
                    RecipePropertySet.PACKET_CODEC.decode(delegate)
            );
        }

        int stoneCuttingSize = delegate.readVarInt();

        List<CuttingRecipeDisplay.GroupEntry<StonecuttingRecipe>> stoneCuttingRecipes = CollectionFactor.arrayList(stoneCuttingSize);

        for (int i = 0; i < stoneCuttingSize; i++) {
            stoneCuttingRecipes.add((CuttingRecipeDisplay.GroupEntry) CuttingRecipeDisplay.GroupEntry.codec().decode(delegate));
        }

        CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping = new CuttingRecipeDisplay.Grouping<>(stoneCuttingRecipes);

        return new AnnuusRecipeEntries(
                replacements,
                propertySetMap,
                stonecuttingRecipeGrouping
        );
    }

    public Map<RegistryKey<RecipePropertySet>, RecipePropertySet> vanillaPropertySetMap() {
        return this.propertySetMap;
    }

    public CuttingRecipeDisplay.Grouping<StonecuttingRecipe> vanillaStonecuttingRecipeGrouping() {
        return this.stonecuttingRecipeGrouping;
    }
}
