package org.mb.tools.rpx.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RekordboxPlaylistParam {

    private String playlistPath;
    private boolean maintainPlaylistOrder;
}
