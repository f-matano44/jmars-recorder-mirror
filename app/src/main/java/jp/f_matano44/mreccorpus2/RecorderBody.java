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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.f_matano44.jfloatwavio.Converter;

final class RecorderBody {
    private final AppConfig conf;
    private final AudioFormat format;
    private final int[] currentIndex;

    public static final String startButtonString = "Start recording";
    public static final String recordingString   = " Stop and Save ";
    private TargetDataLine line;
    private ByteArrayOutputStream outStream;
    private byte[] signal;
    public boolean isRecording = false;

    public final JTextArea recInfoViewer;
    public final JButton playButton;
    public final JToggleButton recordButton;
    public final SpeechSectionEstimator sse;
    public final JTextArea saveToViewer;

    public RecorderBody(AppConfig conf, int[] currentIndex) {
        this.conf = conf;
        this.format = conf.format;
        this.currentIndex = currentIndex;

        // SNR viewer
        this.recInfoViewer = new JTextArea(
            getRecInfo("------", "------")
        );
        this.recInfoViewer.setEditable(false);   
        this.recInfoViewer.setBackground(null);
        this.recInfoViewer.setBorder(null);
        this.recInfoViewer.setLineWrap(false);
        this.recInfoViewer.setAutoscrolls(false);
        this.recInfoViewer.setColumns(60);

        // play button
        this.playButton = new JButton("Play");
        this.playButton.setEnabled(false);
        this.playButton.addActionListener((ActionEvent e) -> this.playSignal());

        // recording button
        this.recordButton = new JToggleButton();

        // Speech section
        this.sse = new SpeechSectionEstimator();

        // saveTo Panel
        this.saveToViewer = new JTextArea();
        ScriptsManager.setTextAreaSetting(this.saveToViewer);
        this.saveToViewer.setColumns(ScriptsManager.textAreaWidth);
    }

    public final File getSavePath(final int number) {
        return new File(
            conf.saveTo, 
            "corpus_" + String.format("%04d", number) + ".wav"
        );
    }

    public void update() {
        final int num = currentIndex[0] + 1;
        final String saveToString = this.getSavePath(num).getAbsolutePath();
        this.saveToViewer.setText("Save to: " + saveToString);
    }

    public final void startRecording() throws Exception {
        // determine buffer-size
        final int fs = (int) format.getSampleRate();
        final int nBits = format.getSampleSizeInBits();
        final int channels = format.getChannels();
        final double recordingTime = 0.05;
        final int baseBufferSize = (int) (fs * nBits * channels * recordingTime / 8.0);
        final int frameSize = format.getFrameSize();
        final int bufferSize = (int) (baseBufferSize / frameSize) * frameSize;

        this.outStream = new ByteArrayOutputStream();
        line = AudioSystem.getTargetDataLine(this.format);
        line.open(this.format);
        line.start();
        new Thread(() -> {
            final byte[] buffer = new byte[bufferSize];
            while (line.isOpen()) {
                final int count = line.read(buffer, 0, buffer.length);
                if (0 < count) {
                    outStream.write(buffer, 0, count);
                }
            }
        }).start();
    }

    public final void stopRecording() {
        if (line != null) {
            this.line.close();
            final byte[] byteSignal = this.conf.isNormalize
                ? normalization(this.outStream.toByteArray(), conf)
                : this.outStream.toByteArray();
            try {
                final double snr = getSignalNoiseRatio(byteSignal, conf.format);
                this.recInfoViewer.setText(getRecInfo(
                    String.format("%.4f", snr),
                    String.valueOf(isClip(this.outStream.toByteArray()))
                ));
            } catch (ArithmeticException e) {
                this.recInfoViewer.setText(getRecInfo(
                    "-Inf",
                    String.valueOf(isClip(this.outStream.toByteArray()))
                ));
            } catch (Exception e) {
                return;
            }

            this.signal = byteSignal;
            this.sse.update();
            this.saveAsWav();
        }
    }

    public final void enforceStopRecording(Exception e) {
        if (this.line != null) {
            this.line.close();
        }
        this.signal = null;
        this.recordButton.setText(startButtonString);
        this.recordButton.setSelected(false);
        this.playButton.setEnabled(false);
        this.isRecording = false;
        this.recInfoViewer.setText(getRecInfo(
            "------", 
            "------"
        ));
        sse.reset();
        JOptionPane.showMessageDialog(
            null, e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE
        );
    }

    private final void playSignal() {
        final AudioFormat format = this.format;
        final byte[] audio = this.signal;

        try (
            final var ais = new AudioInputStream(
                new ByteArrayInputStream(audio), 
                format, audio.length / format.getFrameSize());
        ) {
            // 再生終了は観測しない (特にする意味がない)
            final Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private final static byte[] normalization(byte[] byteSignal, AppConfig conf) {
        // get format
        final int nBits = conf.format.getSampleSizeInBits();
        final boolean isBigEndian = conf.format.isBigEndian();
        try {
            final double[] dSignal = 
                Converter.byte2double(byteSignal, nBits, isBigEndian);
            // seek max value of signal
            final double[] dSignalAbs = new double[dSignal.length]; 
            for (int i = 0; i < dSignal.length; i++) {
                dSignalAbs[i] = Math.abs(dSignal[i]);
            }
            final double max = Arrays.stream(dSignalAbs).max().getAsDouble();
            // exec normalization
            final double level = conf.normalizationLevel;
            for (int i = 0; i < dSignal.length; i++) {
                dSignal[i] /= max;
                dSignal[i] *= level; 
            }
            return Converter.double2byte(dSignal, nBits, isBigEndian);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private final static double getSignalNoiseRatio(
        byte[] byteSignal, AudioFormat format
    ) {
        // get signal format
        final int nBits = format.getSampleSizeInBits();
        final boolean isBigEndian = format.isBigEndian();
        // set Window size (width: 150ms, shift: 75ms)
        final double width = 0.150;  // [s] = 150 [ms]
        final double shift = 0.075; // [s] =  75 [ms]
        final double fs = format.getSampleRate();
        final double t = 1 / fs;
        final int windowSize = (int) (width / t);
        final int shiftSize = (int) (shift / t);

        final double[] dSignal = Converter.byte2double(byteSignal, nBits, isBigEndian);
        final List<Double> power = new ArrayList<>();
        try {
            for (int i = 0; i < dSignal.length; i += shiftSize) {
                double thisPower = 0.0;
                for (int j = 0; j < windowSize; j++) {
                    thisPower += Math.pow(dSignal[i + j], 2);
                }
                thisPower /= windowSize;
                power.add(thisPower);
            }
        } catch (IndexOutOfBoundsException e) {
            // Finish!!!
        }

        final double signalPower = Collections.max(power);
        final double noisePower = Collections.min(power);
        // System.out.println("S " + signalPower);
        // System.out.println("N " + noisePower);

        if (noisePower == 0) {
            throw new ArithmeticException();
        } else {
            return 10 * Math.log10(signalPower / noisePower);
        }
    }

    private boolean isClip(byte[] signal) {
        // get signal format
        final int nBits = conf.format.getSampleSizeInBits();
        final boolean isBigEndian = conf.format.isBigEndian();
        // get double signal
        final double[] dSignal = Converter.byte2double(signal, nBits, isBigEndian);
        for (int i = 0; i < dSignal.length; i++) {
            dSignal[i] = Math.abs(dSignal[i]);
        }

        final double max = Arrays.stream(dSignal).max().getAsDouble();
        return 0.99 < max ? true : false;
    }

    private final void saveAsWav() {
        final File file = this.getSavePath(this.currentIndex[0] + 1);
        final byte[] audio = this.signal;

        try (
            final AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audio),
                this.format, audio.length / this.format.getFrameSize()
            )
        ) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static final String getRecInfo(String snr, String clip) {
        return "S/N: " + snr + "[dB] / Cliping: " + clip;
    }

    private final class SpeechSectionEstimator extends JPanel {
        private static final int defaultMin = 0;
        private static final int defaultMax = 100000;
        private static final int defaultStart = 30000;
        private static final int defaultEnd = 70000;
        private final JSlider startSlider;
        private final JSlider endSlider;
        private final SignalPanel sPanel;
    
        public SpeechSectionEstimator() {
            startSlider = new JSlider(
                JSlider.HORIZONTAL, defaultMin, defaultMax, defaultStart);
            startSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (endSlider.getValue() <= startSlider.getValue()) {
                        endSlider.setValue(startSlider.getValue() + 1);
                    }
                }
            });
            endSlider = new JSlider(
                JSlider.HORIZONTAL, defaultMin, defaultMax, defaultEnd);
            endSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (endSlider.getValue() <= startSlider.getValue()) {
                        startSlider.setValue(endSlider.getValue() - 1);
                    }
                }
            });

            // signal viewer
            sPanel = new SignalPanel();
    
            // determine size
            final Dimension preferredSize = startSlider.getPreferredSize();
            preferredSize.width = 500;
            startSlider.setPreferredSize(preferredSize);
            endSlider.setPreferredSize(preferredSize);

            // set panel layout
            this.setLayout(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            this.add(this.startSlider, gbc);
            gbc.gridy++;
            this.add(this.endSlider, gbc);
            gbc.gridy++;
            this.add(this.sPanel, gbc);
            gbc.gridy++;
            this.add(recInfoViewer, gbc);
            final Border lineBorder = BorderFactory.createLineBorder(
                Color.BLACK, 1
            );
            final CompoundBorder border = new CompoundBorder(
                BorderFactory.createTitledBorder(
                    lineBorder, "Speech Section: Start / End"
                ),
                new EmptyBorder(0, 0, 5, 0)
            );
            this.setBorder(border);
        }
    
        public void update() {
            final int nbits = format.getSampleSizeInBits();
            final boolean isBigEndian = format.isBigEndian();
            final double[] dSignal = Converter.byte2double(signal, nbits, isBigEndian);
            startSlider.setMaximum(dSignal.length);
            endSlider.setMaximum(dSignal.length);
            sPanel.updateSignal(dSignal);
        }

        public void reset() {
            startSlider.setMinimum(defaultMin);
            startSlider.setMaximum(defaultMax);
            startSlider.setValue(defaultStart);
            endSlider.setMinimum(defaultMin);
            endSlider.setMaximum(defaultMax);
            endSlider.setValue(defaultEnd);
            startSlider.setMinimum(defaultMin);
            startSlider.setMaximum(defaultMax);
            startSlider.setValue(defaultStart);
            sPanel.resetSignal();
        }

        private class SignalPanel extends JPanel {
            private static final int WIDTH = 475;
            private static final int HEIGHT = 150;
            private static final double[] defaultSignal = new double[0];
            private double[] signal;
        
            public SignalPanel() {
                this.signal = defaultSignal;
                setPreferredSize(new Dimension(WIDTH, HEIGHT));
            }

            public void resetSignal() {
                updateSignal(defaultSignal);
            }

            public void updateSignal(double[] newSignal) {
                this.signal = newSignal;
                repaint();
            }
        
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                final BufferedImage bi = new BufferedImage(
                    WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                final Graphics2D g2d = bi.createGraphics();

                // set background
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);

                // y = 0
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
                
                if (this.signal != null && 1 < this.signal.length) {
                    final Path2D path = new Path2D.Double();
                    final double y0 = HEIGHT / 2.0 - (HEIGHT * signal[0]) / 2.0;
                    path.moveTo(0, y0);
                    for (int i = 1; i < signal.length; i++) {
                        int x = (int) Math.round((double) i / (signal.length - 1) * WIDTH);
                        double y = HEIGHT / 2.0 - (HEIGHT * signal[i]) / 2.0;
                        path.lineTo(x, y);
                    }
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.draw(path);
                }

                g2d.dispose();
                g.drawImage(bi, 0, 0, this);
            }
        }
    }
}
