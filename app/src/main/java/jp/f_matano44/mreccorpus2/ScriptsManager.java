/*
 * mRecCorpus2
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

package jp.f_matano44.mreccorpus2;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


final class ScriptsManager extends JPanel {
    private final RecorderBody recorder;
    private final int[] currentIndex;
    private static final int textAreaWidth = 60;
    private final List<String> lines;

    public final JTextArea scriptsPathViewer;
    public final JTextArea scriptViewer;
    public final JButton prevButton;
    public final JLabel indexLabel;
    public final JButton nextButton;
    public final JTextArea saveToViewer;

    public ScriptsManager(
        RecorderBody recorder, AppConfig conf, int[] currentIndex
    ) {
        this.recorder = recorder;
        this.currentIndex = currentIndex;

        // Index label
        this.indexLabel = new JLabel();
        this.indexLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Control button
        prevButton = new JButton("<< Prev");
        prevButton.addActionListener((ActionEvent e) -> this.prevLine());
        nextButton = new JButton("Next >>");
        nextButton.addActionListener((ActionEvent e) -> this.nextLine());

        // Script viewer
        this.scriptViewer = new JTextArea();
        setTextAreaSetting(this.scriptViewer);
        this.scriptViewer.setBorder(
            new LineBorder(Color.BLACK, 1, true)
        );
        this.scriptViewer.setColumns(textAreaWidth);
        this.scriptViewer.setRows(7); // ここの数字は決め打ち

        // saveTo Panel
        this.saveToViewer = new JTextArea();
        setTextAreaSetting(this.saveToViewer);
        this.saveToViewer.setColumns(textAreaWidth);

        // Scripts loader
        final String scriptPathString = conf.scripts.getAbsolutePath();
        this.lines = new ArrayList<>();
        readFile(scriptPathString);
        this.scriptsPathViewer = new JTextArea(
            "Scripts: " + scriptPathString
        );
        setTextAreaSetting(this.scriptsPathViewer);
        this.scriptsPathViewer.setColumns(textAreaWidth);
        this.scriptsPathViewer.setBorder(
            new EmptyBorder(0, 0, 5, 0)
        );

        // set default value
        this.updateText();
    }

    private final void nextLine() {
        currentIndex[0]++;
        if (lines.size() <= currentIndex[0]) {
            currentIndex[0] = 0;
        }
        updateText();
    }

    private final void prevLine() {
        if (currentIndex[0] <= 0) {
            currentIndex[0] = lines.size();
        }
        currentIndex[0]--;
        updateText();
    }

    private final void updateText() {
        final int num = currentIndex[0] + 1;
        final String saveToString = recorder.getSavePath(num).getAbsolutePath();
        indexLabel.setText(String.valueOf(num));
        scriptViewer.setText(lines.get(currentIndex[0]));
        saveToViewer.setText("Save to: " + saveToString);
    }

    private final void readFile(final String filePath) {
        try (final var sc = new Scanner(new File(filePath))) {
            while (sc.hasNextLine()) {
                final String[] parts = sc.nextLine().split("[\t,:]");
                final StringBuilder sb = new StringBuilder();
                for (final String temp : parts) {
                    sb.append(temp + "\n");
                }
                lines.add(sb.toString());
            }
        } catch (IOException e) {
            final String errorString = "Error: Can't read script file.";
            lines.clear();
            lines.add(errorString);
        }

        if (!lines.isEmpty()) {
            updateText();
        } else {
            scriptViewer.setText("No lines in the file");
        }
    }

    private static final void setTextAreaSetting(JTextArea textArea) {
        textArea.setWrapStyleWord(true);   
        textArea.setLineWrap(true);        
        textArea.setEditable(false);   
        textArea.setBackground(null);
        textArea.setBorder(null);
    }
}
