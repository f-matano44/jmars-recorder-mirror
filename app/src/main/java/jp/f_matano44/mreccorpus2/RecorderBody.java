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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.f_matano44.jfloatwavio.Converter;

final class RecorderBody {
    private final AudioFormat format;

    private final ByteArrayOutputStream outStream;
    private TargetDataLine line;
    private byte[] originalByteSignal = new byte[0];
    private byte[] byteSignal = new byte[0];
    public boolean isRecording = false;

    public final SpeechSectionEstimator sse;

    public RecorderBody() {
        this.format = AppConfig.format;
        this.outStream = new ByteArrayOutputStream();

        // Speech section
        this.sse = new SpeechSectionEstimator();
    }

    public boolean isNotRecording() {
        return !this.isRecording;
    }

    public final void startRecording() throws Exception {
        // determine recording thread
        final double recordingTime = 0.1;
        final int bufferSize =
            (int) (format.getFrameSize() * format.getFrameRate() * recordingTime);
        final Thread recordThread = new Thread(() -> {
            final byte[] buffer = new byte[bufferSize];
            while (line.isOpen()) {
                final int count = line.read(buffer, 0, buffer.length);
                if (0 < count) {
                    outStream.write(buffer, 0, count);
                }
            }
        });
        recordThread.setPriority(Thread.MAX_PRIORITY);
        // start recording
        outStream.reset();
        line = AudioSystem.getTargetDataLine(this.format);
        line.open(this.format);
        line.start();
        recordThread.start();
    }

    public final void stopRecordingAndSave(final int index) {
        if (line != null) {
            this.line.close();
            this.originalByteSignal = this.outStream.toByteArray();
            final int fs = (int) AppConfig.format.getSampleRate();
            final int nBits = AppConfig.format.getSampleSizeInBits();
            final double signalLength_s =
                (double) this.originalByteSignal.length / ((nBits / 8) * fs);
            if (0.2 /* s */ <= signalLength_s) {
                this.byteSignal = AppConfig.isNormalize
                    ? RecorderBody.normalization(this.originalByteSignal)
                    : this.originalByteSignal.clone();
                this.sse.update();
                this.saveAllSignalAsWav(index);
            }
        }
    }

    private final void saveAllSignalAsWav(final int index) {
        final File file = AppConfig.getSavePath(index);
        final byte[] audio = this.byteSignal;
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

    public final void enforceStopRecording() {
        if (this.line != null) {
            this.line.close();
        }
        this.originalByteSignal = new byte[0];
        this.byteSignal = new byte[0];
        this.isRecording = false;
        // sse.reset();
    }

    public final void playSavedSignal(final int index) {
        final File wavFile = AppConfig.getSavePath(index);
        try (
            final var ais = AudioSystem.getAudioInputStream(wavFile);
        ) {
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

    private static final byte[] normalization(byte[] byteSignal) {
        // get format
        final int nBits = AppConfig.format.getSampleSizeInBits();
        final boolean isBigEndian = AppConfig.format.isBigEndian();
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
            final double level = AppConfig.normalizationLevel;
            for (int i = 0; i < dSignal.length; i++) {
                dSignal[i] *= level; 
                dSignal[i] /= max;
            }
            return Converter.double2byte(dSignal, nBits, isBigEndian);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private final class SpeechSectionEstimator extends JPanel {
        private static final int sPanelWidth = Constant.panelWidth - 14;
        private static final int sPanelHeight = 150;
        private static final int sliderMin = 0;
        private static final int sliderMax = sPanelWidth;
        private static final int defaultStart = sliderMax / 4;
        private static final int defaultEnd = sliderMax * 3 / 4;

        private final JCheckBox trimmingCheckBox = new JCheckBox("Trimming");
        private final JTextField noticeLabel = new JTextField(
            "Saving adds 0.5s of silence before and after trimmed audio.");
        private final JSlider startSlider = new JSlider(
            JSlider.HORIZONTAL, sliderMin, sliderMax, defaultStart);
        private final JSlider endSlider = new JSlider(
            JSlider.HORIZONTAL, sliderMin, sliderMax, defaultEnd);
        private final SignalPanel sPanel = new SignalPanel();
        private final JTextArea recInfoViewer = new JTextArea();
    
        public SpeechSectionEstimator() {

            trimmingCheckBox.setSelected(true);
            trimmingCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    startSlider.setValue(defaultStart);
                    endSlider.setValue(defaultEnd);
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        startSlider.setEnabled(true);
                        endSlider.setEnabled(true);
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        startSlider.setEnabled(false);
                        endSlider.setEnabled(false);
                    }
                    if (byteSignal.length != 0) {
                        update();
                    } else {
                        reset();
                    }
                }
            });

            this.noticeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            this.noticeLabel.setBackground(null);
            this.noticeLabel.setEditable(false);
            this.noticeLabel.setFocusable(false);
            this.noticeLabel.setBorder(null);      
            this.noticeLabel.setAutoscrolls(false);

            startSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (endSlider.getValue() <= startSlider.getValue()) {
                        endSlider.setValue(startSlider.getValue() + 1);
                    }
                    repaint();
                }
            });

            endSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (endSlider.getValue() <= startSlider.getValue()) {
                        startSlider.setValue(endSlider.getValue() - 1);
                    }
                    repaint();
                }
            });

            // SNR viewer
            Utility.setTextAreaSetting(this.recInfoViewer);
            this.recInfoViewer.setColumns(Constant.textAreaWidth);
    
            // determine size
            final Dimension preferredSize = startSlider.getPreferredSize();
            preferredSize.width = Constant.panelWidth;
            startSlider.setPreferredSize(preferredSize);
            endSlider.setPreferredSize(preferredSize);

            // Script chooser panel setting
            final JPanel trimmingPanel = new JPanel(new GridBagLayout());
            final GridBagConstraints trimmingGbc = new GridBagConstraints();
            trimmingGbc.insets = Constant.insets;
            trimmingGbc.gridx = 0;
            trimmingPanel.add(trimmingCheckBox, trimmingGbc);

            // set panel layout
            this.setLayout(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            // this.add(trimmingPanel, gbc);
            // gbc.gridy++;
            // this.add(this.noticeLabel, gbc);
            // gbc.gridy++;
            this.add(this.startSlider, gbc);
            gbc.gridy++;
            this.add(this.endSlider, gbc);
            gbc.gridy++;
            this.add(this.sPanel, gbc);
            gbc.gridy++;
            this.add(this.recInfoViewer, gbc);

            this.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                "Trimming silent sections: semi-auto estimation"
            ));

            // initialization
            this.reset();
        }

        public void update() {
            final int nbits = format.getSampleSizeInBits();
            final boolean isBigEndian = format.isBigEndian();
            final double[] dSignal =
                Converter.byte2double(byteSignal, nbits, isBigEndian);
            sPanel.updateSignal(dSignal);

            try {
                final double snr = getSignalNoiseRatio();
                this.recInfoViewer.setText(getRecInfo(
                    String.format("%.4f", snr),
                    String.valueOf(isClip())
                ));
            } catch (ArithmeticException e) {
                this.recInfoViewer.setText(getRecInfo(
                    "-Inf",
                    String.valueOf(isClip())
                ));
            } catch (Exception e) {
                this.recInfoViewer.setText(getRecInfo(
                    "Unknown ERROR",
                    "Look printed StackTrace"
                ));
                e.printStackTrace();
            }
        }

        public void reset() {
            startSlider.setValue(defaultStart);
            endSlider.setValue(defaultEnd);
            sPanel.resetSignal();
            this.recInfoViewer.setText(getRecInfo(
                "------", 
                "------"
            ));
        }

        private final double getSignalNoiseRatio() {
            final byte[] byteSignal = originalByteSignal.clone();
            // get signal format
            final int nBits = AppConfig.format.getSampleSizeInBits();
            final boolean isBigEndian = AppConfig.format.isBigEndian();
            final double[] dSignal =
                Converter.byte2double(byteSignal, nBits, isBigEndian);

            final List<Double> power = new ArrayList<>();
            try {
                // set Window size (width: 150ms, shift: 75ms)
                final double width = 0.150; // [s] = 150 [ms]
                final double shift = 0.075; // [s] =  75 [ms]
                final double fs = AppConfig.format.getSampleRate();
                final int windowSize = (int) (width * fs);
                final int shiftSize = (int) (shift * fs);

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

            if (noisePower == 0) {
                throw new ArithmeticException();
            } else {
                return 10 * Math.log10(signalPower / noisePower);
            }
        }

        private boolean isClip() {
            final byte[] byteSignal = originalByteSignal.clone();
            // get signal format
            final int nBits = AppConfig.format.getSampleSizeInBits();
            final boolean isBigEndian = AppConfig.format.isBigEndian();
            // get double signal
            final double[] dSignal = 
                Converter.byte2double(byteSignal, nBits, isBigEndian);
            for (int i = 0; i < dSignal.length; i++) {
                dSignal[i] = Math.abs(dSignal[i]);
            }

            final double max = Arrays.stream(dSignal).max().getAsDouble();
            return 0.99 < max ? true : false;
        }

        private static final String getRecInfo(String snr, String clip) {
            return "S/N: " + snr + "[dB] / Cliping: " + clip;
        }

        private class SignalPanel extends JPanel {
            private static final double[] defaultSignal = new double[0];
            private double[] signal;
        
            public SignalPanel() {
                this.signal = defaultSignal;
                setPreferredSize(new Dimension(sPanelWidth, sPanelHeight));
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
                    sPanelWidth, sPanelHeight, BufferedImage.TYPE_INT_RGB);
                final Graphics2D g2d = bi.createGraphics();

                // set background
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(0, 0, sPanelWidth, sPanelHeight);

                // set speech-section
                if (trimmingCheckBox.isSelected()) {
                    final int ssStart = startSlider.getValue();
                    final int ssWidth = endSlider.getValue() - startSlider.getValue();
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(ssStart, 0, ssWidth, sPanelHeight);
                }

                // y = 0
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine(0, sPanelHeight / 2, sPanelWidth, sPanelHeight / 2);
                
                if (this.signal != null && 1 < this.signal.length) {
                    final Path2D path = new Path2D.Double();
                    final double y0 = sPanelHeight / 2.0 - (sPanelHeight * signal[0]) / 2.0;
                    path.moveTo(0, y0);
                    for (int i = 1; i < signal.length; i++) {
                        int x = (int) Math.round((double) i / (signal.length - 1) * sPanelWidth);
                        double y = sPanelHeight / 2.0 - (sPanelHeight * signal[i]) / 2.0;
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
