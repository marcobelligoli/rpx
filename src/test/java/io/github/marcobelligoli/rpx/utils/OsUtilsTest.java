package io.github.marcobelligoli.rpx.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OsUtilsTest {

    @AfterEach
    void tearDown() {
        OsUtils.setSystemPropertyProvider(System::getProperty);
    }

    @Test
    void testGetDesktopPathOnWindows() {
        mockOs("Windows 10");
        FileSystemView mockedFileSystemView = mock(FileSystemView.class);

        // on Windows FileSystemView.getHomeDirectory() resolves the Desktop, even when it is relocated
        File mockedDesktopDirectory = new File("mocked/desktop/path");
        when(mockedFileSystemView.getHomeDirectory()).thenReturn(mockedDesktopDirectory);

        try (var mockedStatic = mockStatic(FileSystemView.class)) {
            mockedStatic.when(FileSystemView::getFileSystemView).thenReturn(mockedFileSystemView);
            String desktopPath = OsUtils.getDesktopPath();
            Path expectedPath = Paths.get("mocked", "desktop", "path").toAbsolutePath();
            Path actualPath = Paths.get(desktopPath).toAbsolutePath();

            assertEquals(expectedPath, actualPath);
        }
    }

    @Test
    void testGetDesktopPathOnUnix(@TempDir Path home) {
        File desktop = new File(home.toFile(), "Desktop");
        assertTrue(desktop.mkdir());
        mockOs("Mac OS X", home.toString());

        assertEquals(desktop.getAbsolutePath(), OsUtils.getDesktopPath());
    }

    @Test
    void testGetDesktopPathOnUnixWithoutDesktopFolder(@TempDir Path home) {
        mockOs("Linux", home.toString());

        assertEquals(home.toFile().getAbsolutePath(), OsUtils.getDesktopPath());
    }

    @Test
    void testGetOperatingSystem_Windows() {
        testOs("Windows 10", "windows 10");
    }

    @Test
    void testGetOperatingSystem_Mac() {
        testOs("Mac OS X", "mac os x");
    }

    @Test
    void testGetOperatingSystem_Linux() {
        testOs("Linux", "linux");
    }

    @Test
    void testIsWindows() {
        mockOs("Windows 10");
        assertTrue(OsUtils.isWindows());
        assertFalse(OsUtils.isMac());
        assertFalse(OsUtils.isLinuxOrUnix());
    }

    @Test
    void testIsMac() {
        mockOs("Mac OS X");
        assertTrue(OsUtils.isMac());
        assertFalse(OsUtils.isWindows());
        assertFalse(OsUtils.isLinuxOrUnix());
    }

    @Test
    void testIsLinuxOrUnix() {
        mockOs("Linux");
        assertTrue(OsUtils.isLinuxOrUnix());
        assertFalse(OsUtils.isWindows());
        assertFalse(OsUtils.isMac());
    }

    private static void testOs(String osName, String expected) {
        mockOs(osName);
        assertEquals(expected, OsUtils.getOperatingSystem());
    }

    private static void mockOs(String osName) {
        mockOs(osName, System.getProperty("user.home"));
    }

    private static void mockOs(String osName, String userHome) {
        SystemPropertyProvider mockProvider = mock(SystemPropertyProvider.class);
        when(mockProvider.getProperty("os.name")).thenReturn(osName);
        when(mockProvider.getProperty("user.home")).thenReturn(userHome);
        OsUtils.setSystemPropertyProvider(mockProvider);
    }
}
