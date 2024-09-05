package org.mb.tools.rpx.service.export;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mb.tools.rpx.exception.RPXException;
import org.mb.tools.rpx.model.RekordboxPlaylistParam;
import org.mb.tools.rpx.model.RekordboxSong;
import org.mb.tools.rpx.utils.OsUtils;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class AbstractExportServiceTest {

    @InjectMocks
    private TestExportService testExportService;

    private File songFile;
    private Path desktop;
    private Path playlistFolder;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() throws IOException {
        openMocks = MockitoAnnotations.openMocks(this);
        desktop = Files.createTempDirectory("desktop");
        playlistFolder = Files.createTempDirectory("playlist");
        songFile = new File(desktop.toFile(), "song.mp3");
        if (!songFile.createNewFile()) {
            throw new RuntimeException("Error during test song creation");
        }
        testExportService = new TestExportService(songFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (desktop != null) desktop.toFile().delete();
        if (playlistFolder != null) playlistFolder.toFile().delete();
        if (songFile != null) songFile.delete();
        openMocks.close();
    }

    @Test
    void testExportPlaylists() {

        try (MockedStatic<OsUtils> mockedOsUtils = mockStatic(OsUtils.class)) {
            mockedOsUtils.when(OsUtils::getDesktopPath).thenReturn(desktop.toFile().getAbsolutePath());
            File mockedFolder = new File(playlistFolder.toFile().getAbsolutePath());

            List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
            RekordboxPlaylistParam param = new RekordboxPlaylistParam();
            param.setPlaylistPath(getPath("test.txt"));
            param.setMaintainPlaylistOrder(false);
            playlistsToExport.add(param);

            testExportService.exportPlaylists(playlistsToExport);

            assertTrue(mockedFolder.exists());
        }
    }

    @Test
    void testExportPlaylistsMaintainOrder() {

        try (MockedStatic<OsUtils> mockedOsUtils = mockStatic(OsUtils.class)) {
            mockedOsUtils.when(OsUtils::getDesktopPath).thenReturn(desktop.toFile().getAbsolutePath());
            File mockedFolder = new File(playlistFolder.toFile().getAbsolutePath());

            List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
            RekordboxPlaylistParam param = new RekordboxPlaylistParam();
            param.setPlaylistPath(getPath("test.txt"));
            param.setMaintainPlaylistOrder(true);
            playlistsToExport.add(param);

            testExportService.exportPlaylists(playlistsToExport);

            assertTrue(mockedFolder.exists());
        }
    }

    @Test
    void testExportPlaylistsError() {

        List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
        RekordboxPlaylistParam param = new RekordboxPlaylistParam();
        param.setMaintainPlaylistOrder(false);
        playlistsToExport.add(param);

        assertThrows(RPXException.class, () -> testExportService.exportPlaylists(playlistsToExport));
    }

    @Test
    void testExportPlaylistsFileNotFound() {

        List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
        RekordboxPlaylistParam param = new RekordboxPlaylistParam();
        param.setPlaylistPath(getPath("test.txt"));
        param.setMaintainPlaylistOrder(false);
        playlistsToExport.add(param);

        songFile.delete();

        assertThrows(RPXException.class, () -> testExportService.exportPlaylists(playlistsToExport));
    }

    private static class TestExportService extends AbstractExportService {

        private final File songFile;

        private TestExportService(File songFile) {
            this.songFile = songFile;
        }

        @Override
        protected List<RekordboxSong> getRekordboxSongs(String playlistFilePath) {
            List<RekordboxSong> songs = new ArrayList<>();
            RekordboxSong song = new RekordboxSong();
            song.setFilePath(songFile.getAbsolutePath());
            songs.add(song);
            return songs;
        }
    }

    private String getPath(String filename) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).getPath();
    }
}
