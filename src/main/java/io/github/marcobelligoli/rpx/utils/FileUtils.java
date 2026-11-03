package io.github.marcobelligoli.rpx.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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
        try {
            return readLines(filePath, inputEncoding);
        } catch (IOException e) {
            LogUtils.error(logger, e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<String> readLines(String filePath, Charset inputEncoding) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = getFileReader(filePath, inputEncoding)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
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

        Path destinationPath = resolveInsideFolder(destinationFolder, newFileName);
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        LogUtils.info(logger, String.format("File copied and renamed successfully: %s", destinationPath));
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

        Path destinationPath = resolveInsideFolder(destinationDirectory, sourceFile.getName());
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
        LogUtils.info(logger, String.format("File copied successfully: %s", destinationPath));
    }

    /**
     * Resolves a file name against a folder, refusing any name that would place the file anywhere but directly
     * inside that folder (e.g. names containing path separators or "..")
     *
     * @param folder   Folder the file must be created in
     * @param fileName Name of the file
     * @return Normalized destination path, a direct child of the folder
     * @throws IOException if the name escapes the folder
     */
    static Path resolveInsideFolder(File folder, String fileName) throws IOException {
        Path folderPath = folder.toPath().toAbsolutePath().normalize();
        Path destinationPath = folderPath.resolve(fileName).normalize();
        if (!folderPath.equals(destinationPath.getParent())) {
            throw new IOException(String.format("File name [%s] would be written outside the folder [%s]",
                    fileName, folderPath));
        }
        return destinationPath;
    }

    /**
     * Checks if a path points to a network location (UNC path such as \\host\share or //host/share).
     * Opening such a path can make the operating system connect, and authenticate, to the remote host.
     *
     * @param path Path to check
     * @return True if the path is a network path
     */
    public static boolean isNetworkPath(String path) {
        String trimmedPath = path.trim();
        return trimmedPath.startsWith("\\\\") || trimmedPath.startsWith("//")
                || trimmedPath.startsWith("\\/") || trimmedPath.startsWith("/\\");
    }

    /**
     * Changes file encoding to UTF-8. The file is left untouched when it is already UTF-8 or cannot be read, and it
     * is replaced atomically, so that a failure never leaves it truncated.
     *
     * @param filePath file to change encoding
     */
    public static void changeFileEncoding(String filePath) {
        Charset inputCharset = toCharset(getFileEncoding(new File(filePath)));
        if (StandardCharsets.UTF_8.equals(inputCharset)) {
            return;
        }

        List<String> lines;
        try {
            lines = readLines(filePath, inputCharset);
        } catch (IOException e) {
            LogUtils.error(logger, String.format("Impossible to read [%s], its encoding is not changed: %s",
                    filePath, e.getMessage()));
            return;
        }

        Path targetPath = Paths.get(filePath).toAbsolutePath();
        Path tempPath = null;
        try {
            // the temporary file is a sibling of the target, so that the final move stays on the same file system
            tempPath = Files.createTempFile(targetPath.getParent(), targetPath.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            replaceFile(tempPath, targetPath);
        } catch (IOException e) {
            LogUtils.error(logger, String.format("Impossible to change the encoding of [%s]: %s",
                    filePath, e.getMessage()));
        } finally {
            deleteQuietly(tempPath);
        }
    }

    private static void replaceFile(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LogUtils.error(logger, String.format("Impossible to delete temporary file [%s]", path));
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
