package io.github.marcobelligoli.rpx.service.export;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.marcobelligoli.rpx.model.RekordboxSong;
import io.github.marcobelligoli.rpx.utils.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class ExportServiceM3u8ImplTest {

    private final static String TEST_FILE_PATH = "test.m3u8";
    private final static String FIRST_SONG_PATH = "D:\\PLAYLIST NEW\\Hardstyle\\Axel F - Crazy Frog.mp3";
    private final static String SECOND_SONG_PATH = "D:\\PLAYLIST NEW\\Hardstyle\\Ran-D - Zombie.mp3";

    @InjectMocks
    private ExportServiceM3u8Impl exportService;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void testGetRekordboxSongsValidInput() {
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(TEST_FILE_PATH), any()))
                    .thenReturn(getFileLines("#EXTM3U"));

            List<RekordboxSong> songs = exportService.getRekordboxSongs(TEST_FILE_PATH);

            assertNotNull(songs);
            assertEquals(2, songs.size());

            // the m3u8 format carries no track number, the position in the file is the one of the playlist
            assertEquals("001", songs.get(0).getTrackNumber());
            assertEquals(FIRST_SONG_PATH, songs.get(0).getFilePath());
            assertEquals("002", songs.get(1).getTrackNumber());
            assertEquals(SECOND_SONG_PATH, songs.get(1).getFilePath());
        }
    }

    @Test
    void testGetRekordboxSongsSkipsByteOrderMark() {
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(TEST_FILE_PATH), any()))
                    .thenReturn(getFileLines("\uFEFF#EXTM3U"));

            List<RekordboxSong> songs = exportService.getRekordboxSongs(TEST_FILE_PATH);

            assertEquals(2, songs.size());
            assertEquals(FIRST_SONG_PATH, songs.get(0).getFilePath());
        }
    }

    @Test
    void testGetRekordboxSongsEmptyFile() {
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(TEST_FILE_PATH), any()))
                    .thenReturn(new ArrayList<>());

            List<RekordboxSong> songs = exportService.getRekordboxSongs(TEST_FILE_PATH);

            assertNotNull(songs);
            assertTrue(songs.isEmpty());
        }
    }

    @Test
    void testGetRekordboxSongsWithoutDirectives() {
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            List<String> lines = new ArrayList<>();
            lines.add(FIRST_SONG_PATH);
            lines.add(SECOND_SONG_PATH);
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(TEST_FILE_PATH), any()))
                    .thenReturn(lines);

            List<RekordboxSong> songs = exportService.getRekordboxSongs(TEST_FILE_PATH);

            assertEquals(2, songs.size());
            assertEquals(FIRST_SONG_PATH, songs.get(0).getFilePath());
        }
    }

    private static List<String> getFileLines(String header) {
        List<String> sampleFileLines = new ArrayList<>();
        sampleFileLines.add(header);
        sampleFileLines.add("#EXTINF:155, - Axel F - Crazy Frog (HardEditz Remix) [Hardstyle]");
        sampleFileLines.add(FIRST_SONG_PATH);
        sampleFileLines.add("");
        sampleFileLines.add("#EXTINF:289,Ran-D - Zombie");
        sampleFileLines.add(SECOND_SONG_PATH);
        return sampleFileLines;
    }
}
