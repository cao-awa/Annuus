package com.github.cao.awa.annuus.network.packet.server.notice;

import com.github.cao.awa.annuus.network.packet.client.play.chunk.data.CollectedChunkDataPayload;
import com.github.cao.awa.annuus.version.AnnuusVersionStorage;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.BitSet;
import java.util.Iterator;

public class NoticeServerAnnuusPayloadHandler {
    public static void noticeAnnuusFromPayload(NoticeServerAnnuusPayload payload, MinecraftServer client, ServerConfigurationNetworkHandler networkHandler) {
        ((AnnuusVersionStorage) networkHandler).setAnnuusVersion(payload.versionId());
    }
}
