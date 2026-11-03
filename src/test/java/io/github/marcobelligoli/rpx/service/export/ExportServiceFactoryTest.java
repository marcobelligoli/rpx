package io.github.marcobelligoli.rpx.service.export;

import io.github.marcobelligoli.rpx.exception.RPXException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceFactoryTest {

    @Test
    void testGetExportServiceForTxt() {
        assertInstanceOf(ExportServiceTxtImpl.class, ExportServiceFactory.getExportService("txt"));
    }

    @Test
    void testGetExportServiceForM3u8() {
        assertInstanceOf(ExportServiceM3u8Impl.class, ExportServiceFactory.getExportService("m3u8"));
    }

    @Test
    void testGetExportServiceIgnoresCase() {
        assertInstanceOf(ExportServiceM3u8Impl.class, ExportServiceFactory.getExportService("M3U8"));
    }

    @Test
    void testGetExportServiceUnsupportedFormat() {
        RPXException exception = assertThrows(RPXException.class, () -> ExportServiceFactory.getExportService("csv"));
        assertTrue(exception.getMessage().contains("csv"));
    }

    @Test
    void testGetInputFormatFromWindowsPath() {
        assertEquals("m3u8", ExportServiceFactory.getInputFormat("C:\\Users\\Marco\\Desktop\\Hardstyle.m3u8"));
    }

    @Test
    void testGetInputFormatFromUnixPath() {
        assertEquals("txt", ExportServiceFactory.getInputFormat("/Users/marco/Desktop/my.playlist.txt"));
    }

    @Test
    void testGetInputFormatWithoutExtension() {
        assertEquals("", ExportServiceFactory.getInputFormat("/Users/marco/Desktop/playlist"));
    }

    @Test
    void testGetSupportedFormats() {
        assertEquals(List.of("txt", "m3u8"), ExportServiceFactory.getSupportedFormats());
    }
}
