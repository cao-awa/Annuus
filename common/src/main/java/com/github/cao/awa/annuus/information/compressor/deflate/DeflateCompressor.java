package com.github.cao.awa.annuus.information.compressor.deflate;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.InformationCompressors;
import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sinuatum.util.io.IOUtil;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Stable
public class DeflateCompressor implements InformationCompressor {
    public static final DeflateCompressor BEST_INSTANCE = InformationCompressors.register(new DeflateCompressor(Deflater.BEST_COMPRESSION));
    public static final DeflateCompressor DEFLATE_8_INSTANCE = InformationCompressors.register(new DeflateCompressor(8));
    public static final DeflateCompressor DEFLATE_7_INSTANCE = InformationCompressors.register(new DeflateCompressor(7));
    public static final DeflateCompressor DEFLATE_6_INSTANCE = InformationCompressors.register(new DeflateCompressor(6));
    public static final DeflateCompressor DEFLATE_5_INSTANCE = InformationCompressors.register(new DeflateCompressor(5));
    public static final DeflateCompressor DEFLATE_4_INSTANCE = InformationCompressors.register(new DeflateCompressor(4));
    public static final DeflateCompressor DEFLATE_3_INSTANCE = InformationCompressors.register(new DeflateCompressor(3));
    public static final DeflateCompressor DEFLATE_2_INSTANCE = InformationCompressors.register(new DeflateCompressor(2));
    public static final DeflateCompressor FASTEST_INSTANCE = InformationCompressors.register(new DeflateCompressor(Deflater.BEST_SPEED));
    private final int compressLevel;

    public DeflateCompressor(int compressLevel) {
        this.compressLevel = compressLevel;
    }

    @Override
    public int getId() {
        return this.compressLevel;
    }

    /**
     * Compress using 'deflate' with the best compression.
     *
     * @param bytes data source
     * @return compress result
     * @author cao_awa
     * @since 1.0.0
     */
    public byte[] compress(byte[] bytes) {
        return compress(bytes, DeflaterOutputStream::new);
    }

    /**
     * Decompress using 'inflate'.
     *
     * @param bytes data source
     * @return decompress result
     * @author cao_awa
     * @since 1.0.0
     */
    public byte[] decompress(byte[] bytes) {
        return decompress(bytes, InflaterInputStream::new);
    }
}
