package org.mb.tools.rpx.exceptions;

public class RPXException extends RuntimeException {

    public RPXException(Throwable cause) {
        super(cause);
    }

    public RPXException(int playlistFileLines, int totalCopiedSongs) {
        super(String.format("ATTENTION: number of song in txt [%d] is different from total file copied [%d]. " +
                        "Errors occurred during file copy, please retry later.",
                playlistFileLines, totalCopiedSongs));
    }
}
