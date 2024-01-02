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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

final class AppConfig extends JFrame {
    public static final AudioFormat format;
    public static final File script;
    public static final File reference;
    public static final File saveTo;
    public static final boolean isTrimming;
    public static final PrintStream logTargetStream = System.out;
    private static final String confFileName = "." + Main.appName + ".yaml";

    static {
        final String fsKey = "sample_rate";
        final String nbitsKey = "bit_depths";
        final String scriptKey = "script_file_path";
        final String referenceKey = "reference_sound_folder";
        final String saveToKey = "save_to_folder";
        final String isTrimmingKey = "trimming";

        final String osName = System.getProperty("os.name").toLowerCase();
        final File basePath = osName.contains("win") || osName.contains("mac")
            ? new File(System.getProperty("user.home"), "Desktop/" + Main.appName)
            : new File(System.getProperty("user.home"), Main.appName);

        final int defaultFs = 48000;
        final int defaultNbits = 16;
        final File defaultScript = new File(basePath, "script.txt");
        final File defaultReference = new File(basePath, "reference/");
        final File defaultSaveTo = new File(basePath, "wav/");
        final boolean defaultTrimming = true;
    
        final File confFile = new File(System.getProperty("user.home"), confFileName);

        AudioFormat fo = null;
        File sc = null;
        File re = null;
        File sa = null;
        Boolean tr = null;
        try {    
            @SuppressWarnings("unchecked") // もう少し良い方法がありそう
            final Map<String, Object> conf = (Map<String, Object>) new Yaml().load(
                new FileInputStream(confFile)
            );

            final int fs = (Integer) conf.get(fsKey);
            final int nbits = (Integer) conf.get(nbitsKey);
            final int channels = 1;
            fo = new AudioFormat(fs, nbits, channels, true, false);
            sc = new File((String) conf.get(scriptKey));
            re = new File((String) conf.get(referenceKey));
            sa = new File((String) conf.get(saveToKey));
            tr = (Boolean) conf.get(isTrimmingKey);
        } catch (FileNotFoundException e) {
            fo = new AudioFormat(defaultFs, defaultNbits, 1, true, false);
            sc = defaultScript;
            re = defaultReference;
            sa = defaultSaveTo;
            tr = defaultTrimming;
            try (
                final Writer fw = new OutputStreamWriter(
                    new FileOutputStream(confFile), StandardCharsets.UTF_8)
            ) {
                final Map<String, Object> map = new HashMap<>();
                map.put(fsKey, defaultFs);
                map.put(nbitsKey, defaultNbits);
                map.put(scriptKey, defaultScript.toString());
                map.put(referenceKey, defaultReference.toString());
                map.put(saveToKey, defaultSaveTo.toString());
                map.put(isTrimmingKey, defaultTrimming);
                final DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                new Yaml(options).dump(map, fw); // write out
            } catch (Exception ex) {
                unexpectedError(ex);
            }
        } catch (NullPointerException | ClassCastException e) {
            e.printStackTrace(logTargetStream);
            String[] messages = {
                "Configuration file (${HOME}/" + confFileName + ") is corrupted.",
                "Therefore, this application will start in default setting.",
                "",
                "If you wish to resolve this issue, delete the config file",
                "and restart the application. Then, this app will start correctly."
            };
            StringBuilder message = new StringBuilder();
            for (String m : messages) {
                Util.appendLn(message, m);
            }
            JOptionPane.showMessageDialog(
                null, message, "Error", JOptionPane.ERROR_MESSAGE
            );

            fo = new AudioFormat(defaultFs, defaultNbits, 1, true, false);
            sc = defaultScript;
            re = defaultReference;
            sa = defaultSaveTo;
            tr = defaultTrimming;
        } catch (Exception e) {
            unexpectedError(e);
        }
        format = fo;
        script = sc;
        reference = re;
        saveTo = sa;
        isTrimming = tr;

        if (!saveTo.exists() && !reference.exists() && !script.exists()) {
            final File scriptParent = new File(script.getParent());
            if (!scriptParent.exists()) {
                scriptParent.mkdir();
            }

            try {
                Util.copyResource(
                    "ENDSVILLE400.txt",
                    script.toString()
                );
            } catch (final Exception e) {
                e.printStackTrace(logTargetStream);
            }
        }

        if (!reference.exists()) {
            reference.mkdirs();
        }

        if (!saveTo.exists()) {
            saveTo.mkdirs();
        }
    }

    public AppConfig() {
        super("Configuration (read-only)");

        // Build string
        final StringBuilder sb = new StringBuilder();
        Util.appendLn(sb, "If you want to change configuration,");
        Util.appendLn(sb, "edit `{HOME}/" + confFileName + "`.");
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Sampling rate (Fs)");
        Util.appendLn(sb, ">> " + AppConfig.format.getSampleRate() + " [Hz]");
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Bit depth (nBits)");
        Util.appendLn(sb, ">> " + AppConfig.format.getSampleSizeInBits() + " [bit]");
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Channels");
        Util.appendLn(sb, ">> " + AppConfig.format.getChannels() + " (Cannot change)");
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Script file");
        Util.appendLn(sb, ">> " + AppConfig.script);
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Reference sound folder");
        Util.appendLn(sb, ">> " + AppConfig.reference);
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Save to...");
        Util.appendLn(sb, ">> " + AppConfig.saveTo);
        Util.appendLn(sb, "");
        Util.appendLn(sb, "Trimming");
        Util.appendLn(sb, ">> " + AppConfig.isTrimming);
        
        final JTextArea textArea = new JTextArea(sb.toString());
        Util.changeFont(textArea);
        Util.setTextAreaSetting(textArea);
        textArea.setWrapStyleWord(false);
        textArea.setEditable(false);
        textArea.setColumns(Main.textAreaWidth);
        final int blank = 20;
        textArea.setBorder(new EmptyBorder(blank, blank, blank, blank));
        final JScrollPane textPane = new JScrollPane(textArea);
        this.add(textPane);

        // Window setting
        this.pack();
        this.setResizable(true);
        this.setMinimumSize(getSize());
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static final File getSaveFile(final int index) {
        final int num = index + 1;
        final String fileString = "corpus_" + String.format("%04d", num) + ".wav";
        return new File(saveTo, fileString);
    }

    public static void unexpectedError(Exception e) {
        String[] messages = {
            "An unexpected error has occurred in loading configure.",
            "Please send the following information to the author:",
            "OS information, Java version, the outputted StackTrace",
            "and contents of the configuration file(${HOME}/" + confFileName + ").",
            "",
            "X/Twitter: @f_matano44"
        };
        final StringBuilder message = new StringBuilder();
        for (String m : messages) {
            Util.appendLn(message, m);
        }

        e.printStackTrace(logTargetStream);
        JOptionPane.showMessageDialog(
            null, message,
            "Error", JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }
}
