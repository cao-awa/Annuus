package com.github.cao.awa.annuus.information.compressor.lz4;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.InformationCompressors;
import com.github.cao.awa.apricot.annotations.Stable;
import net.jpountz.lz4.LZ4Factory;

@Stable
public class Lz4Compressor implements InformationCompressor {
    public static final Lz4Compressor INSTANCE = InformationCompressors.register(new Lz4Compressor());

    @Override
    public int getId() {
        return 10;
    }

    /**
     * Compress using lz4 with the fastest compression.
     *
     * @param bytes data source
     *
     * @return compress result
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public byte[] compress(byte[] bytes) {
        return LZ4Factory.fastestJavaInstance()
                         .fastCompressor()
                         .compress(bytes);
    }

    /**
     * Decompress using lz4.
     *
     * @param bytes data source
     *
     * @author cao_awa
     *
     * @return decompress result
     */
    public byte[] decompress(byte[] bytes) {
        return LZ4Factory.fastestJavaInstance()
                         .fastDecompressor()
                         .decompress(
                                 bytes,
                                 bytes.length
                         );
    }
}