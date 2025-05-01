package com.github.cao.awa.annuus.recipe;

import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AnnuusRecipeEntries {
    private final Map<String, Integer> replacements;
    private final AnnuusRecipeEntry[] recipeEntries;

    public AnnuusRecipeEntries(Map<String, Integer> replacements, AnnuusRecipeEntry[] recipeEntries) {
        this.replacements = replacements;
        this.recipeEntries = recipeEntries;
    }

    public static AnnuusRecipeEntries create(RecipeEntry<?>[] entries) {
        AnnuusRecipeEntry[] recipeEntries = new AnnuusRecipeEntry[entries.length];

        Map<String, Integer> replacements = CollectionFactor.hashMap();

        int replacementId = 0;
        for (RecipeEntry<?> entry : entries) {
            Identifier identifier= entry.id();
            if (!replacements.containsKey(identifier.getNamespace())) {
                replacements.put(identifier.getNamespace(), replacementId);
                replacementId++;
            }
        }

        for (int i = 0; i < entries.length; i++) {
            recipeEntries[i] = AnnuusRecipeEntry.create(replacements, entries[i]);
        }

        return new AnnuusRecipeEntries(replacements, recipeEntries);
    }

    public static void encode(RegistryByteBuf delegate, AnnuusRecipeEntries entries) {
        delegate.writeVarInt(entries.replacements.size());

        entries.replacements.forEach((key, id) -> {
            delegate.writeVarInt(id);
            delegate.writeString(key);
        });

        delegate.writeVarInt(entries.recipeEntries.length);

        for (AnnuusRecipeEntry entry : entries.recipeEntries) {
            AnnuusRecipeEntry.encode(delegate, entry);
        }
    }

    public static AnnuusRecipeEntries decode(RegistryByteBuf delegate) {
        int replacementsSize = delegate.readVarInt();

        Map<String, Integer> replacements = CollectionFactor.hashMap();

        for (int i = 0;i < replacementsSize; i++) {
            int replacementId = delegate.readVarInt();
            String replacement = delegate.readString();
            replacements.put(replacement, replacementId);
        }

        int recipesSize = delegate.readVarInt();

        AnnuusRecipeEntry[] recipeEntries = new AnnuusRecipeEntry[recipesSize];

        for (int i = 0;i < recipesSize; i++) {
            recipeEntries[i] = AnnuusRecipeEntry.decode(delegate);
        }

        return new AnnuusRecipeEntries(replacements, recipeEntries);
    }

    public RecipeEntry<?>[] toVanilla() {
        RecipeEntry<?>[] recipes = new RecipeEntry[this.recipeEntries.length];

        Map<Integer, String> reverseReplacements = CollectionFactor.hashMap();

        this.replacements.forEach((key, id) -> reverseReplacements.put(id, key));

        for (int i = 0;i < this.recipeEntries.length;i ++) {
            recipes[i] = this.recipeEntries[i].toVanilla(reverseReplacements);
        }

        return recipes;
    }
}
