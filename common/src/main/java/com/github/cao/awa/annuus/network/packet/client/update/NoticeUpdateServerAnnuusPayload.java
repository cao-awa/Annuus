package com.github.cao.awa.annuus.network.packet.client.update;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public record NoticeUpdateServerAnnuusPayload(boolean needUpdate) implements CustomPayload {
    public static final Id<NoticeUpdateServerAnnuusPayload> IDENTIFIER = new Id<>(Identifier.of("annuus:notice_update_annuus_version"));
    public static final PacketCodec<PacketByteBuf, NoticeUpdateServerAnnuusPayload> CODEC = PacketCodec.ofStatic(
        NoticeUpdateServerAnnuusPayload::encode,
        NoticeUpdateServerAnnuusPayload::decode
    );

    public static CustomPayloadS2CPacket createPacket() {
        return new CustomPayloadS2CPacket(createData());
    }

    public static NoticeUpdateServerAnnuusPayload createData() {
        return new NoticeUpdateServerAnnuusPayload(true);
    }

    public static NoticeUpdateServerAnnuusPayload decode(PacketByteBuf buf) {
        return new NoticeUpdateServerAnnuusPayload(buf.readBoolean());
    }

    public static void encode(PacketByteBuf buf, NoticeUpdateServerAnnuusPayload packet) {
        buf.writeBoolean(true);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return IDENTIFIER;
    }
}
