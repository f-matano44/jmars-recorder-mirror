package jp.f_matano44.mreccorpus2;

import java.io.File;
import java.util.Arrays;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

class PreferencePlayer {
    public final File[] list;
    private final NativeDiscovery vlc;
    private final MediaPlayer mediaPlayer;

    public PreferencePlayer() {
        vlc = new NativeDiscovery();
        System.out.println("VLC Native Discovery: " + vlc.discover());

        this.mediaPlayer = vlc.discover()
            ? new MediaPlayerFactory().mediaPlayers().newMediaPlayer()
            : null;

        if (AppConfig.preference.exists() && AppConfig.preference.isDirectory()) {
            list = AppConfig.preference.listFiles((dir, file) -> {
                return file.toLowerCase().endsWith(".wav") ? true : false;
            });
        } else {
            list = new File[0];
        }

        if (list != null) {
            Arrays.sort(list, (file1, file2) -> file1.getName().compareTo(file2.getName()));
        }
    }

    public void playPreference(final int currentIndex) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.media().play(list[currentIndex].getAbsolutePath());
        }
    }
}
