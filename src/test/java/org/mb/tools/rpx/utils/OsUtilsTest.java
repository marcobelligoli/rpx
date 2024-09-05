package org.mb.tools.rpx.utils;

import org.junit.jupiter.api.Test;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OsUtilsTest {

    @Test
    void testGetDesktopPath() {
        FileSystemView mockedFileSystemView = mock(FileSystemView.class);

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
        SystemPropertyProvider mockProvider = mock(SystemPropertyProvider.class);
        when(mockProvider.getProperty("os.name")).thenReturn("Windows 10");
        OsUtils.setSystemPropertyProvider(mockProvider);
        assertTrue(OsUtils.isWindows());
        assertFalse(OsUtils.isMac());
        assertFalse(OsUtils.isLinuxOrUnix());
    }

    @Test
    void testIsMac() {
        SystemPropertyProvider mockProvider = mock(SystemPropertyProvider.class);
        when(mockProvider.getProperty("os.name")).thenReturn("Mac OS X");
        OsUtils.setSystemPropertyProvider(mockProvider);
        assertTrue(OsUtils.isMac());
        assertFalse(OsUtils.isWindows());
        assertFalse(OsUtils.isLinuxOrUnix());
    }

    @Test
    void testIsLinuxOrUnix() {
        SystemPropertyProvider mockProvider = mock(SystemPropertyProvider.class);
        when(mockProvider.getProperty("os.name")).thenReturn("Linux");
        OsUtils.setSystemPropertyProvider(mockProvider);
        assertTrue(OsUtils.isLinuxOrUnix());
        assertFalse(OsUtils.isWindows());
        assertFalse(OsUtils.isMac());
    }

    private static void testOs(String t, String expected) {
        SystemPropertyProvider mockProvider = mock(SystemPropertyProvider.class);
        when(mockProvider.getProperty("os.name")).thenReturn(t);
        OsUtils.setSystemPropertyProvider(mockProvider);
        assertEquals(expected, OsUtils.getOperatingSystem());
    }
}
