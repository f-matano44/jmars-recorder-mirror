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

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


final class ScriptsManager {
    private final List<String> lines = readFile();
    public final int minOfIndex = 0;
    public final int maxOfIndex = lines.size() - 1;
    public final int minOfLabel = 1;
    public final int maxOfLabel = lines.size();
    public static int currentIndex = 0;


    public class ScriptPanel extends JScrollPane {
        private final JTextArea scriptTextArea = new JTextArea();

        public ScriptPanel() {
            Util.setTextViewerSetting(scriptTextArea);
            scriptTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
            this.setViewportView(scriptTextArea);
            this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.setBorder(new LineBorder(Color.BLACK, Main.lineBorderThickness));
            // this.scriptTextArea.setRows(rows);
            // this.scriptTextArea.setColumns(Main.textAreaWidth);
        }

        public void updateText(final String str) {
            scriptTextArea.setText(str);
        }

        public void updateColor(final Color color) {
            scriptTextArea.setBackground(color);
        }
    }


    public class IndexSlider extends JSlider {
        public IndexSlider() {
            this.setMinimum(minOfIndex);
            this.setMaximum(maxOfIndex);
        }

        public void updateIndex() {
            ScriptsManager.currentIndex = this.getValue();
        }

        public void updateValue() {
            this.setValue(ScriptsManager.currentIndex);
        }
    }


    public class IndexLabel extends JTextField {
        public IndexLabel() {
            super("0 / 0");
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setFocusable(true);
            this.setBorder(new LineBorder(Color.BLACK, Main.lineBorderThickness));
        }

        public void updateIndexNumber() {
            final int currentIndex = ScriptsManager.currentIndex;
            try {
                final String[] inputSt = this.getText().replace(" ", "").split("/");
                final int ansIndex = Integer.parseInt(inputSt[0]) - 1;
                if (ansIndex < minOfIndex || maxOfIndex < ansIndex) {
                    throw new Exception("Too small or too big.");
                }
                ScriptsManager.currentIndex = ansIndex;
            } catch (final Exception e) {
                ScriptsManager.currentIndex = currentIndex;
            }
        }

        public void updateValue() {
            final boolean isRecording = RecorderBody.isRecording();
            this.setText(isRecording
                ? "** RECORDING **"
                : ((ScriptsManager.currentIndex + 1) + " / " + maxOfLabel));
            this.setEditable(!isRecording);
            this.setFocusable(!isRecording);
            this.setForeground(isRecording ? Color.WHITE : Color.BLACK);
            this.setBackground(isRecording ? Color.RED : null);
        }
    }


    public final void nextLine() {
        final int nextIndex = currentIndex + 1;
        currentIndex = Math.min(nextIndex, maxOfIndex);
    }

    public final String getScriptText() {
        return lines.get(ScriptsManager.currentIndex);
    }

    private static final List<String> readFile() {
        final List<String> lines = new ArrayList<>();

        try (
            final Scanner sc = new Scanner(
                new FileInputStream(AppConfig.script),
                StandardCharsets.UTF_8.name())
            ) {
            while (sc.hasNextLine()) {
                final String str = sc.nextLine();
                if (!str.isEmpty()) {
                    lines.add(str
                        .replace(":", System.lineSeparator())
                        .replace(",", System.lineSeparator())
                    );
                }
            }
        } catch (IOException e) {
            // StackTrace to String
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            final String stacktrace = sw.toString();

            lines.clear();
            lines.add(
                "Error: Can't read script file.\n"
                + "\n"
                + stacktrace
            );
        }

        if (lines.isEmpty()) {
            lines.clear();
            lines.add("Error: Script file is empty.");
        }

        return lines;
    }
}
