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

import java.awt.Dimension;
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
import jp.f_matano44.jmars_recorder.MyClasses.MyStringBuilder;
import jp.f_matano44.jmars_recorder.MyClasses.UneditableTextArea;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


final class AppConfig extends JFrame {
    public static final AudioFormat format;
    public static final File script;
    public static final File reference;
    public static final File saveTo;
    public static final boolean isTrimming;
    public static final PrintStream logTargetStream = System.out;
    public static final float fontSize = 15f;
    public static final float scriptFontSize = fontSize + 1;
    private static final String confFileName = "." + AppInfo.name + ".yaml";

    static {
        final String fsKey = "sample_rate";
        final String nbitsKey = "bit_depths";
        final String scriptKey = "script_file_path";
        final String referenceKey = "reference_sound_folder";
        final String saveToKey = "save_to_folder";
        final String isTrimmingKey = "trimming";

        final File basePath = AppInfo.os.contains("win") || AppInfo.os.contains("mac")
            ? new File(System.getProperty("user.home"), "Desktop/" + AppInfo.name)
            : new File(System.getProperty("user.home"), AppInfo.name);

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
            final Map<Object, Object>
                conf = new Yaml().load(new FileInputStream(confFile));

            final int fs = (Integer) conf.get(fsKey);
            final int nbits = (Integer) conf.get(nbitsKey);
            final int channels = 1;
            fo = new AudioFormat(fs, nbits, channels, true, false);
            sc = new File((String) conf.get(scriptKey));
            re = new File((String) conf.get(referenceKey));
            sa = new File((String) conf.get(saveToKey));
            tr = (Boolean) conf.get(isTrimmingKey);
        } catch (final FileNotFoundException e) {
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
            } catch (final Exception ex) {
                unexpectedError(ex);
            }
        } catch (final NullPointerException | ClassCastException e) {
            e.printStackTrace(logTargetStream);
            final String[] messages = {
                "Configuration file (${HOME}/" + confFileName + ") is corrupted.",
                "Therefore, this application will start in default setting.",
                "",
                "If you wish to resolve this issue, delete the config file",
                "and restart the application. Then, this app will start correctly."
            };
            final MyStringBuilder sb = new MyStringBuilder();
            for (final String m : messages) {
                sb.appendLn(m);
            }
            JOptionPane.showMessageDialog(
                null, sb.toString(),
                "Error", JOptionPane.ERROR_MESSAGE
            );

            fo = new AudioFormat(defaultFs, defaultNbits, 1, true, false);
            sc = defaultScript;
            re = defaultReference;
            sa = defaultSaveTo;
            tr = defaultTrimming;
        } catch (final Exception e) {
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
        final MyStringBuilder sb = new MyStringBuilder();
        sb.appendLn("If you want to change configuration,");
        sb.appendLn("edit `{HOME}/" + confFileName + "`.");
        sb.appendLn("");
        sb.appendLn("Sampling rate (Fs)");
        sb.appendLn(">> " + AppConfig.format.getSampleRate() + " [Hz]");
        sb.appendLn("");
        sb.appendLn("Bit depth (nBits)");
        sb.appendLn(">> " + AppConfig.format.getSampleSizeInBits() + " [bit]");
        sb.appendLn("");
        sb.appendLn("Channels");
        sb.appendLn(">> " + AppConfig.format.getChannels() + " (Cannot change)");
        sb.appendLn("");
        sb.appendLn("Script file");
        sb.appendLn(">> " + AppConfig.script);
        sb.appendLn("");
        sb.appendLn("Reference sound folder");
        sb.appendLn(">> " + AppConfig.reference);
        sb.appendLn("");
        sb.appendLn("Save to...");
        sb.appendLn(">> " + AppConfig.saveTo);
        sb.appendLn("");
        sb.appendLn("Trimming");
        sb.appendLn(">> " + AppConfig.isTrimming);

        final JTextArea textArea = new UneditableTextArea(sb.toString());
        Util.setFontRecursive(textArea, AppConfig.fontSize);
        textArea.setWrapStyleWord(false);
        final Dimension textAreaDimension = textArea.getPreferredSize();
        textAreaDimension.width = (int) (Main.panelWidth * 0.9);
        textArea.setPreferredSize(textAreaDimension);
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


    private static void unexpectedError(final Exception e) {
        final MyStringBuilder sb = new MyStringBuilder();

        sb.appendLn("An unexpected error has occurred in loading configure.");
        sb.appendLn("Please send the following information to the author:");
        sb.appendLn("OS information, Java version, the outputted StackTrace");
        sb.appendLn("and contents of the configuration file(${HOME}/" + confFileName + ").");
        sb.appendLn("");
        sb.appendLn("X/Twitter: @f_matano44");

        e.printStackTrace(logTargetStream);
        JOptionPane.showMessageDialog(
            null, sb.toString(),
            "Error", JOptionPane.ERROR_MESSAGE
        );

        System.exit(1);
    }
}
