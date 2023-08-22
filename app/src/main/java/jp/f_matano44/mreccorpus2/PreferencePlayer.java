package jp.f_matano44.mreccorpus2;

import java.io.File;
import java.util.Arrays;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

class PreferencePlayer {
    private final File[] list;
    private final int[] currentIndex;
    private final MediaPlayer mediaPlayer;

    public PreferencePlayer(final AppConfig conf, int[] currentIndex) {
        System.out.println("VLC Native Discovery: " + new NativeDiscovery().discover());

        this.currentIndex = currentIndex;
        this.mediaPlayer = new MediaPlayerFactory().mediaPlayers().newMediaPlayer();

        if (conf.preference.exists() && conf.preference.isDirectory()) {
            list = conf.preference.listFiles((dir, file) -> {
                if (file.toLowerCase().endsWith(".wav")) {
                    return true;
                } else {
                    return false;
                }
            });
        } else {
            list = new File[0];
        }

        if (list != null) {
            Arrays.sort(list, (file1, file2) -> file1.getName().compareTo(file2.getName()));
        }
    }

    public void playPreference() {
        this.mediaPlayer.media().play(list[currentIndex[0]].getAbsolutePath());
    }
}
