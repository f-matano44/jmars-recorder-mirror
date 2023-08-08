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

import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

class RecorderPanel extends JPanel {
        private final RecorderBody recorder;

        private final JButton saveButton;
        private final JButton playButton;
        private final JToggleButton toggleButton;

        private final String startButtonString = "Start Recording";
        private final String recordingString = "Recording......";
        private boolean isRecording;

        public RecorderPanel(RecorderBody recorder) {
            this.recorder = recorder;
            isRecording = false;

            // save as wav
            saveButton = new JButton("Save");
            saveButton.setEnabled(false);
            saveButton.addActionListener((ActionEvent e) -> recorder.saveAsWav());

            // play button
            playButton = new JButton("Play");
            playButton.setEnabled(false);
            playButton.addActionListener(e -> recorder.playSignal());

            // recording button
            toggleButton = new JToggleButton(startButtonString);
            toggleButton.addActionListener(e -> recordButtonAction());

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(playButton);
            this.add(toggleButton);
            this.add(saveButton);
        }


        private void recordButtonAction() {
            try {
                if (!isRecording) {
                    recorder.startRecording();
                    toggleButton.setText(recordingString);
                } else {
                    recorder.stopRecording();
                    toggleButton.setText(startButtonString);
                }
                // ボタンを有効化 or 無効化
                playButton.setEnabled(isRecording);
                saveButton.setEnabled(isRecording);
                isRecording = !isRecording;
            } catch (Exception ex) {
                recorder.stopRecording();
                toggleButton.setText(startButtonString);
                toggleButton.setSelected(false);
                playButton.setEnabled(false);
                saveButton.setEnabled(false);
                isRecording = false;
                JOptionPane.showMessageDialog(
                    null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
