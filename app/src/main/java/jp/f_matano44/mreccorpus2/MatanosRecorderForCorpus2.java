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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Main-Class. */
public final class MatanosRecorderForCorpus2 extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MatanosRecorderForCorpus2());
    }

    final int[] currentIndex = {0};
    final AppConfig conf = new AppConfig();
    final RecorderBody recorder = new RecorderBody(conf, currentIndex);
    final ScriptsManager sm = new ScriptsManager(recorder, conf, currentIndex);

    private MatanosRecorderForCorpus2() {
        // Window title
        super("mRecCorpus2");

        // set menu-bar
        TopBarMenu menuBar = new TopBarMenu(conf);
        this.setJMenuBar(menuBar);

        // main panel setting
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        final EmptyBorder blank = new EmptyBorder(15, 20, 15, 20);
        mainPanel.setBorder(blank);

        final JPanel[] xPanels = new JPanel[2];
        xPanels[0] = new JPanel(new GridBagLayout());
        xPanels[1] = new JPanel(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        xPanels[1].add(sm.scriptsPathViewer, gbc);
        gbc.gridy++;
        xPanels[1].add(sm.scrollPane, gbc);
        gbc.gridy++;
        xPanels[1].add(sm.indexSlider, gbc);
        gbc.gridy++;
        xPanels[1].add(this.new ScriptChooserPanel(), gbc);
        gbc.gridy++;
        xPanels[1].add(this.new RecorderPanel(recorder), gbc);
        gbc.gridy++;
        xPanels[1].add(recorder.sse, gbc);
        gbc.gridy++;
        xPanels[1].add(recorder.saveToViewer, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(xPanels[1], gbc);
        changeFont(mainPanel, AppConfig.fontSet);
        this.add(mainPanel);

        // Window setting
        this.pack();
        this.setMinimumSize(getSize());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }


    private final class ScriptChooserPanel extends JPanel {
        public ScriptChooserPanel() { // GridBagLayout は NG
            this.setLayout(new GridLayout(1, 3));
            this.add(sm.prevButton);
            this.add(sm.indexLabel);
            this.add(sm.nextButton);
        }
    }


    private final class RecorderPanel extends JPanel {
        public RecorderPanel(RecorderBody recorder) {
            recorder.recordButton.setText(RecorderBody.startButtonString);
            recorder.recordButton.addActionListener(
                (ActionEvent e) -> this.recordButtonAction()
            );

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(recorder.recordButton);
            this.add(recorder.playButton);
        }

        private final void recordButtonAction() {
            try {
                if (!recorder.isRecording) {
                    recorder.isRecording = true;
                    recorder.recordButton.setText(RecorderBody.recordingString);
                    recorder.startRecording();
                } else {
                    recorder.stopRecording();
                    recorder.recordButton.setText(RecorderBody.startButtonString);
                    recorder.isRecording = false;
                }
                // ボタンを有効化 or 無効化
                recorder.playButton.setEnabled(!recorder.isRecording);
                sm.prevButton.setEnabled(!recorder.isRecording);
                sm.nextButton.setEnabled(!recorder.isRecording);
            } catch (Exception e) {
                recorder.enforceStopRecording(e);
            }
        }
    }


    private static final void changeFont(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                changeFont(child, font);
            }
        }
    }
}
