package org.mb.tools.rpx.utils;

import java.util.logging.Logger;

/**
 * Log Utils class
 */
public class LogUtils {

    private LogUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Logs error
     *
     * @param logger  Logger instance
     * @param message Message to log
     */
    public static void error(Logger logger, String message) {
        logger.severe(message);
    }

    /**
     * Logs info
     *
     * @param logger  Logger instance
     * @param message Message to log
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
    }
}
