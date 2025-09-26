package com.github.cao.awa.annuus.recipe;

import com.github.cao.awa.annuus.mixin.registry.RegistryKeyAccessor;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class AnnuusRecipeReplacementV1 {
    public static void encode(Map<String, Integer> replacements, Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap, CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping, RegistryByteBuf delegate) {
        delegate.writeMap(
                replacements,
                PacketByteBuf::writeString,
                PacketByteBuf::writeInt
        );

        delegate.writeVarInt(propertySetMap.size());

        propertySetMap.forEach((key, property) -> {
            delegate.writeIdentifier(key.getRegistry());
            delegate.writeIdentifier(key.getValue());

            RecipePropertySet.PACKET_CODEC.encode(delegate, property);
        });

        List<CuttingRecipeDisplay.GroupEntry<StonecuttingRecipe>> stoneCuttingEntries = stonecuttingRecipeGrouping.entries();

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
}
