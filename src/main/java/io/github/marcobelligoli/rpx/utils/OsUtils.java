package io.github.marcobelligoli.rpx.utils;

import lombok.Setter;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Locale;

public class OsUtils {

    /**
     * -- SETTER --
     * Sets the system property provider. For testing purposes.
     */
    @Setter
    private static SystemPropertyProvider systemPropertyProvider = System::getProperty;

    private OsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the Desktop path, falling back to the home directory when no Desktop folder exists.
     *
     * @return Desktop path
     */
    public static String getDesktopPath() {
        if (isWindows()) {
            // On Windows the Desktop can be relocated (e.g. OneDrive) and FileSystemView resolves the real one
            return FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
        }

        // On macOS and Linux FileSystemView returns the home directory, so the Desktop is resolved explicitly
        File homeDirectory = new File(systemPropertyProvider.getProperty("user.home"));
        File desktopDirectory = new File(homeDirectory, "Desktop");
        return desktopDirectory.isDirectory() ? desktopDirectory.getAbsolutePath() : homeDirectory.getAbsolutePath();
    }

    /**
     * Returns the operating system name.
     *
     * @return The name of the operating system in lowercase.
     */
    public static String getOperatingSystem() {
        return systemPropertyProvider.getProperty("os.name").toLowerCase(Locale.ROOT);
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
