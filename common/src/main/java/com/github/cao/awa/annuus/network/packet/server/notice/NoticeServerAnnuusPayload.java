package com.github.cao.awa.annuus.network.packet.server.notice;

import com.github.cao.awa.annuus.Annuus;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

public record NoticeServerAnnuusPayload(int versionId) implements CustomPayload {
    public static final Id<NoticeServerAnnuusPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:notice_annuus_version"));
    public static final PacketCodec<PacketByteBuf, NoticeServerAnnuusPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, NoticeServerAnnuusPayload::versionId,
        NoticeServerAnnuusPayload::new
    );

    public static CustomPayloadC2SPacket createPacket() {
        return new CustomPayloadC2SPacket(createData());
    }

    public static NoticeServerAnnuusPayload createData() {
        return new NoticeServerAnnuusPayload(Annuus.PROTOCOL_VERSION_ID);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
