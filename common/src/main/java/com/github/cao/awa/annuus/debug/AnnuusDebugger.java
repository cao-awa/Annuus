package com.github.cao.awa.annuus.debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnnuusDebugger {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusDebugger");
    public static long processedChunks = 0;
    public static long processedChunksBytes = 0;
    public static long processedBlockUpdates = 0;
    public static long vanillaBlockUpdateBytes = 0;
    public static long processedBlockUpdateBytes = 0;
    public static double chunkCalculatedTimes = 0D;
    public static boolean enableDebugs = true;

    public static void debug(String message, Object... params) {
        LOGGER.info("[Debug] " + message, params);
    }
}
