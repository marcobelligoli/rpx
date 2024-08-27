package org.mb.tools.rpx.models;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RekordboxSong {

    private String trackNumber;
    private String title;
    private Double bpm;
    private Integer year;
    private String artist;
    private String personalTag;
    private String album;
    private String genre;
    private String classification;
    private String duration;
    private String filePath;
    private String key;
    private Date addDate;

    public void setTrackNumber(String trackNumber) {
        if (trackNumber.length() == 1)
            this.trackNumber = ("00" + trackNumber);
        else if (trackNumber.length() == 2)
            this.trackNumber = ("0" + trackNumber);
        else
            this.trackNumber = trackNumber;
    }
}
