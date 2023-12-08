/*
 * jMARS Recorder
 * Copyright (C) 2023  Fumiyoshi MATANO
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jp.f_matano44.jmars_recorder;

import java.io.File;
import java.util.Arrays;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

class ReferencePlayer {
    public final File[] list;
    public final boolean isPlayerExist = new NativeDiscovery().discover();
    private final MediaPlayer mediaPlayer = isPlayerExist 
        ? new MediaPlayerFactory().mediaPlayers().newMediaPlayer() : null;

    public ReferencePlayer() {
        if (AppConfig.reference.exists() && AppConfig.reference.isDirectory()) {
            list = AppConfig.reference.listFiles((dir, file) -> {
                return file.toLowerCase().endsWith(".wav")
                    || file.toLowerCase().endsWith(".mp3");
            });
            Arrays.sort(list, (file1, file2) -> 
                file1.getName().compareTo(file2.getName())
            );
        } else {
            list = new File[0];
        }
    }

    public void playPreference(final int currentIndex) {
        try {
            this.mediaPlayer.media().play(this.list[currentIndex].getAbsolutePath());
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            // media-player cannot work.
        }
    }
}
