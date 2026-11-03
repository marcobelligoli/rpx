package io.github.marcobelligoli.rpx.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

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
    void testChangeFileEncodingConvertsToUtf8() throws IOException {
        String content = "#\tTitolo\n1\tCafé è ü\n";
        byte[] bom = {(byte) 0xFF, (byte) 0xFE};
        byte[] body = content.getBytes(StandardCharsets.UTF_16LE);
        byte[] utf16 = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, utf16, 0, bom.length);
        System.arraycopy(body, 0, utf16, bom.length, body.length);
        Files.write(tempFile.toPath(), utf16);

        FileUtils.changeFileEncoding(tempFile.getAbsolutePath());

        String converted = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8).replace("﻿", "");
        assertTrue(converted.contains("Café è ü"));
        assertNoTemporaryFilesLeft();
    }

    @Test
    void testChangeFileEncodingLeavesUtf8FileUntouched() throws IOException {
        byte[] original = "#\tTitolo\r\n1\tCafé\r\n".getBytes(StandardCharsets.UTF_8);
        Files.write(tempFile.toPath(), original);

        FileUtils.changeFileEncoding(tempFile.getAbsolutePath());

        assertArrayEquals(original, Files.readAllBytes(tempFile.toPath()));
    }

    @Test
    void testChangeFileEncodingOnMissingFileCreatesNothing() {
        File missingFile = new File(tempDirectory, "missing.txt");

        FileUtils.changeFileEncoding(missingFile.getAbsolutePath());

        assertFalse(missingFile.exists());
        assertEquals(0, Objects.requireNonNull(tempDirectory.listFiles()).length);
    }

    private void assertNoTemporaryFilesLeft() {
        File[] leftovers = tempFile.getParentFile().listFiles(
                (dir, name) -> name.startsWith(tempFile.getName()) && name.endsWith(".tmp"));
        assertEquals(0, Objects.requireNonNull(leftovers).length);
    }

    @Test
    void testGetFileEncodingForNonExistentFile() {
        File nonExistentFile = new File("nonExistentFile.txt");
        String encoding = FileUtils.getFileEncoding(nonExistentFile);
        assertNull(encoding);
    }
}
