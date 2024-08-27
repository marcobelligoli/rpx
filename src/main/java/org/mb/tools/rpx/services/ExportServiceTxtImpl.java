package org.mb.tools.rpx.services;

import org.mb.tools.rpx.models.RekordboxSong;
import org.mb.tools.rpx.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of Export Service for txt files
 */
public class ExportServiceTxtImpl extends ExportService {

    @Override
    protected void exportPlaylist(String playlistName, String playlistFilePath, boolean maintainOrder,
                                  String outputFolderPath) throws IOException {
        List<RekordboxSong> songs = new ArrayList<>();
        FileUtils.changeFileEncoding(playlistFilePath);
        String playlistFileEncoding = FileUtils.getFileEncoding(new File(playlistFilePath));
        List<String> playlistLines = FileUtils.readLinesFromFile(playlistFilePath, playlistFileEncoding != null ?
                Charset.forName(playlistFileEncoding) : null);

        // remove headers
        playlistLines.remove(0);

        for (String line : playlistLines) {
            if (!line.equals("\u0000")) {
                List<String> fields = getCleanedLineItems(line, "\t");
                RekordboxSong rekordboxSong = new RekordboxSong();
                rekordboxSong.setTrackNumber(fields.get(0));
                rekordboxSong.setTitle(fixDoubleUTF8Encoding(fields.get(2)));
                rekordboxSong.setBpm(convertDouble(fields.get(3)));
                rekordboxSong.setYear(Integer.parseInt(fields.get(4)));
                rekordboxSong.setArtist(fixDoubleUTF8Encoding(fields.get(5)));
                rekordboxSong.setPersonalTag(fields.get(6));
                rekordboxSong.setAlbum(fields.get(7));
                rekordboxSong.setGenre(fields.get(8));
                rekordboxSong.setClassification(fields.get(9));
                rekordboxSong.setDuration(fields.get(10));
                rekordboxSong.setFilePath(fixDoubleUTF8Encoding(fields.get(11)));
                rekordboxSong.setKey(fields.get(12));
                rekordboxSong.setAddDate(convertStringToDate(fields.get(13)));
                songs.add(rekordboxSong);
            }
        }

        // total file lines without headers
        int playlistFileLines = songs.size();

        // copy list of RekordboxSong in final folder
        logInfo(String.format("Got %d songs from playlist %s", songs.size(), playlistName));

        // if not present, create it
        File playlistFolder = FileUtils.createFolderIfNotExists(outputFolderPath);

        for (RekordboxSong song : songs) {
            String windowsPath = song.getFilePath().replace("/", "\\");
            File songFile = new File(windowsPath);
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
        assert playlistFolder != null;
        checkSongsNumber(playlistFileLines, playlistFolder);
    }

    private static List<String> getCleanedLineItems(String line, String regexForSplit) {
        List<String> cleanedFields = new ArrayList<>();
        String[] fields = line.split(regexForSplit);
        for (String field : fields) {
            String newField = field.replace("\u0000", "");
            cleanedFields.add(newField);
        }
        return cleanedFields;
    }

    private static Double convertDouble(String doubleString) {
        String result = doubleString.replace(",", ".");
        return Double.parseDouble(result);
    }

    private static Date convertStringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            logError(e.getMessage());
            return null;
        }
    }

    private static String fixDoubleUTF8Encoding(String s) {
        // interpret the string as UTF_8
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        // now check if the bytes contain 0x83 0xC2, meaning double encoded garbage
        if (isDoubleEncoded(bytes)) {
            // if so, lets fix the string by assuming it is ASCII extended and recode it once
            s = new String(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
        return s;
    }

    private static boolean isDoubleEncoded(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == -125 && i + 1 < bytes.length && bytes[i + 1] == -62) {
                return true;
            }
        }
        return false;
    }
}