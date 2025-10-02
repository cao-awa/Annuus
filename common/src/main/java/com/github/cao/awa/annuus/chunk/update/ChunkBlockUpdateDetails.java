package com.github.cao.awa.annuus.chunk.update;

public record ChunkBlockUpdateDetails(
        short[] positions,
        int[] states
) {
}
