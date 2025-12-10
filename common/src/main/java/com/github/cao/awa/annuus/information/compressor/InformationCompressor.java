package com.github.cao.awa.annuus.information.compressor;

import com.github.cao.awa.apricot.annotations.Stable;
import com.github.cao.awa.sinuatum.function.exception.function.ExceptingFunction;
import com.github.cao.awa.sinuatum.util.io.IOUtil;

import java.io.*;

/**
 * Information compressor.
 *
 * @author cao_awa
 * @since 1.0.0
 */
@Stable
public interface InformationCompressor {
    byte[] EMPTY_BYTES = new byte[0];

    int getId();

    /**
     * Compress for a data.
     *
     * @param bytes data source
     * @return compress result
     * @author cao_awa
     * @since 1.0.0
     */
    byte[] compress(byte[] bytes);

    /**
     * Decompress for a data.
     *
     * @param bytes data source
     * @return decompress result
     * @author cao_awa
     * @since 1.0.0
     */
    byte[] decompress(byte[] bytes);

    default byte[] compress(byte[] bytes, ExceptingFunction<ByteArrayOutputStream, OutputStream, IOException> outputStreamSupplier) {
        if (bytes.length == 0) {
            return EMPTY_BYTES;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            IOUtil.write(
                    outputStreamSupplier.apply(out),
                    bytes
            );
            return out.toByteArray();
        } catch (Exception e) {
            return bytes;
        }
    }

    default byte[] decompress(byte[] bytes, ExceptingFunction<ByteArrayInputStream, InputStream, IOException> inputSupplier) {
        if (bytes.length == 0) {
            return EMPTY_BYTES;
        }
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            IOUtil.write(
                    result,
                    inputSupplier.apply(new ByteArrayInputStream(bytes))
            );
            return result.toByteArray();
        } catch (Exception ex) {
            return bytes;
        }
    }
}
