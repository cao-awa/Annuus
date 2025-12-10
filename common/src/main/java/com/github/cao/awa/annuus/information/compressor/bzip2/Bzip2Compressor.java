package com.github.cao.awa.annuus.information.compressor.bzip2;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.annuus.information.compressor.InformationCompressors;
import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sinuatum.util.io.IOUtil;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.InflaterInputStream;

@Stable
public class Bzip2Compressor implements InformationCompressor {
    public static final Bzip2Compressor INSTANCE = InformationCompressors.register(new Bzip2Compressor());

    @Override
    public int getId() {
        return 12;
    }

    /**
     * Compress using Bzip2.
     *
     * @param bytes data source
     * @return compress result
     * @author cao_awa
     * @since 1.0.0
     */
    public byte[] compress(byte[] bytes) {
        return compress(bytes, BZip2CompressorOutputStream::new);
    }

    /**
     * Decompress using Bzip2.
     *
     * @param bytes data source
     * @return decompress result
     * @author cao_awa
     * @since 1.0.0
     */
    public byte[] decompress(byte[] bytes) {
        return decompress(bytes, BZip2CompressorInputStream::new);
    }
}
