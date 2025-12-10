package com.github.cao.awa.annuus.information.compressor.lzma;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.InformationCompressors;
import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sinuatum.util.io.IOUtil;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Stable
public class LZMACompressor implements InformationCompressor {
    public static final LZMACompressor INSTANCE = InformationCompressors.register(new LZMACompressor());

    @Override
    public int getId() {
        return 11;
    }

    /**
     * Compress using LZMA.
     *
     * @param bytes data source
     * @return compress result
     * @author cao_awa
     * @since 1.0.0
     */
    public byte[] compress(byte[] bytes) {
        return compress(bytes, LZMACompressorOutputStream::new);
    }

    /**
     * Decompress using LZMA.
     *
     * @param bytes data source
     * @return decompress result
     * @author cao_awa
     * @since 1.0.0
     */
    public byte[] decompress(byte[] bytes) {
        return decompress(bytes, LZMACompressorInputStream::new);
    }
}
