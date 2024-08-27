package org.mb.tools.rpx.services;

import org.mb.tools.rpx.exceptions.RPXException;
import org.mb.tools.rpx.models.RekordboxPlaylistParam;
import org.mb.tools.rpx.utils.FileUtils;
import org.mb.tools.rpx.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Abstract service class for export
 */
public abstract class ExportService {

    private static final String[] AUDIO_FORMATS = {".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a"};

    private static final Logger logger = Logger.getLogger(ExportService.class.getName());

    /**
     * Exports all playlists
     *
     * @param playlistsToExport List of RekordboxPlaylistParam with info about playlist file to export
     */
    public void exportPlaylists(List<RekordboxPlaylistParam> playlistsToExport) {
        try {
            String desktopPath = FileUtils.getDesktopPath();

            for (RekordboxPlaylistParam rekordboxPlaylistParam : playlistsToExport) {

                String playlistName = rekordboxPlaylistParam.getPlaylistPath()
                        .split("\\\\")[rekordboxPlaylistParam.getPlaylistPath().split("\\\\").length - 1]
                        .split("\\.")[0];
                String outputFolderPath = desktopPath + "\\" + playlistName;

                exportPlaylist(playlistName, rekordboxPlaylistParam.getPlaylistPath(),
                        rekordboxPlaylistParam.isMaintainPlaylistOrder(), outputFolderPath);

                logInfo("Operation completed for playlist " + playlistName + "! Output: " +
                        outputFolderPath);
            }
            logInfo("Operation completed for " + playlistsToExport.size() + " playlist");
        } catch (Exception e) {
            throw new RPXException(e);
        }
    }

    /**
     * Works on single playlist file and export content to folder
     *
     * @param playlistName     Name of playlist to export
     * @param playlistFilePath File path of playlist to export
     * @param maintainOrder    True if maintain tracks order, false otherwise
     * @param outputFolderPath Playlist output folder path
     * @throws IOException IOException
     */
    protected abstract void exportPlaylist(String playlistName, String playlistFilePath, boolean maintainOrder,
                                           String outputFolderPath) throws IOException;

    /**
     * Logs info
     *
     * @param message Message to log
     */
    protected static void logInfo(String message) {
        LogUtils.info(logger, message);
    }

    /**
     * Logs error
     *
     * @param message Message to log
     */
    protected static void logError(String message) {
        LogUtils.error(logger, message);
    }

    /**
     * Checks if the playlists number in the input file is equals to files in the created playlist folder
     *
     * @param playlistFileLines Number of songs in the input playlist file
     * @param playlistFolder    Playlist folder created
     */
    protected static void checkSongsNumber(int playlistFileLines, File playlistFolder) {
        File[] copiedSongs = Objects.requireNonNull(playlistFolder.listFiles());

        int totalCopiedSongs = 0;
        for (File song : copiedSongs) {
            if (isAudioFile(song.getName()))
                totalCopiedSongs++;
        }

        if (playlistFileLines != totalCopiedSongs) {
            throw new RPXException(playlistFileLines, totalCopiedSongs);
        } else {
            logInfo("All files copied in folder.");
        }
    }

    private static boolean isAudioFile(String songFileName) {
        boolean isAudioFile = false;

        for (String format : AUDIO_FORMATS) {
            if (songFileName.toLowerCase(Locale.ROOT).endsWith(format)) {
                isAudioFile = true;
                break;
            }
        }

        return isAudioFile;
    }
}
