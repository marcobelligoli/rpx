package io.github.marcobelligoli.rpx.service.export;

import io.github.marcobelligoli.rpx.model.RekordboxPlaylistParam;

import java.util.List;

/**
 * Service class for export
 */
public interface ExportService {

    /**
     * Exports all playlists in the default destination folder (the Desktop)
     *
     * @param playlistsToExport List of RekordboxPlaylistParam with info about playlist file to export
     */
    void exportPlaylists(List<RekordboxPlaylistParam> playlistsToExport);

    /**
     * Exports all playlists in the given destination folder
     *
     * @param playlistsToExport     List of RekordboxPlaylistParam with info about playlist file to export
     * @param destinationFolderPath Folder where a subfolder is created for each exported playlist
     */
    void exportPlaylists(List<RekordboxPlaylistParam> playlistsToExport, String destinationFolderPath);
}
