package jp.f_matano44.mreccorpus2;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileNameExtensionFilter;


class Recorder extends JPanel {
    private DataClass dat;
    private TargetDataLine line;
    private ByteArrayOutputStream out;
    private boolean isRecording;
    private final String startButtonString = "start recording";
    private final String recordingString =   "recording......";

    public Recorder(DataClass dat) {
        this.dat = dat;
        isRecording = false;

        // combo box of sampling rate
        final Integer[] samplingRates = {8000, 16000, 32000, 44100, 48000, 96000};
        final JComboBox<Integer> samplingRateBox = new JComboBox<>(samplingRates);
        samplingRateBox.setSelectedIndex(3);

        // combo box of bit depth
        final Integer[] bitDepths = {16, 24, 32, 48, 96};
        final JComboBox<Integer> bitSizeBox = new JComboBox<>(bitDepths);
        bitSizeBox.setSelectedIndex(1);

        // save as wav
        final JButton saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        final File[] lastSelectedDir = new File[]{
            new File(System.getProperty("user.home"))
        };
        saveButton.addActionListener(e -> saveAsWav(lastSelectedDir));

        // play button
        final JButton playButton = new JButton("Play");
        playButton.setEnabled(false);
        playButton.addActionListener(e -> playRecording());

        // recording button
        final JToggleButton toggleButton = new JToggleButton(startButtonString);
        // Listener
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final float fs = (float) (Integer) samplingRateBox.getSelectedItem();
                final int nbits = (Integer) bitSizeBox.getSelectedItem();
                dat.setFormat(fs, nbits, 1);

                try {
                    if (!isRecording) {
                        startRecording();
                        toggleButton.setText(recordingString);
                    } else {
                        stopRecording();
                        toggleButton.setText(startButtonString);
                    }
                    // ボタンを有効化 or 無効化
                    samplingRateBox.setEnabled(isRecording);
                    bitSizeBox.setEnabled(isRecording);
                    playButton.setEnabled(isRecording);
                    saveButton.setEnabled(isRecording);
                    isRecording = !isRecording;
                } catch (Exception ex) {
                    stopRecording();
                    toggleButton.setText(startButtonString);
                    playButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    toggleButton.setSelected(false);
                    isRecording = false;
                    JOptionPane.showMessageDialog(
                        null, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // panel setting -----------------------------------------------------------------
        final JPanel fsPanel = new JPanel();
        fsPanel.setLayout(new GridLayout(1, 1));
        fsPanel.add(new JLabel("Sampling rate: "));
        fsPanel.add(samplingRateBox);

        final JPanel nbitsPanel = new JPanel();
        nbitsPanel.setLayout(new GridLayout(1, 1));
        nbitsPanel.add(new JLabel("Encoding bit rate: "));
        nbitsPanel.add(bitSizeBox);

        final JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.add(fsPanel);
        configPanel.add(nbitsPanel);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(playButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(saveButton);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(configPanel);
        mainPanel.add(buttonPanel);
        add(mainPanel);
    }


    private void saveAsWav(final File[] lastSelectedDir){
        final JFileChooser fileChooser = new JFileChooser(lastSelectedDir[0]) {
            @Override
            public void approveSelection() {
                final File file = getSelectedFile();
                if (!file.getName().endsWith(".wav")) {
                    setSelectedFile(new File(file.toString() + ".wav"));
                }
                super.approveSelection();
            }
        };

        fileChooser.setFileFilter(
            new FileNameExtensionFilter(
                "WAV files", "wav"
            )
        );

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            lastSelectedDir[0] = file.getParentFile();

            final byte[] aDat = dat.getByteSignal();
            final AudioFormat format = dat.getFormat();
            try (
                AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(aDat),
                    format, aDat.length / format.getFrameSize()
                )
            ){
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    private void startRecording() throws Exception {
        final AudioFormat format = dat.getFormat();
        final int baseBufferSize = 10240;
        final int frameSize = format.getFrameSize();
        final int bufferSize = (baseBufferSize / frameSize) * frameSize;

        out = new ByteArrayOutputStream();

        // recording main
        line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();
        new Thread(() -> {
            byte[] buffer = new byte[bufferSize];
            while (line.isOpen()) {
                int count = line.read(buffer, 0, buffer.length);
                if (0 < count) {
                    out.write(buffer, 0, count);
                }
            }
        }).start();
    }

    private void stopRecording() {
        line.close();
        dat.setSignal(out);
    }

    private void playRecording() {
        final AudioFormat format = dat.getFormat();
        final byte[] audio = dat.getByteSignal();

        try (
            final var ais = new AudioInputStream(
                new ByteArrayInputStream(audio), 
                format, audio.length / format.getFrameSize());
        ) {
            // 再生のみで終了は観測しない (特にする意味がない)
            final Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
