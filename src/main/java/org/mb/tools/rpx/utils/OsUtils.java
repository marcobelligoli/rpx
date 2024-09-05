package org.mb.tools.rpx.utils;

import lombok.Setter;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class OsUtils {

    /**
     * -- SETTER --
     * Sets the system property provider. For testing purposes.
     *
     * @param provider The system property provider.
     */
    @Setter
    private static SystemPropertyProvider systemPropertyProvider = System::getProperty;

    private OsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the Desktop path
     *
     * @return Desktop path
     */
    public static String getDesktopPath() {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File desktopDirectory = fileSystemView.getHomeDirectory();
        return desktopDirectory.getAbsolutePath();
    }

    /**
     * Returns the operating system name.
     *
     * @return The name of the operating system in lowercase.
     */
    public static String getOperatingSystem() {
        return systemPropertyProvider.getProperty("os.name").toLowerCase();
    }

    /**
     * Checks if the application is running on a Windows operating system.
     *
     * @return True if the operating system is Windows, false otherwise.
     */
    public static boolean isWindows() {
        return getOperatingSystem().contains("win");
    }

    /**
     * Checks if the application is running on a Mac operating system.
     *
     * @return True if the operating system is Mac, false otherwise.
     */
    public static boolean isMac() {
        return getOperatingSystem().contains("mac");
    }

    /**
     * Checks if the application is running on a Linux or Unix operating system.
     *
     * @return True if the operating system is Linux or Unix, false otherwise.
     */
    public static boolean isLinuxOrUnix() {
        return getOperatingSystem().contains("nux") || getOperatingSystem().contains("nix");
    }
}
