package com.github.cao.awa.annuus.information.compressor;

import com.github.cao.awa.apricot.annotations.Stable;

/**
 * Information compressor.
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
@Stable
public interface InformationCompressor {
    byte[] EMPTY_BYTES = new byte[0];

    /**
     * Compress for a data.
     *
     * @param bytes data source
     *
     * @return compress result
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    byte[] compress(byte[] bytes);

    /**
     * Decompress for a data.
     *
     * @param bytes data source
     *
     * @return decompress result
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    byte[] decompress(byte[] bytes);
}
