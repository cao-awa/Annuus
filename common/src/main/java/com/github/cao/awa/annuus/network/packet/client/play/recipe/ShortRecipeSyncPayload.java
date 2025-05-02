package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
import com.github.cao.awa.annuus.recipe.AnnuusRecipeEntries;
import com.github.cao.awa.annuus.update.ChunkBlockUpdateDetails;
import com.github.cao.awa.annuus.util.compress.AnnuusCompressUtil;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.*;

public record ShortRecipeSyncPayload(
        AnnuusRecipeEntries recipes
) implements CustomPayload {
    public static final Id<ShortRecipeSyncPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:short_recipe_sync"));
    public static final PacketCodec<RegistryByteBuf, ShortRecipeSyncPayload> CODEC = PacketCodec.ofStatic(
            ShortRecipeSyncPayload::encode,
            ShortRecipeSyncPayload::decode
    );
    private static InformationCompressor currentCompressor = DeflateCompressor.BEST_INSTANCE;

    public static void setCurrentCompressor(InformationCompressor compressor) {
        currentCompressor = compressor;
    }

    public static CustomPayloadS2CPacket createPacket(
            Map<RegistryKey<RecipePropertySet>, RecipePropertySet> propertySetMap,
            CuttingRecipeDisplay.Grouping<StonecuttingRecipe> stonecuttingRecipeGrouping
    ) {
        return new CustomPayloadS2CPacket(createData(propertySetMap, stonecuttingRecipeGrouping));
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

    // TODO
    private static ShortRecipeSyncPayload decode(RegistryByteBuf buf) {
        try {
            RegistryByteBuf delegate = AnnuusCompressUtil.doDecompressRegistryBuf(buf);

            AnnuusRecipeEntries recipes = AnnuusRecipeEntries.decode(
                    delegate
            );

            return createData(recipes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // TODO
    private static void encode(RegistryByteBuf buf, ShortRecipeSyncPayload packet) {
        RegistryByteBuf delegate = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), buf.getRegistryManager());

        AnnuusRecipeEntries.encode(
                delegate,
                packet.recipes
        );

        System.out.println("Delegate size: " + delegate.readableBytes() + " by " + currentCompressor);

        AnnuusCompressUtil.doCompress(buf, delegate, () -> currentCompressor);

        System.out.println("Short recipes size: " + buf.readableBytes() + " bytes");
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
