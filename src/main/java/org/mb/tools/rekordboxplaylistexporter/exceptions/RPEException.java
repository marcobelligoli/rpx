package org.mb.tools.rekordboxplaylistexporter.exceptions;

public class RPEException extends RuntimeException {

    public RPEException(Throwable cause) {
        super(cause);
    }

    public RPEException(int playlistFileLines, int totalCopiedSongs) {
        super(String.format("ATTENTION: number of song in txt [%d] is different from total file copied [%d]. " +
                        "Errors occured during file copy: Check txt file encoding first (it must be UTF-8).",
                playlistFileLines, totalCopiedSongs));
    }
}
