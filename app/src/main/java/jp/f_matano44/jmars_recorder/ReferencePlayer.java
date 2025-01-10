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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import javax.swing.JButton;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

final class ReferencePlayer {
    // MARK: Swing Components
    public static final JButton refButton = new JButton("Play Ref.");
    public static final JButton no001Button = new JButton("Play No.001");

    // MARK: Member variables
    private static final File[] list;
    private static final boolean isPlayerExist = new NativeDiscovery().discover();
    private static final MediaPlayer mediaPlayer = isPlayerExist
        ? new MediaPlayerFactory().mediaPlayers().newMediaPlayer() : null;


    // MARK: Constructor
    private ReferencePlayer() {}


    // MARK: Static initializer
    static {
        list = loadFiles();

        refButton.addActionListener((final ActionEvent e) ->
            playReference(ScriptManager.getCurrentIndex())
        );

        no001Button.addActionListener((final ActionEvent e) ->
            playNumber001()
        );
    }


    public static void updateThis() {
        no001Button.setEnabled(
            !RecorderBody.isRecording()
            && ReferencePlayer.isPlayerExist
            && ReferencePlayer.isNo001Exist()
        );

        refButton.setEnabled(
            !RecorderBody.isRecording()
            && ReferencePlayer.isPlayerExist
            && ReferencePlayer.list.length > ScriptManager.getCurrentIndex()
            && ReferencePlayer.list[ScriptManager.getCurrentIndex()].exists()
        );
    }

    private static File[] loadFiles() {
        File[] list = null;

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

        return list;
    }

    private static void playNumber001() {
        final File dir = new File(AppConfig.saveTo.getAbsolutePath());
        final String[] wavFiles = dir.list(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".wav");
            }
        });

        Arrays.sort(wavFiles);
        final String playWavPath = new File(dir, wavFiles[0]).getAbsolutePath();

        try {
            mediaPlayer.media().play(playWavPath);
        } catch (Exception e) {
            // media-player cannot work.
        }
    }

    private static boolean isNo001Exist() {
        final File dir = new File(AppConfig.saveTo.getAbsolutePath());
        final String[] wavFiles = dir.list(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                return name.toLowerCase().endsWith(".wav");
            }
        });

        return 1 <= wavFiles.length;
    }

    private static void playReference(final int currentIndex) {
        try {
            mediaPlayer.media().play(list[currentIndex].getAbsolutePath());
        } catch (final NullPointerException | IndexOutOfBoundsException e) {
            // media-player cannot work.
        } catch (final Exception e) {
            e.printStackTrace(AppConfig.logTargetStream);
        }
    }
}
