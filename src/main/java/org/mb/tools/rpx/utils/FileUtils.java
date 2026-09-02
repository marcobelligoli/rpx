package org.mb.tools.rpx.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Files utilities class
 */
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Reads all text file lines
     *
     * @param filePath      Path of file to read
     * @param inputEncoding Input file encoding (UTF-8 is used when null)
     * @return List of read lines
     */
    public static List<String> readLinesFromFile(String filePath, Charset inputEncoding) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = getFileReader(filePath, inputEncoding)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            LogUtils.error(logger, e.getMessage());
        }

        return lines;
    }

    /**
     * Creates a folder (if not exist)
     *
     * @param folderPath Directory to create
     * @return Directory created (null if any errors occurred)
     */
    public static File createFolderIfNotExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                LogUtils.info(logger, String.format("Folder successfully created: %s", folderPath));
                return folder;
            } else {
                LogUtils.error(logger, String.format("Impossible to create folder: %s", folderPath));
                return null;
            }
        } else {
            LogUtils.info(logger, String.format("Folder already exist: %s", folderPath));
            return folder;
        }
    }

    /**
     * Copies a file changing the name
     *
     * @param sourceFile        File to copy
     * @param destinationFolder Destination folder
     * @param newFileName       New filename to set
     */
    public static void copyAndRenameFile(File sourceFile, File destinationFolder, String newFileName) throws IOException {
        if (!sourceFile.exists()) {
            LogUtils.error(logger, String.format("Source file [%s] not exist.", sourceFile));
            return;
        }

        File newFile = new File(destinationFolder, newFileName);
        Path sourcePath = sourceFile.toPath();
        Path destinationPath = newFile.toPath();
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        LogUtils.info(logger, String.format("File copied and renamed successfully: %s", newFile.getAbsolutePath()));
    }

    /**
     * Copies a file
     *
     * @param sourceFile           File to copy
     * @param destinationDirectory Destination folder
     */
    public static void copyFile(File sourceFile, File destinationDirectory) throws IOException {
        if (!sourceFile.exists()) {
            LogUtils.error(logger, String.format("Source file [%s] not exist.", sourceFile));
            return;
        }

        File destinationFile = new File(destinationDirectory, sourceFile.getName());

        Path sourcePath = sourceFile.toPath();
        Path destinationPath = destinationFile.toPath();
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        LogUtils.info(logger, String.format("File copied successfully: %s", destinationFile.getAbsolutePath()));
    }

    /**
     * Changes file encoding to UTF-8
     *
     * @param filePath file to change encoding
     */
    public static void changeFileEncoding(String filePath) {
        Charset inputCharset = toCharset(getFileEncoding(new File(filePath)));
        List<String> lines = FileUtils.readLinesFromFile(filePath, inputCharset);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {

            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            LogUtils.error(logger, e.getMessage());
        }
    }

    /**
     * Gets file encoding
     *
     * @param file File to analyze
     * @return Encoding found (null if error occurs)
     */
    public static String getFileEncoding(File file) {
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            byte[] buffer = new byte[4096];
            UniversalDetector detector = new UniversalDetector(null);

            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1 && !detector.isDone()) {
                detector.handleData(buffer, 0, bytesRead);
            }

            detector.dataEnd();
            String encoding = detector.getDetectedCharset();
            detector.reset();

            return encoding;
        } catch (IOException e) {
            LogUtils.error(logger, e.getMessage());
            return null;
        }
    }

    /**
     * Converts a detected encoding name into a Charset
     *
     * @param encoding Encoding name (as detected on the file), may be null
     * @return Matching Charset, UTF-8 if the name is null or not supported on this platform
     */
    public static Charset toCharset(String encoding) {
        if (encoding == null) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (IllegalArgumentException e) {
            LogUtils.error(logger, String.format("Encoding [%s] is not supported, falling back to UTF-8", encoding));
            return StandardCharsets.UTF_8;
        }
    }

    private static BufferedReader getFileReader(String filePath, Charset encoding) throws IOException {
        // never fall back to the platform default charset: it would make the same playlist
        // decode differently on Windows, macOS and Linux
        return new BufferedReader(new FileReader(filePath, encoding != null ? encoding : StandardCharsets.UTF_8));
    }
}
