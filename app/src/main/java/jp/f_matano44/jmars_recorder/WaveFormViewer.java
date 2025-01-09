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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jp.f_matano44.jmars_recorder.MyClasses.SuperIndexViewer;
import jp.f_matano44.jmars_recorder.MyClasses.UneditableTextArea;


final class WaveFormViewer extends JPanel {
    // MARK: Variables
    private static final int sPanelWidth = Main.panelWidth - 14;
    private static final int sPanelHeight = 200;
    private static final int sliderMin = 0;
    private static final int sliderMax = sPanelWidth;
    private static final int defaultStart = sliderMax / 4;
    private static final int defaultEnd = sliderMax * 3 / 4;
    private static final double[] defaultSignal = new double[0];

    private static int recsIndex = 0;
    private static final List<RecorderBody> recs = new ArrayList<>();

    // MARK: Components
    private static final PrevButton prevButton = new PrevButton();
    private static final IndexViewer indexViewer = new IndexViewer();
    private static final NextButton nextButton = new NextButton();
    private static final StartSlider startSlider = new StartSlider();
    private static final EndSlider endSlider = new EndSlider();
    private static final SignalPanel sPanel = new SignalPanel(sPanelWidth, sPanelHeight);
    private static final JTextArea recInfoViewer = new UneditableTextArea();


    // MARK: Constructor
    public WaveFormViewer() {
        // Script chooser panel setting
        final JPanel recorderChooserPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints recorderChooserGbc = new GridBagConstraints();
        recorderChooserGbc.insets = Main.defaultInsets;
        recorderChooserGbc.gridx = 0;
        recorderChooserPanel.add(prevButton, recorderChooserGbc);
        recorderChooserGbc.gridx++;
        recorderChooserPanel.add(indexViewer, recorderChooserGbc);
        recorderChooserGbc.gridx++;
        recorderChooserPanel.add(nextButton, recorderChooserGbc);

        // set panel layout
        this.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(recorderChooserPanel, gbc);
        gbc.gridy++;
        this.add(startSlider, gbc);
        gbc.gridy++;
        this.add(endSlider, gbc);
        gbc.gridy++;
        this.add(sPanel, gbc);
        gbc.gridy++;
        this.add(recInfoViewer, gbc);

        final Dimension indexLabelDimension = indexViewer.getPreferredSize();
        indexLabelDimension.width = Main.panelWidth / 4;
        indexViewer.setPreferredSize(indexLabelDimension);

        final Dimension preferredSize = startSlider.getPreferredSize();
        preferredSize.width = Main.panelWidth;
        startSlider.setPreferredSize(preferredSize);
        endSlider.setPreferredSize(preferredSize);

        recInfoViewer.setRows(1);
        recInfoViewer.setPreferredSize(new Dimension(sPanelWidth, Main.oneRowHeight));

        this.setBorder(new LineBorder(Color.BLACK, Main.lineBorderThickness));

        // initialization
        WaveFormViewer.resetThis();
    }


    // MARK: Public method
    public void playSignal() {
        recs.get(recsIndex).playSignal(
            (double) startSlider.getValue() / startSlider.getMaximum(),
            (double) endSlider.getValue() / endSlider.getMaximum()
        );
    }

    public static boolean hasRecData() {
        return recs.size() != 0;
    }

    public void add(RecorderBody recorder) {
        if (recorder.getByteSignal().length != 0) {
            try {
                recs.add(recorder.clone());
                recsIndex = getMaxOfIndex();
            } catch (final Exception e) {
                e.printStackTrace(AppConfig.logTargetStream);
            }
        }
        WaveFormViewer.updateThis();
    }

    public static void resetThis() {
        if (!AppConfig.isTrimming) {
            startSlider.setValue(sliderMin);
            startSlider.setEnabled(false);
            endSlider.setValue(sliderMax);
            endSlider.setEnabled(false);
        }

        recs.clear();
        startSlider.setValue(AppConfig.isTrimming ? defaultStart : sliderMin);
        endSlider.setValue(AppConfig.isTrimming ? defaultEnd : sliderMax);
        sPanel.updateSignal(defaultSignal);
        recInfoViewer.setText(getRecInfo("----", "----"));
        indexViewer.resetThis();
    }


    // MARK: Private method
    private static int getMaxOfIndex() {
        return recs.size() - 1;
    }

    private static void saveSignalAsWav() {
        recs.get(recsIndex).saveSignalAsWav(
            (double) startSlider.getValue() / sliderMax,
            (double) endSlider.getValue() / sliderMax
        );
    }

    private static String getRecInfo(String snr, String clip) {
        return "S/N: " + snr + "[dB] / Clipping: " + clip;
    }

    private static void updateThis() {
        if (recs.size() != 0) {
            final RecorderBody recorder = recs.get(recsIndex);
            if (AppConfig.isTrimming) {
                final int start = (int) Math.round(
                    sliderMax * recorder.getStartPointOfSpeechSection_percent());
                startSlider.setValue(start);
                final int end = (int) Math.round(
                    sliderMax * recorder.getEndPointOfSpeechSection_percent());
                endSlider.setValue(end);
            }
            final double[] dSignal = recorder.getDoubleSignal();
            sPanel.updateSignal(dSignal);
            recorder.saveSignalAsWav(
                (double) startSlider.getValue() / sliderMax,
                (double) endSlider.getValue() / sliderMax
            );

            try {
                recInfoViewer.setText(getRecInfo(
                    String.format("%.1f", recorder.getSignalNoiseRatio()),
                    String.valueOf(recorder.isClipping())
                ));
            } catch (ArithmeticException e) {
                recInfoViewer.setText(getRecInfo(
                    "-Inf",
                    String.valueOf(recorder.isClipping())
                ));
            } catch (Exception e) {
                recInfoViewer.setText(getRecInfo(
                    "Unknown ERROR",
                    "Look StackTrace that printed std-out"
                ));
                e.printStackTrace();
            }
            indexViewer.updateThisObj();
        } else {
            resetThis();
        }
    }


    // MARK: Inner Classes
    private static final class PrevButton extends JButton {
        public PrevButton() {
            super("< Prev");

            this.addActionListener((ActionEvent e) -> {
                final int prevIndex = recsIndex - 1;
                recsIndex = 0 <= prevIndex ? prevIndex : recs.size() - 1;
                WaveFormViewer.updateThis();
            });
        }
    }


    private static final class IndexViewer extends SuperIndexViewer {
        public IndexViewer() {
            super(0);

            this.addActionListener((ActionEvent e) -> {
                this.updateIndex();
                WaveFormViewer.updateThis();
            });
            this.addFocusListener(new FocusAdapter() {
                @Override public void focusLost(FocusEvent e) {
                    updateIndex();
                    WaveFormViewer.updateThis();
                }
            });
        }

        @Override public void updateThisObj() {
            final boolean isRecording = RecorderBody.isRecording();
            this.setText((recsIndex + 1) + " / " + (getMaxOfIndex() + 1));
            this.setEditable(!isRecording);
            this.setFocusable(!isRecording);
            this.setBackground(isRecording ? Color.LIGHT_GRAY : null);
        }

        @Override public void updateIndex() {
            final int currentTemp = recsIndex;
            try {
                recsIndex = this.getIndexFromText();
            } catch (final NumberFormatException e) {
                recsIndex = currentTemp;
            }
        }
    }


    private static final class NextButton extends JButton {
        public NextButton() {
            super("Next >");

            this.addActionListener((ActionEvent e) -> {
                final int nextIndex = recsIndex + 1;
                recsIndex = nextIndex <= recs.size() - 1 ? nextIndex : 0;
                WaveFormViewer.updateThis();
            });
        }
    }


    private static final class StartSlider extends JSlider {
        public StartSlider() {
            super(JSlider.HORIZONTAL, sliderMin, sliderMax, defaultStart);

            this.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (endSlider.getValue() <= startSlider.getValue()) {
                        endSlider.setValue(startSlider.getValue() + 1);
                    }
                    final double[] signal = recs.size() != 0
                        ? recs.get(recsIndex).getDoubleSignal() : defaultSignal;
                    sPanel.updateSignal(signal);
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (WaveFormViewer.hasRecData()) {
                        WaveFormViewer.saveSignalAsWav();
                    }
                }
            });
        }
    }


    private static final class EndSlider extends JSlider {
        public EndSlider() {
            super(JSlider.HORIZONTAL, sliderMin, sliderMax, defaultEnd);

            this.addChangeListener(new ChangeListener() {
                @Override public void stateChanged(ChangeEvent e) {
                    if (endSlider.getValue() <= startSlider.getValue()) {
                        startSlider.setValue(endSlider.getValue() - 1);
                    }
                    final double[] signal = recs.size() != 0
                        ? recs.get(recsIndex).getDoubleSignal() : defaultSignal;
                    sPanel.updateSignal(signal);
                }
            });
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (WaveFormViewer.hasRecData()) {
                        WaveFormViewer.saveSignalAsWav();
                    }
                }
            });
        }
    }


    private static final class SignalPanel extends JPanel {
        private int ssStart = defaultStart;
        private int ssEnd = defaultEnd;
        private double[] signal = defaultSignal;

        public SignalPanel(final int width, final int height) {
            this.setPreferredSize(new Dimension(width, height));
        }

        public void updateSignal(final double[] signal) {
            this.ssStart = WaveFormViewer.startSlider.getValue();
            this.ssEnd = WaveFormViewer.endSlider.getValue();
            this.signal = signal;
            this.repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final int width = this.getSize().width;
            final int height = this.getSize().height;

            final BufferedImage bi = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2d = bi.createGraphics();

            // set background
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, width, height);

            // set speech-section
            final int ssWidth = ssEnd - ssStart;
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(ssStart, 0, ssWidth, height);

            // y = 0
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(0, height / 2, width, height / 2);

            if (1 < this.signal.length) {
                final Path2D path = new Path2D.Double();
                final double y0 = height / 2.0 - (height * signal[0]) / 2.0;
                path.moveTo(0, y0);
                for (int i = 1; i < signal.length; i++) {
                    final int x = (int) Math.round((double) i / (signal.length - 1) * width);
                    final double y = height / 2.0 - (height * signal[i]) / 2.0;
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
