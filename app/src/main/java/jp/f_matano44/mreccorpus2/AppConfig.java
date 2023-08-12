package jp.f_matano44.mreccorpus2;

import java.awt.Font;
import java.io.File;
import javax.sound.sampled.AudioFormat;

class AppConfig {
    public final AudioFormat format;
    public final File scripts;
    public final File saveTo;
    public final boolean isNormalize;
    public final double normalizationLevel; // 0.05 ~ 0.95
    public static final Font fontSet = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    public AppConfig() {
        try {
            throw new Exception();
        } catch (Exception e) {
            // audio format
            final int channels = 1;
            final int nbits = 24;
            final int fs = 44100;
            this.format = new AudioFormat(fs, nbits, channels, true, false);

            // path setting
            final String osName = System.getProperty("os.name").toLowerCase();
            final File basePath;
            if (osName.contains("win") || osName.contains("mac")) {
                basePath = new File(System.getProperty("user.home"), "Desktop");
            } else {
                basePath = new File(System.getProperty("user.home"));
            }
            this.scripts = new File(basePath, "scripts.txt");
            this.saveTo = new File(basePath, "corpus/");

            this.normalizationLevel = 0.9;
        }

        if (!this.saveTo.exists()) {
            this.saveTo.mkdirs();
        }

        this.isNormalize = (0.05 <= this.normalizationLevel && this.normalizationLevel <= 0.95);
    }
}
