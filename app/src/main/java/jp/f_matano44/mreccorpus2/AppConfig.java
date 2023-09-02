package jp.f_matano44.mreccorpus2;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

final class AppConfig extends JFrame {
    public static final AudioFormat format;
    public static final File script;
    public static final File saveTo;
    public static final File preference;
    public static final boolean isNormalize;
    public static final double normalizationLevel; // 0.05 ~ 0.95
    public static final String splitChars;

    static {
        // Load config file
        try {
            throw new Exception();
        } catch (Exception e) {
            // audio format
            final int channels = 1;
            final int nbits = 24;
            final int fs = 96000;
            format = new AudioFormat(fs, nbits, channels, true, false);

            // path setting
            final String osName = System.getProperty("os.name").toLowerCase();
            final File basePath = osName.contains("win") || osName.contains("mac")
                ? new File(System.getProperty("user.home"), "Desktop")
                : new File(System.getProperty("user.home"));
            script = new File(basePath, "script.txt");
            saveTo = new File(basePath, "corpus/");
            preference = new File(basePath, "preference/");

            normalizationLevel = 0.8;
            splitChars = "[\t,:]";
        }
        if (!saveTo.exists()) {
            saveTo.mkdirs();
        }
        isNormalize = (0.05 <= AppConfig.normalizationLevel
            && AppConfig.normalizationLevel <= 0.95);
    }

    public AppConfig() {
        super("Preferences (read-only)");

        // Build string
        final int fs = (int) format.getSampleRate();
        final int nbits = format.getSampleSizeInBits();
        final int channels = format.getChannels();
        final StringBuilder sb = new StringBuilder();
        sb.append("Sampling rate (Fs)\n");
        sb.append(">> " + fs + " [Hz]\n");
        sb.append("\n");
        sb.append("Bit depth (nBits)\n");
        sb.append(">> " + nbits + " [bit]\n");
        sb.append("\n");
        sb.append("Channels\n");
        sb.append(">> " + channels + "\n");
        sb.append("\n");
        sb.append("Normalization\n");
        if (isNormalize) {
            sb.append(">> " + isNormalize + " (" + normalizationLevel + ")\n");
        } else {
            sb.append(">> " + isNormalize + "\n");
        }
        sb.append("\n");
        sb.append("Script file\n");
        sb.append(">> " + AppConfig.script + "\n");
        sb.append("\n");
        sb.append("Preference sound folder\n");
        sb.append(">> " + AppConfig.preference + "\n");
        sb.append("\n");
        sb.append("Save to...\n");
        sb.append(">> " + AppConfig.saveTo);
        
        final JTextArea textArea = new JTextArea(sb.toString());
        Utility.setTextAreaSetting(textArea);
        final int blank = 20;
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(blank, blank, blank, blank));
        textArea.setColumns(Constant.textAreaWidth);
        Utility.changeFont(textArea);
        this.add(textArea);

        // Window setting
        this.pack();
        this.setResizable(false);
        this.setMinimumSize(getSize());
        this.setLocationRelativeTo(null);
        this.setVisible(false);
    }

    public static final File getSavePath(final int index) {
        final int num = index + 1;
        final var fileString = "corpus_" + String.format("%04d", num) + ".wav";
        return new File(saveTo, fileString);
    }
}
