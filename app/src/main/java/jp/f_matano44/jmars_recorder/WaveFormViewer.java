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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class WaveFormViewer extends JPanel {
    private static final int sPanelWidth = Main.panelWidth - 14;
    private static final int sPanelHeight = 200;
    private static final int sliderMin = 0;
    private static final int sliderMax = sPanelWidth;
    private static final int defaultStart = sliderMax / 4;
    private static final int defaultEnd = sliderMax * 3 / 4;
    private static final double[] defaultSignal = new double[0];

    private int recsIndex = 0;
    public final List<RecorderBody> recs = new ArrayList<>();

    private final JButton prevButton = new JButton("< Prev");
    private final JTextField indexLabel = new JTextField("0 / 0");
    private final JButton nextButton = new JButton("Next >");
    private final JSlider startSlider = new JSlider(
        JSlider.HORIZONTAL, sliderMin, sliderMax, defaultStart);
    private final JSlider endSlider = new JSlider(
        JSlider.HORIZONTAL, sliderMin, sliderMax, defaultEnd);
    private final SignalPanel sPanel = new SignalPanel();
    private final JTextArea recInfoViewer = new JTextArea();

    public WaveFormViewer() {
        // Previous button
        this.prevButton.addActionListener((ActionEvent e) -> {
            final int prevIndex = recsIndex - 1;
            recsIndex = 0 <= prevIndex ? prevIndex : this.recs.size() - 1;
            this.update();
        });

        // Next button
        this.nextButton.addActionListener((ActionEvent e) -> {
            final int nextIndex = recsIndex + 1;
            recsIndex = nextIndex <= this.recs.size() - 1 ? nextIndex : 0;
            this.update();
        });

        // Index viewer
        this.indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.indexLabel.setColumns(Main.textAreaWidth / 4);
        this.indexLabel.setBackground(null);
        this.indexLabel.setEditable(true);
        this.indexLabel.setFocusable(true);
        this.indexLabel.setBorder(new LineBorder(Color.BLACK, 1));
        this.indexLabel.addActionListener((ActionEvent e) -> {
            this.getIndexFromIndexViewer();
            this.update();
        });
        this.indexLabel.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                getIndexFromIndexViewer();
                update();
            }
        });

        startSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (endSlider.getValue() <= startSlider.getValue()) {
                    endSlider.setValue(startSlider.getValue() + 1);
                }
                repaint();
            }
        });
        startSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDataExist()) {
                    recs.get(recsIndex).saveSignalAsWav(
                        (double) startSlider.getValue() / sliderMax,
                        (double) endSlider.getValue() / sliderMax
                    );
                }
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
        endSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDataExist()) {
                    recs.get(recsIndex).saveSignalAsWav(
                        (double) startSlider.getValue() / sliderMax,
                        (double) endSlider.getValue() / sliderMax
                    );
                }
            }
        });

        if (!AppConfig.isTrimming) {
            startSlider.setValue(sliderMin);
            startSlider.setEnabled(false);
            endSlider.setValue(sliderMax);
            endSlider.setEnabled(false);
        }

        // Script chooser panel setting
        final JPanel recorderChooserPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints recorderChooserGbc = new GridBagConstraints();
        recorderChooserGbc.insets = Main.insets;
        recorderChooserGbc.gridx = 0;
        recorderChooserPanel.add(this.prevButton, recorderChooserGbc);
        recorderChooserGbc.gridx++;
        recorderChooserPanel.add(this.indexLabel, recorderChooserGbc);
        recorderChooserGbc.gridx++;
        recorderChooserPanel.add(this.nextButton, recorderChooserGbc);

        // SNR viewer
        Util.setTextAreaSetting(this.recInfoViewer);
        // set size
        this.recInfoViewer.setRows(1);
        this.recInfoViewer.setPreferredSize(
            new Dimension(sPanelWidth, Main.oneRowHeight));

        // determine size
        final Dimension preferredSize = startSlider.getPreferredSize();
        preferredSize.width = Main.panelWidth;
        startSlider.setPreferredSize(preferredSize);
        endSlider.setPreferredSize(preferredSize);

        // set panel layout
        this.setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(recorderChooserPanel, gbc);
        gbc.gridy++;
        this.add(this.startSlider, gbc);
        gbc.gridy++;
        this.add(this.endSlider, gbc);
        gbc.gridy++;
        this.add(this.sPanel, gbc);
        gbc.gridy++;
        this.add(this.recInfoViewer, gbc);

        this.setBorder(new LineBorder(Color.BLACK, Main.lineBorderThickness));

        // initialization
        this.reset();
    }

    public void playSignal() {
        this.recs.get(recsIndex).playSignal(
            (double) startSlider.getValue() / sliderMax,
            (double) endSlider.getValue() / sliderMax
        );
    }

    public boolean isDataExist() {
        return this.recs.size() != 0;
    }

    public void add(RecorderBody recorder) {
        if (recorder.getByteSignal().length != 0) {
            try {
                this.recs.add(recorder.clone());
                this.recsIndex = this.recs.size() - 1;
            } catch (Exception e) {
                e.printStackTrace(AppConfig.logTargetStream);
            }
        }
        this.update();
    }

    private void update() {
        if (this.recs.size() != 0) {
            final RecorderBody recorder = this.recs.get(recsIndex);
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
                this.recInfoViewer.setText(getRecInfo(
                    String.format("%.1f", recorder.getSignalNoiseRatio()),
                    String.valueOf(recorder.isClipping())
                ));
            } catch (ArithmeticException e) {
                this.recInfoViewer.setText(getRecInfo(
                    "-Inf",
                    String.valueOf(recorder.isClipping())
                ));
            } catch (Exception e) {
                this.recInfoViewer.setText(getRecInfo(
                    "Unknown ERROR",
                    "Look StackTrace that printed std-out"
                ));
                e.printStackTrace();
            }
            this.indexLabel.setText((recsIndex + 1) + " / " + this.recs.size());
        } else {
            reset();
        }
    }

    public void reset() {
        this.recs.clear();
        this.startSlider.setValue(AppConfig.isTrimming ? defaultStart : sliderMin);
        this.endSlider.setValue(AppConfig.isTrimming ? defaultEnd : sliderMax);
        this.sPanel.resetSignal();
        this.recInfoViewer.setText(getRecInfo(
            "----",
            "----"
        ));
        this.indexLabel.setText("0 / 0");
    }

    private static final String getRecInfo(String snr, String clip) {
        return "S/N: " + snr + "[dB] / Clipping: " + clip;
    }

    private class SignalPanel extends JPanel {
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
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, sPanelWidth, sPanelHeight);

            // set speech-section
            final int ssStart = startSlider.getValue();
            final int ssWidth = endSlider.getValue() - startSlider.getValue();
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(ssStart, 0, ssWidth, sPanelHeight);

            // y = 0
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(0, sPanelHeight / 2, sPanelWidth, sPanelHeight / 2);

            if (1 < this.signal.length) {
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

    private void getIndexFromIndexViewer() {
        final int currentTemp = this.recsIndex;
        try {
            final String[] inputSt = indexLabel.getText()
                .replace(" ", "").split("/");
            this.recsIndex = Integer.parseInt(inputSt[0]) - 1;
            if (this.recsIndex < 0 || this.recs.size() <= this.recsIndex) {
                throw new Exception("Too small or too big.");
            }
        } catch (Exception ex) {
            this.recsIndex = currentTemp;
        }
    }
}
