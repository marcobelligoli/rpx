package org.mb.tools.rekordboxplaylistexporter.utils;

import java.util.logging.Logger;

public class LogUtils {

    private LogUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void error(Logger logger, String message) {
        logger.severe(message);
    }

    public static void info(Logger logger, String message) {
        logger.info(message);
    }
}
