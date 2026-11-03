package io.github.marcobelligoli.rpx.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    private File tempFile;
    private File tempDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("testFile", ".txt");
        tempDirectory = Files.createTempDirectory("testDir").toFile();
        Files.writeString(tempFile.toPath(), "test content");
    }

    @AfterEach
    public void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
        if (tempDirectory.exists()) {
            tempDirectory.delete();
        }
    }

    @Test
    void testReadLinesFromFile() {
        List<String> lines = FileUtils.readLinesFromFile(tempFile.getAbsolutePath(), StandardCharsets.UTF_8);
        assertNotNull(lines);
        assertEquals(1, lines.size());
        assertEquals("test content", lines.get(0));
    }

    @Test
    void testCreateFolderIfNotExists() {
        File newFolder = new File(tempDirectory, "newFolder");
        assertFalse(newFolder.exists());

        File createdFolder = FileUtils.createFolderIfNotExists(newFolder.getAbsolutePath());
        assertNotNull(createdFolder);
        assertTrue(createdFolder.exists());
        assertTrue(createdFolder.isDirectory());
    }

    @Test
    void testCreateFolderIfNotExistsAlreadyExists() {
        File existingFolder = FileUtils.createFolderIfNotExists(tempDirectory.getAbsolutePath());
        assertNotNull(existingFolder);
        assertTrue(existingFolder.exists());
    }

    @Test
    void testCopyAndRenameFile() throws IOException {
        File newFile = new File(tempDirectory, "renamedFile.txt");
        assertFalse(newFile.exists());

        FileUtils.copyAndRenameFile(tempFile, tempDirectory, "renamedFile.txt");
        assertTrue(newFile.exists());

        List<String> lines = Files.readAllLines(newFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("test content", lines.get(0));
    }

    @Test
    void testCopyFile() throws IOException {
        File destinationFile = new File(tempDirectory, tempFile.getName());
        assertFalse(destinationFile.exists());

        FileUtils.copyFile(tempFile, tempDirectory);
        assertTrue(destinationFile.exists());

        List<String> lines = Files.readAllLines(destinationFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("test content", lines.get(0));
    }

    @Test
    void testCopyAndRenameFileRefusesNameEscapingTheFolder() throws IOException {
        File folder = new File(tempDirectory, "playlist");
        assertTrue(folder.mkdir());
        File escapedFile = new File(tempDirectory, "escaped.txt");

        try {
            assertThrows(IOException.class, () -> FileUtils.copyAndRenameFile(tempFile, folder, "../escaped.txt"));
            assertThrows(IOException.class, () -> FileUtils.copyAndRenameFile(tempFile, folder, "sub/escaped.txt"));
            assertFalse(escapedFile.exists());
        } finally {
            Files.deleteIfExists(escapedFile.toPath());
            Files.deleteIfExists(folder.toPath());
        }
    }

    @Test
    void testIsNetworkPath() {
        assertTrue(FileUtils.isNetworkPath("\\\\host\\share\\song.mp3"));
        assertTrue(FileUtils.isNetworkPath("//host/share/song.mp3"));
        assertTrue(FileUtils.isNetworkPath("  \\\\?\\C:\\Music\\song.mp3"));
        assertFalse(FileUtils.isNetworkPath("C:\\Music\\song.mp3"));
        assertFalse(FileUtils.isNetworkPath("D:/Music/song.mp3"));
        assertFalse(FileUtils.isNetworkPath("/Users/marco/Music/song.mp3"));
        assertFalse(FileUtils.isNetworkPath("Music/song.mp3"));
    }

    @Test
    void testGetFileEncodingForNonExistentFile() {
        File nonExistentFile = new File("nonExistentFile.txt");
        String encoding = FileUtils.getFileEncoding(nonExistentFile);
        assertNull(encoding);
    }
}
