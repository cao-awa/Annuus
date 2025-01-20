package com.github.cao.awa.annuus.information.compressor.deflate;

import com.github.cao.awa.annuus.information.compressor.InformationCompressor;
import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sinuatum.util.io.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Stable
public class DeflateCompressor implements InformationCompressor {
    public static final DeflateCompressor BEST_INSTANCE = new DeflateCompressor(true);
    public static final DeflateCompressor FASTEST_INSTANCE = new DeflateCompressor(false);
    private final boolean isBestSpeed;

    public DeflateCompressor(boolean isBestSpeed) {
        this.isBestSpeed = isBestSpeed;
    }

    /**
     * Compress using 'deflate' with the best compression.
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
        if (bytes.length == 0) {
            return EMPTY_BYTES;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IOUtil.write(
                    new DeflaterOutputStream(
                            out,
                            new Deflater(this.isBestSpeed ? Deflater.BEST_SPEED : Deflater.BEST_COMPRESSION)
                    ),
                    bytes
            );
            return out.toByteArray();
        } catch (Exception e) {
            return bytes;
        }
    }

    /**
     * Decompress using 'inflate'.
     *
     * @param bytes data source
     *
     * @return decompress result
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public byte[] decompress(byte[] bytes) {
        if (bytes.length == 0) {
            return EMPTY_BYTES;
        }
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(bytes));
            IOUtil.write(
                    result,
                    inflater
            );
            return result.toByteArray();
        } catch (Exception ex) {
            return bytes;
        }
    }
}
