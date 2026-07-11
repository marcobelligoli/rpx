package org.mb.tools.rpx.service.export;

import org.mb.tools.rpx.exception.RPXException;
import org.mb.tools.rpx.model.RekordboxPlaylistParam;
import org.mb.tools.rpx.model.RekordboxSong;
import org.mb.tools.rpx.utils.FileUtils;
import org.mb.tools.rpx.utils.LogUtils;
import org.mb.tools.rpx.utils.OsUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Abstract service class for export
 */
public abstract class AbstractExportService implements ExportService {

    private static final String[] AUDIO_FORMATS = {".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a"};

    private static final Logger logger = Logger.getLogger(AbstractExportService.class.getName());

    @Override
    public void exportPlaylists(List<RekordboxPlaylistParam> playlistsToExport) {
        try {
            String desktopPath = OsUtils.getDesktopPath();

            for (RekordboxPlaylistParam rekordboxPlaylistParam : playlistsToExport) {

                String playlistName = getPlaylistName(rekordboxPlaylistParam);
                String outputFolderPath = getOutputFolderPath(desktopPath, playlistName);

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

    static String getOutputFolderPath(String desktopPath, String playlistName) {
        return Path.of(desktopPath).resolve(playlistName).toString();
    }

    static String getPlaylistName(RekordboxPlaylistParam rekordboxPlaylistParam) {
        String playlistPath = rekordboxPlaylistParam.getPlaylistPath();
        String normalizedPath = playlistPath.replace('\\', '/');
        String fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex);
    }

    private void exportPlaylist(String playlistName, String playlistFilePath, boolean maintainOrder,
                                String outputFolderPath) throws IOException {
        List<RekordboxSong> songs = getRekordboxSongs(playlistFilePath);

        // total file lines without headers
        int playlistFileLines = songs.size();

        // copy list of RekordboxSong in final folder
        logInfo(String.format("Got %d songs from playlist %s", songs.size(), playlistName));

        // if not present, create it
        File playlistFolder = FileUtils.createFolderIfNotExists(outputFolderPath);
        if (playlistFolder == null) {
            throw new IOException("Impossible to create folder: " + outputFolderPath);
        }

        for (RekordboxSong song : songs) {
            File songFile = getFile(song);
            if (songFile.exists()) {
                logInfo(String.format("Found file [%s]; copying...", songFile.getAbsolutePath()));
                if (maintainOrder) {
                    String newName = song.getTrackNumber() + " - " + songFile.getName();
                    FileUtils.copyAndRenameFile(songFile, playlistFolder, newName);
                } else {
                    FileUtils.copyFile(songFile, playlistFolder);
                }
            } else {
                logInfo(String.format("File [%s] not found", songFile.getAbsolutePath()));
            }
        }

        // check on copied files
        checkSongsNumber(playlistFileLines, playlistFolder);
    }

    static File getFile(RekordboxSong song) {
        String filePath = song.getFilePath();
        File originalFile = new File(filePath);
        if (originalFile.exists()) {
            return originalFile;
        }

        String nativePath = filePath
                .replace('\\', File.separatorChar)
                .replace('/', File.separatorChar);
        return new File(nativePath);
    }

    /**
     * Gets a list of Rekordbox songs from playlist file
     *
     * @param playlistFilePath Path of playlist file (txt,...)
     * @return List of Rekordbox songs retrieved
     */
    protected abstract List<RekordboxSong> getRekordboxSongs(String playlistFilePath);

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

    private static void checkSongsNumber(int playlistFileLines, File playlistFolder) {
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
