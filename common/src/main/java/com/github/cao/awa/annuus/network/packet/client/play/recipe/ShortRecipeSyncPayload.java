package com.github.cao.awa.annuus.network.packet.client.play.recipe;

import com.github.cao.awa.annuus.Annuus;
import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.deflate.DeflateCompressor;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.*;

public record ShortRecipeSyncPayload(
        RecipeEntry<?>[] recipes
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

    public static CustomPayloadS2CPacket createPacket(RecipeEntry<?>[] recipes) {
        return new CustomPayloadS2CPacket(createData(recipes));
    }

    public static ShortRecipeSyncPayload createData(RecipeEntry<?>[] recipes) {
        return new ShortRecipeSyncPayload(recipes);
    }

    // TODO
    private static ShortRecipeSyncPayload decode(RegistryByteBuf buf) {
        try {
            RegistryByteBuf delegate = AnnuusCompressUtil.doDecompressRegistryBuf(buf);

            int recipeCount = delegate.readVarInt();

            RecipeEntry<?>[] recipes = new RecipeEntry[recipeCount];

            for (int i = 0; i < recipeCount; i++) {
                recipes[i] = RecipeEntry.PACKET_CODEC.decode(delegate);
            }

            return createData(recipes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // TODO
    private static void encode(RegistryByteBuf buf, ShortRecipeSyncPayload packet) {
        RegistryByteBuf delegate = new RegistryByteBuf(new PacketByteBuf(Unpooled.buffer()), buf.getRegistryManager());

        int size = packet.recipes.length;

        delegate.writeVarInt(size);

        for (RecipeEntry<?> entry : packet.recipes) {
            RecipeEntry.PACKET_CODEC.encode(delegate, entry);
        }

        delegate.writeVarInt(size);

        AnnuusCompressUtil.doCompress(buf, delegate, () -> currentCompressor);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
