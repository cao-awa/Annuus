package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.recipe.AnnuusRecipeEntries;
import com.github.cao.awa.annuus.util.compress.AnnuusCompressUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.TestOnly;

import java.util.*;

public record ShortRecipeSyncPayload(
        AnnuusRecipeEntries recipes
) implements CustomPayload {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusShortRecipeSyncPayload");
    public static final Id<ShortRecipeSyncPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:short_recipe_sync"));
    public static final PacketCodec<RegistryByteBuf, ShortRecipeSyncPayload> CODEC = PacketCodec.ofStatic(
            ShortRecipeSyncPayload::encode,
            ShortRecipeSyncPayload::decode
    );
    private static InformationCompressor currentCompressor = DeflateCompressor.BEST_INSTANCE;

    public static void setCurrentCompressor(InformationCompressor compressor) {
        currentCompressor = compressor;
    }

    public static InformationCompressor getCurrentCompressor() {
        return currentCompressor;
    }

    public static CustomPayloadS2CPacket createPacket(
            Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap,
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping
    ) {
        return new CustomPayloadS2CPacket(createData(propertySetMap, stonecuttingRecipeGrouping));
    }

    public static CustomPayloadS2CPacket createPacket(
            ShortRecipeSyncPayload payload
    ) {
        return new CustomPayloadS2CPacket(payload);
    }

    public static ShortRecipeSyncPayload createData(
            Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap,
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping
    ) {
        return new ShortRecipeSyncPayload(AnnuusRecipeEntries.create(propertySetMap, stonecuttingRecipeGrouping));
    }

    public static ShortRecipeSyncPayload createData(AnnuusRecipeEntries recipes) {
        return new ShortRecipeSyncPayload(recipes);
    }

    public static ShortRecipeSyncPayload decode(RegistryByteBuf buf) {
        try {
            RegistryByteBuf delegate = AnnuusCompressUtil.decompressRegistryBuf(buf);

            AnnuusRecipeEntries recipes = AnnuusRecipeEntries.decode(
                    delegate
            );

            return createData(recipes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void encode(RegistryByteBuf buf, ShortRecipeSyncPayload packet) {
        RegistryByteBuf delegate = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), buf.getRegistryManager());

        AnnuusRecipeEntries.encode(
                delegate,
                packet.recipes
        );

        AnnuusCompressUtil.compress(buf, delegate, () -> currentCompressor);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
