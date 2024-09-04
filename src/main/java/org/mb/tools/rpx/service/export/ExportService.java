package org.mb.tools.rpx.service.export;

import org.mb.tools.rpx.model.RekordboxPlaylistParam;

import java.util.List;

/**
 * Service class for export
 */
public interface ExportService {

    /**
     * Exports all playlists
     *
     * @param playlistsToExport List of RekordboxPlaylistParam with info about playlist file to export
     */
    void exportPlaylists(List<RekordboxPlaylistParam> playlistsToExport);
}
