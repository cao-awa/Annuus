package com.github.cao.awa.annuus.network.packet.server.notice;

import com.github.cao.awa.annuus.Annuus;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

public record NoticeServerAnnuusPayload(int versionId) implements CustomPayload {
    public static final Id<NoticeServerAnnuusPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:notice_annuus"));
    public static final PacketCodec<PacketByteBuf, NoticeServerAnnuusPayload> CODEC = PacketCodec.ofStatic(
            NoticeServerAnnuusPayload::encode,
            NoticeServerAnnuusPayload::decode
    );

    public static CustomPayloadC2SPacket createPacket() {
        return new CustomPayloadC2SPacket(createData());
    }

    public static NoticeServerAnnuusPayload createData() {
        return new NoticeServerAnnuusPayload(Annuus.VERSION_ID);
    }

    private static NoticeServerAnnuusPayload decode(PacketByteBuf buf) {
       return new NoticeServerAnnuusPayload(buf.readInt());
    }

    private static void encode(PacketByteBuf buf, NoticeServerAnnuusPayload packet) {
        buf.writeInt(Annuus.VERSION_ID);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
