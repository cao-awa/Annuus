package com.github.cao.awa.annuus.recipe;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AnnuusRecipeEntry {
    private final int namespace;
    private final String path;
    private final Recipe<?> recipe;

    public AnnuusRecipeEntry(int namespace, String path, Recipe<?> recipe) {
        this.namespace = namespace;
        this.path = path;
        this.recipe = recipe;
    }

    public static AnnuusRecipeEntry create(Map<String, Integer> replacements, RecipeEntry<? extends Recipe<?>> recipeEntry) {
        return new AnnuusRecipeEntry(
                replacements.get(recipeEntry.id().getNamespace()),
                recipeEntry.id().getPath(),
                recipeEntry.value()
        );
    }

    public static void encode(RegistryByteBuf delegate, AnnuusRecipeEntry entry) {
        delegate.writeVarInt(entry.namespace);
        delegate.writeString(entry.path);
        Recipe.PACKET_CODEC.encode(delegate, entry.recipe);
    }

    public static AnnuusRecipeEntry decode(RegistryByteBuf delegate) {
        int namespace = delegate.readVarInt();
        String path = delegate.readString();
        Recipe<?> recipe = Recipe.PACKET_CODEC.decode(delegate);

        return new AnnuusRecipeEntry(namespace, path, recipe);
    }

    public RecipeEntry<?> toVanilla(Map<Integer, String> replacements) {
        return new RecipeEntry<Recipe<?>>(
                Identifier.of(
                        replacements.get(this.namespace),
                        this.path
                ),
                this.recipe
        );
    }
}