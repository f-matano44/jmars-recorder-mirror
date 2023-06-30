package jp.f_matano44.mreccorpus2;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

class RecorderPanel extends JPanel {
        private final RecorderBody recorder;
        private boolean isRecording;
        private final JComboBox<Integer> samplingRateBox;
        private final JComboBox<Integer> bitSizeBox;
        private final JComboBox<Integer> channelBox;

        private final String startButtonString = "start recording";
        private final String recordingString =   "recording......";
        private final JButton saveButton;
        private final JButton playButton;
        private final JToggleButton toggleButton;

        public RecorderPanel(RecorderBody recorder) {
            this.recorder = recorder;
            isRecording = false;

            // combo box of sampling rate
            final Integer[] samplingRates = {8000, 16000, 32000, 44100, 48000, 96000};
            samplingRateBox = new JComboBox<>(samplingRates);
            samplingRateBox.setSelectedIndex(3);

            // combo box of bit depth
            final Integer[] bitDepths = {16, 24, 32, 48, 96};
            bitSizeBox = new JComboBox<>(bitDepths);
            bitSizeBox.setSelectedIndex(1);

            // combo box of channels
            final Integer[] channels = {1};
            channelBox = new JComboBox<>(channels);
            channelBox.setSelectedIndex(0);

            // save as wav
            saveButton = new JButton("Save");

            // play button
            playButton = new JButton("Play");

            // recording button
            toggleButton = new JToggleButton(startButtonString);
        }


        public JPanel ConfigPanel() {
            final JPanel fsPanel = new JPanel();
            fsPanel.setLayout(new GridLayout(1, 1));
            fsPanel.add(new JLabel("Sampling rate: "));
            fsPanel.add(samplingRateBox);

            final JPanel nbitsPanel = new JPanel();
            nbitsPanel.setLayout(new GridLayout(1, 1));
            nbitsPanel.add(new JLabel("Encoding bit rate: "));
            nbitsPanel.add(bitSizeBox);
            
            final JPanel channelsPanel = new JPanel();
            channelsPanel.setLayout(new GridLayout(1, 1));
            channelsPanel.add(new JLabel("Channels: "));
            channelsPanel.add(channelBox);
            
            final JPanel builder = new JPanel();
            builder.setLayout(new BoxLayout(builder, BoxLayout.Y_AXIS));
            builder.add(fsPanel);
            builder.add(nbitsPanel);
            builder.add(channelsPanel);
            return builder;
        }


        public JPanel ControlPanel() {
            final String homedir = System.getProperty("user.home");
            final File[] lastSelectedDir = new File[]{new File(homedir)};
            saveButton.setEnabled(false);
            saveButton.addActionListener(e -> recorder.saveAsWav(lastSelectedDir));

            // play button
            playButton.setEnabled(false);
            playButton.addActionListener(e -> recorder.playSignal());

            // recording button
            toggleButton.addActionListener(e -> recordButtonAction());

            JPanel builder = new JPanel();
            builder.setLayout(new BoxLayout(builder, BoxLayout.X_AXIS));
            builder.add(playButton);
            builder.add(toggleButton);
            builder.add(saveButton);
            return builder;
        }


        private void recordButtonAction() {
            final float fs = (float) (Integer) samplingRateBox.getSelectedItem();
            final int nbits = (Integer) bitSizeBox.getSelectedItem();
            final int channels = (Integer) channelBox.getSelectedItem();

            try {
                if (!isRecording) {
                    recorder.startRecording(fs, nbits, channels);
                    toggleButton.setText(recordingString);
                } else {
                    recorder.stopRecording();
                    toggleButton.setText(startButtonString);
                }
                // ボタンを有効化 or 無効化
                samplingRateBox.setEnabled(isRecording);
                bitSizeBox.setEnabled(isRecording);
                channelBox.setEnabled(isRecording);
                playButton.setEnabled(isRecording);
                saveButton.setEnabled(isRecording);
                isRecording = !isRecording;
            } catch (Exception ex) {
                recorder.stopRecording();
                toggleButton.setText(startButtonString);
                samplingRateBox.setEnabled(true);
                bitSizeBox.setEnabled(true);
                channelBox.setEnabled(true);
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
