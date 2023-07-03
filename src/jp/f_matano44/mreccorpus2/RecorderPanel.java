package jp.f_matano44.mreccorpus2;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

class RecorderPanel extends JPanel {
        private final RecorderBody recorder;
        private final FileInOutPanel fileio;

        private final JComboBox<Integer> samplingRateBox;
        private final JComboBox<Integer> bitSizeBox;
        private final JComboBox<Integer> channelBox;
        private final JButton saveButton;
        private final JButton playButton;
        private final JToggleButton toggleButton;

        private final String startButtonString = "Start Recording";
        private final String recordingString = "Recording......";
        private boolean isRecording;

        public RecorderPanel(
            RecorderBody recorder,
            FileInOutPanel fileio
        ) {
            this.recorder = recorder;
            this.fileio = fileio;
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
            final Integer[] channels = {1, 2};
            channelBox = new JComboBox<>(channels);
            channelBox.setSelectedIndex(0);

            // save as wav
            saveButton = fileio.saveButton;

            // play button
            playButton = new JButton("Play");

            // recording button
            toggleButton = new JToggleButton(startButtonString);
        }


        public JPanel ConfigPanel() {
            final JPanel builder = new JPanel(new GridLayout(3, 1));
            final int innerRows = 2;
            final int innerCols = 1;
            final int blankSide = 5;
            final int blankBottom = 5;
            final EmptyBorder[] blank = new EmptyBorder[3];
            blank[0] = new EmptyBorder(0, blankSide, 0, blankSide);
            blank[1] = new EmptyBorder(0, blankSide, 0, blankSide);
            blank[2] = new EmptyBorder(0, blankSide, blankBottom, blankSide);

            final JPanel[] cells = new JPanel[3];

            cells[0] = new JPanel(new GridLayout(innerRows, innerCols));
            cells[0].add(new JLabel("Sampling Rate [Hz]"));
            cells[0].add(samplingRateBox);
            cells[0].setBorder(blank[0]);

            cells[1] = new JPanel(new GridLayout(innerRows, innerCols));
            cells[1].add(new JLabel("Bit Rate [bit]"));
            cells[1].add(bitSizeBox);
            cells[1].setBorder(blank[1]);

            cells[2] = new JPanel(new GridLayout(innerRows, innerCols));
            cells[2].add(new JLabel("Channels"));
            cells[2].add(channelBox);
            cells[2].setBorder(blank[2]);

            builder.add(cells[0]);
            builder.add(cells[1]);
            builder.add(cells[2]);
            builder.setBorder(
                BorderFactory.createTitledBorder("Config Panel")
            );

            return builder;
        }


        public JPanel ControlPanel() {
            // play button
            playButton.setEnabled(false);
            playButton.addActionListener(e -> recorder.playSignal());

            // recording button
            toggleButton.addActionListener(e -> recordButtonAction());

            // save button
            saveButton.setEnabled(false);
            saveButton.addActionListener(e -> fileio.saveAsWav());

            final JPanel builder = new JPanel();
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
