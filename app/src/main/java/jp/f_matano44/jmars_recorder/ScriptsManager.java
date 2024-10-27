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
    private final String[] lines;
    public final int minOfIndex;
    public final int maxOfIndex;
    public final int minOfLabel;
    public final int maxOfLabel;
    private int currentIndex = 0;


    // MARK: Constructor
    public ScriptsManager() {
        final List<String> linesList = new ArrayList<String>();

        try (
            final Scanner sc = new Scanner(
                new FileInputStream(AppConfig.script),
                StandardCharsets.UTF_8.name())
            ) {
            while (sc.hasNextLine()) {
                final String str = sc.nextLine();
                if (!str.isEmpty()) {
                    linesList.add(str
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

            linesList.clear();
            linesList.add(
                "Error: Can't read script file.\n"
                + "\n"
                + stacktrace
            );
        }

        if (linesList.isEmpty()) {
            linesList.clear();
            linesList.add("Error: Script file is empty.");
        }

        lines = linesList.toArray(new String[linesList.size()]);
        minOfIndex = 0;
        maxOfIndex = lines.length - 1;
        minOfLabel = 1;
        maxOfLabel = lines.length;
    }


    // MARK: Child classes
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
            currentIndex = this.getValue();
        }

        public void updateValue() {
            this.setValue(currentIndex);
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
            final int tempIndex = currentIndex;
            try {
                final String[] inputSt = this.getText().replace(" ", "").split("/");
                final int ansIndex = Integer.parseInt(inputSt[0]) - 1;
                if (ansIndex < minOfIndex || maxOfIndex < ansIndex) {
                    throw new Exception("Too small or too big.");
                }
                currentIndex = ansIndex;
            } catch (final Exception e) {
                e.printStackTrace();
                currentIndex = tempIndex;
            }
        }

        public void updateValue() {
            final boolean isRecording = RecorderBody.isRecording();
            this.setText(isRecording
                ? "** RECORDING **"
                : ((currentIndex + 1) + " / " + maxOfLabel));
            this.setEditable(!isRecording);
            this.setFocusable(!isRecording);
            this.setForeground(isRecording ? Color.WHITE : Color.BLACK);
            this.setBackground(isRecording ? Color.RED : null);
        }
    }


    // MARK: Methods
    public final void nextLine() {
        final int nextIndex = currentIndex + 1;
        currentIndex = Math.min(nextIndex, maxOfIndex);
    }

    public final String getScriptText() {
        return lines[this.currentIndex];
    }

    public final int getCurrentIndex() {
        return this.currentIndex;
    }
}
