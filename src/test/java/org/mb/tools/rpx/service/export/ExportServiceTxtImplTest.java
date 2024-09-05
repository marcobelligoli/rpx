package org.mb.tools.rpx.service.export;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mb.tools.rpx.model.RekordboxSong;
import org.mb.tools.rpx.utils.FileUtils;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class ExportServiceTxtImplTest {

    private final static String TEST_FILE_PATH = "test.txt";
    private final static String EMPTY_FILE_PATH = "empty.txt";

    @InjectMocks
    private ExportServiceTxtImpl exportService;

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
            mockedFileUtils.when(() -> FileUtils.getFileEncoding(any(File.class)))
                    .thenReturn(StandardCharsets.UTF_8.name());
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(TEST_FILE_PATH), any()))
                    .thenReturn(getFileLines(true, true));

            List<RekordboxSong> songs = exportService.getRekordboxSongs(TEST_FILE_PATH);

            assertNotNull(songs);
            assertEquals(1, songs.size());

            RekordboxSong song = songs.get(0);
            assertEquals("001", song.getTrackNumber());
            assertEquals("ROMANCE", song.getTitle());
            assertEquals(88, song.getBpm());
            assertEquals(0, song.getYear());
            assertEquals("Fred De Palma", song.getArtist());
            assertEquals("ROMANCE", song.getAlbum());
            assertEquals("03:00", song.getDuration());
            assertEquals("D:/test/Fred De Palma - ROMANCE.mp3", song.getFilePath());
            assertEquals("", song.getPersonalTag());
            assertEquals("", song.getKey());
            assertNotNull(song.getAddDate());
        }
    }

    @Test
    void testGetRekordboxSongsInvalidDate() {
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            mockedFileUtils.when(() -> FileUtils.getFileEncoding(any(File.class)))
                    .thenReturn(StandardCharsets.UTF_8.name());
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(TEST_FILE_PATH), any()))
                    .thenReturn(getFileLines(true, false));

            List<RekordboxSong> songs = exportService.getRekordboxSongs(TEST_FILE_PATH);

            assertNotNull(songs);
            assertEquals(1, songs.size());

            RekordboxSong song = songs.get(0);
            assertEquals("001", song.getTrackNumber());
            assertEquals("ROMANCE", song.getTitle());
            assertEquals(88, song.getBpm());
            assertEquals(0, song.getYear());
            assertEquals("Fred De Palma", song.getArtist());
            assertEquals("ROMANCE", song.getAlbum());
            assertEquals("03:00", song.getDuration());
            assertEquals("D:/test/Fred De Palma - ROMANCE.mp3", song.getFilePath());
            assertEquals("", song.getPersonalTag());
            assertEquals("", song.getKey());
            assertNull(song.getAddDate());
        }
    }

    @Test
    void testGetRekordboxSongsEmptyFile() {
        try (MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            mockedFileUtils.when(() -> FileUtils.getFileEncoding(any(File.class)))
                    .thenReturn(StandardCharsets.UTF_8.name());
            mockedFileUtils.when(() -> FileUtils.readLinesFromFile(Mockito.eq(EMPTY_FILE_PATH), any()))
                    .thenReturn(getFileLines(false, false));

            List<RekordboxSong> songs = exportService.getRekordboxSongs(EMPTY_FILE_PATH);
            assertNotNull(songs);
            assertTrue(songs.isEmpty());
        }
    }

    private static List<String> getFileLines(boolean addLine, boolean isDateValid) {
        List<String> sampleFileLines = new ArrayList<>();
        sampleFileLines.add("#\tIllustrazione\tTitolo Brano\tBPM\tAnno\tArtista\tTag personale\tAlbum\tGenere\tClassifica\tTempo\tPosizione\tTonalità\tData di aggiunta");
        if (addLine) {
            String line = "1\t\tROMANCE\t88,00\t0\tFred De Palma\t\tROMANCE\t\t     \t03:00\tD:/test/Fred De Palma - ROMANCE.mp3\t\t";
            if (isDateValid) {
                line += "2023-07-16";
            } else {
                line += "16/07/2023";
            }
            sampleFileLines.add(line);
        }
        return sampleFileLines;
    }
}
