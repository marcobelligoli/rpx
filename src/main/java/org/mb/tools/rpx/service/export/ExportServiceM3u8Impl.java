package org.mb.tools.rpx.service.export;

import org.mb.tools.rpx.model.RekordboxSong;
import org.mb.tools.rpx.utils.FileUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Export Service for m3u8 files
 */
public class ExportServiceM3u8Impl extends AbstractExportService {

    private static final String DIRECTIVE_PREFIX = "#";
    private static final String BYTE_ORDER_MARK = "﻿";

    @Override
    protected List<RekordboxSong> getRekordboxSongs(String playlistFilePath) {
        // m3u8 is UTF-8 by definition, so the encoding is neither detected nor rewritten as for txt playlists
        List<String> playlistLines = FileUtils.readLinesFromFile(playlistFilePath, StandardCharsets.UTF_8);

        List<RekordboxSong> songs = new ArrayList<>();
        int trackNumber = 1;
        for (String playlistLine : playlistLines) {
            String filePath = cleanLine(playlistLine);

            // directives such as #EXTM3U and #EXTINF carry no path: every other line is a track
            if (filePath.isEmpty() || filePath.startsWith(DIRECTIVE_PREFIX)) {
                continue;
            }

            RekordboxSong song = new RekordboxSong();
            // the format has no track number column, the position in the file is the order of the playlist
            song.setTrackNumber(String.valueOf(trackNumber));
            song.setFilePath(filePath);
            songs.add(song);
            trackNumber++;
        }
        return songs;
    }

    private static String cleanLine(String playlistLine) {
        return playlistLine.replace(BYTE_ORDER_MARK, "").trim();
    }
}
