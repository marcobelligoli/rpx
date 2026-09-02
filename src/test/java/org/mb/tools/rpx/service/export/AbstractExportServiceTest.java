package org.mb.tools.rpx.service.export;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class AbstractExportServiceTest {

    @InjectMocks
    private TestExportService testExportService;

    private File songFile;
    private Path desktop;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() throws IOException {
        openMocks = MockitoAnnotations.openMocks(this);
        desktop = Files.createTempDirectory("desktop");
        songFile = new File(desktop.toFile(), "song.mp3");
        if (!songFile.createNewFile()) {
            throw new RuntimeException("Error during test song creation");
        }
        testExportService = new TestExportService(songFile);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (desktop != null) {
            deleteRecursively(desktop);
        }
        openMocks.close();
    }

    @Test
    void testExportPlaylists() {

        try (MockedStatic<OsUtils> mockedOsUtils = mockStatic(OsUtils.class)) {
            mockedOsUtils.when(OsUtils::getDesktopPath).thenReturn(desktop.toFile().getAbsolutePath());

            List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
            RekordboxPlaylistParam param = new RekordboxPlaylistParam();
            param.setPlaylistPath(getPath("test.txt"));
            param.setMaintainPlaylistOrder(false);
            playlistsToExport.add(param);

            testExportService.exportPlaylists(playlistsToExport);

            assertTrue(desktop.resolve("test").resolve("song.mp3").toFile().exists());
        }
    }

    @Test
    void testExportPlaylistsMaintainOrder() {

        try (MockedStatic<OsUtils> mockedOsUtils = mockStatic(OsUtils.class)) {
            mockedOsUtils.when(OsUtils::getDesktopPath).thenReturn(desktop.toFile().getAbsolutePath());

            List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
            RekordboxPlaylistParam param = new RekordboxPlaylistParam();
            param.setPlaylistPath(getPath("test.txt"));
            param.setMaintainPlaylistOrder(true);
            playlistsToExport.add(param);

            testExportService.exportPlaylists(playlistsToExport);

            assertTrue(desktop.resolve("test").resolve("001 - song.mp3").toFile().exists());
        }
    }

    @Test
    void testExportPlaylistsInChosenFolder(@TempDir Path chosenFolder) {

        try (MockedStatic<OsUtils> mockedOsUtils = mockStatic(OsUtils.class)) {
            mockedOsUtils.when(OsUtils::getDesktopPath).thenReturn(desktop.toFile().getAbsolutePath());

            List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
            RekordboxPlaylistParam param = new RekordboxPlaylistParam();
            param.setPlaylistPath(getPath("test.txt"));
            param.setMaintainPlaylistOrder(false);
            playlistsToExport.add(param);

            testExportService.exportPlaylists(playlistsToExport, chosenFolder.toFile().getAbsolutePath());

            assertTrue(chosenFolder.resolve("test").resolve("song.mp3").toFile().exists());
            assertFalse(desktop.resolve("test").toFile().exists());
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

        try (MockedStatic<OsUtils> mockedOsUtils = mockStatic(OsUtils.class)) {
            mockedOsUtils.when(OsUtils::getDesktopPath).thenReturn(desktop.toFile().getAbsolutePath());

            List<RekordboxPlaylistParam> playlistsToExport = new ArrayList<>();
            RekordboxPlaylistParam param = new RekordboxPlaylistParam();
            param.setPlaylistPath(getPath("test.txt"));
            param.setMaintainPlaylistOrder(false);
            playlistsToExport.add(param);

            if (!songFile.delete()) {
                throw new RuntimeException("Error during test song deletion");
            }

            assertThrows(RPXException.class, () -> testExportService.exportPlaylists(playlistsToExport));
        }
    }

    @Test
    void testGetOutputFolderPathUsesNativePathResolution() {
        assertEquals(desktop.resolve("playlist").toString(),
                AbstractExportService.getOutputFolderPath(desktop.toString(), "playlist"));
    }

    @Test
    void testGetPlaylistNameSupportsWindowsPath() {
        RekordboxPlaylistParam param = new RekordboxPlaylistParam();
        param.setPlaylistPath("C:\\Users\\Marco\\Desktop\\my.playlist.txt");

        assertEquals("my.playlist", AbstractExportService.getPlaylistName(param));
    }

    @Test
    void testGetPlaylistNameSupportsUnixPath() {
        RekordboxPlaylistParam param = new RekordboxPlaylistParam();
        param.setPlaylistPath("/Users/marco/Desktop/my.playlist.txt");

        assertEquals("my.playlist", AbstractExportService.getPlaylistName(param));
    }

    @Test
    void testGetFileSupportsAlternateSeparators() throws IOException {
        Path folder = Files.createDirectories(desktop.resolve("music"));
        Path file = Files.createFile(folder.resolve("track.mp3"));
        String nativePath = file.toString();
        char alternateSeparator = File.separatorChar == '/' ? '\\' : '/';
        String alternatePath = nativePath.replace(File.separatorChar, alternateSeparator);
        RekordboxSong song = new RekordboxSong();
        song.setFilePath(alternatePath);

        assertTrue(AbstractExportService.getFile(song).exists());
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
            song.setTrackNumber("1");
            song.setFilePath(songFile.getAbsolutePath());
            songs.add(song);
            return songs;
        }
    }

    private String getPath(String filename) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toExternalForm();
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder())
                    .forEach(file -> file.toFile().delete());
        }
    }
}
