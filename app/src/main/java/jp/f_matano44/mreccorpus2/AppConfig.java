package jp.f_matano44.mreccorpus2;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

final class AppConfig extends JFrame {
    public final AudioFormat format;
    public final File script;
    public final File saveTo;
    public final File preference;
    public final boolean isNormalize;
    public final double normalizationLevel; // 0.05 ~ 0.95

    public AppConfig() {
        super("Config");

        // Load config file
        try {
            throw new Exception();
        } catch (Exception e) {
            // audio format
            final int channels = 1;
            final int nbits = 24;
            final int fs = 96000;
            this.format = new AudioFormat(fs, nbits, channels, true, false);

            // path setting
            final String osName = System.getProperty("os.name").toLowerCase();
            final File basePath;
            if (osName.contains("win") || osName.contains("mac")) {
                basePath = new File(System.getProperty("user.home"), "Desktop");
            } else {
                basePath = new File(System.getProperty("user.home"));
            }
            this.script = new File(basePath, "script.txt");
            this.saveTo = new File(basePath, "corpus/");
            this.preference = new File(basePath, "preference/");

            this.normalizationLevel = 1.0;
        }
        if (!this.saveTo.exists()) {
            this.saveTo.mkdirs();
        }
        this.isNormalize = (0.05 <= this.normalizationLevel && this.normalizationLevel <= 0.95);

        // Build string
        final int fs = (int) this.format.getSampleRate();
        final int nbits = this.format.getSampleSizeInBits();
        final int channels = this.format.getChannels();
        final StringBuilder sb = new StringBuilder();
        sb.append("Sampling rate (Fs)\n");
        sb.append("> " + fs + " [Hz]\n");
        sb.append("\n");
        sb.append("Bit depth (nBits)\n");
        sb.append("> " + nbits + " [bit]\n");
        sb.append("\n");
        sb.append("Channels\n");
        sb.append("> " + channels + "\n");
        sb.append("\n");
        sb.append("Normalization\n");
        if (this.isNormalize) {
            sb.append("> " + this.isNormalize + " (" + this.normalizationLevel + ")");
        } else {
            sb.append("> " + this.isNormalize);
        }
        final JTextArea textArea = new JTextArea(sb.toString());
        final int blank = 20;
        textArea.setEditable(false);   
        textArea.setBorder(new EmptyBorder(blank, blank, blank, blank));
        this.add(textArea);

        // Window setting
        this.pack();
        this.setResizable(false);
        this.setMinimumSize(getSize());
        this.setLocationRelativeTo(null);
        this.setVisible(false);
    }

    public final File getSavePath(final int number) {
        return new File(
            this.saveTo, 
            "corpus_" + String.format("%04d", number) + ".wav"
        );
    }
}
