package com.github.cao.awa.annuus.mixin.server.network.encoding;

import net.minecraft.network.encoding.VarInts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VarInts.class)
public class VarIntsMixin {
    /**
//     * @author cao_awa
//     * @reason Replace to more performances encoding
//     */
//    @Overwrite
//    public static int getSizeInBytes(int i) {
//        return Annuus32BitsInt.writeZigZagVarInt(Unpooled.buffer(), i).readableBytes();
//    }
//
//    /**
//     * @author cao_awa
//     * @reason Replace to more performances decoding
//     */
//    @Overwrite
//    public static int read(ByteBuf buf) {
//        return Annuus32BitsInt.readZigZagVarInt(buf);
//    }
//
//    /**
//     * @author cao_awa
//     * @reason Replace to more performances encoding
//     */
//    @Overwrite
//    public static ByteBuf write(ByteBuf buf, int i) {
//        if (AnnuusDebugger.enableDebugs) {
//            AnnuusDebugger.countVarIntSaves(i);
//        }
//
//        Annuus32BitsInt.writeZigZagVarInt(buf, i);
//
//        return buf;
//    }
}
