package io.github.marcobelligoli.rpx.service.export;

import io.github.marcobelligoli.rpx.exception.RPXException;

import java.util.List;
import java.util.Locale;

/**
 * Creates the Export Service able to read a given playlist format
 */
public class ExportServiceFactory {

    private static final String TXT_FORMAT = "txt";
    private static final String M3U8_FORMAT = "m3u8";

    private static final List<String> SUPPORTED_FORMATS = List.of(TXT_FORMAT, M3U8_FORMAT);

    private ExportServiceFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the playlist formats the application can read
     *
     * @return Supported file extensions, without the dot
     */
    public static List<String> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    /**
     * Gets the Export Service able to read the given playlist format
     *
     * @param inputFormat Playlist format, i.e. the file extension without the dot
     * @return Export Service for that format
     */
    public static ExportService getExportService(String inputFormat) {
        switch (inputFormat.toLowerCase(Locale.ROOT)) {
            case TXT_FORMAT:
                return new ExportServiceTxtImpl();
            case M3U8_FORMAT:
                return new ExportServiceM3u8Impl();
            default:
                throw new RPXException(String.format("Playlist format [%s] is not supported. Supported formats: %s",
                        inputFormat, String.join(", ", SUPPORTED_FORMATS)));
        }
    }

    /**
     * Gets the format of a playlist file from its extension
     *
     * @param playlistFilePath Path of the playlist file
     * @return File extension without the dot, empty when the file has none
     */
    public static String getInputFormat(String playlistFilePath) {
        String normalizedPath = playlistFilePath.replace('\\', '/');
        String fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex < 0 ? "" : fileName.substring(extensionIndex + 1);
    }
}
