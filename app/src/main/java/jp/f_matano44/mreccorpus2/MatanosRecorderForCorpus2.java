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
import java.awt.Dimension;
// import java.awt.GraphicsConfiguration;
// import java.awt.GraphicsDevice;
// import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

/** Main-Class. */
public final class MatanosRecorderForCorpus2 extends JFrame {
    /** main-function. */
    public static final void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Utility.setLookAndFeel();
            new MatanosRecorderForCorpus2();
        });
    }

    private static final String startButtonString = "Start recording";
    private static final String recordingString   = " Stop and Save ";
    final Dimension defaultDimension;

    private int currentIndex = 0;
    private final NativeDiscovery vlc = new NativeDiscovery();
    private final AppConfig conf = new AppConfig();
    private final PreferencePlayer pp = new PreferencePlayer();
    private final RecorderBody recorder = new RecorderBody();
    private final ScriptsManager sm = new ScriptsManager();
    private final UnreadSentences us = new UnreadSentences(sm.getScriptSize());

    private final JTextArea scriptTextArea = new JTextArea();
    private final JScrollPane scriptPanel = new JScrollPane(this.scriptTextArea);
    private final JSlider indexSlider = new JSlider();
    private final JButton prevButton = new JButton("<< Prev");
    private final JTextField indexLabel = new JTextField();
    private final JButton nextButton = new JButton("Next >>");
    private final JButton prefButton = new JButton("Play Preference Sound");
    private final JButton cancelButton = new JButton("Cancel");
    private final JToggleButton recordButton = new JToggleButton(startButtonString);
    private final JButton playButton = new JButton("Play");

    private MatanosRecorderForCorpus2() {
        // Window config
        super("mRecCorpus2");
        final TopBarMenu menuBar = new TopBarMenu(conf, this);
        this.setJMenuBar(menuBar);

        // Script viewer
        Utility.setTextAreaSetting(this.scriptTextArea);
        this.scriptTextArea.setColumns(Constant.textAreaWidth);
        this.scriptTextArea.setRows(9); // ここの数字は決め打ち
        this.scriptTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.scriptPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.scriptPanel.setBackground(null);
        this.scriptPanel.getViewport().setBackground(null);
        this.scriptPanel.setBorder(new LineBorder(Color.BLACK, 1));

        // Index slider
        this.indexSlider.setMinimum(0);
        this.indexSlider.setMaximum(this.sm.getScriptSize() - 1);
        this.indexSlider.addChangeListener((ChangeEvent e) -> {
            this.currentIndex = this.indexSlider.getValue();
            this.update();
        });
        final Dimension sliderSize = indexSlider.getPreferredSize();
        sliderSize.width = Constant.panelWidth;
        this.indexSlider.setPreferredSize(sliderSize);

        // Previous button
        this.prevButton.addActionListener((ActionEvent e) -> {
            currentIndex = this.sm.prevLine(currentIndex);
            this.update();
        });

        // Index viewer
        this.indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.indexLabel.setColumns(Constant.textAreaWidth / 4);
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

        // Next button
        this.nextButton.addActionListener((ActionEvent e) -> {
            currentIndex = this.sm.nextLine(currentIndex);
            this.update();
        });

        // Preference player button
        this.prefButton.addActionListener((ActionEvent e) -> 
            pp.playPreference(currentIndex)
        );

        // Cancel recording button
        this.cancelButton.setEnabled(true);
        this.cancelButton.addActionListener((ActionEvent e) -> {
            recorder.enforceStopRecording();
            this.recorder.isRecording = false;
            this.update();
        });
        final Dimension cancelDimension = this.cancelButton.getPreferredSize();
        cancelDimension.height *= 2;
        cancelDimension.width *= 2;
        this.cancelButton.setPreferredSize(cancelDimension);

        // Recording button
        final Dimension recordDimension = this.recordButton.getPreferredSize();
        this.recordButton.addActionListener(
            (ActionEvent e) -> {
                try {
                    if (this.recorder.isNotRecording()) {
                        this.recorder.startRecording();
                        this.recorder.isRecording = true;
                        this.update();
                    } else {
                        this.recorder.stopRecordingAndSave(currentIndex);
                        this.recorder.isRecording = false;
                        this.update();
                    }
                } catch (Exception ex) {
                    this.recorder.enforceStopRecording();
                    this.recorder.isRecording = false;
                    this.update();
                    JOptionPane.showMessageDialog(
                        null, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        );
        recordDimension.height *= 2;
        recordDimension.width *= 2;
        this.recordButton.setPreferredSize(recordDimension);

        // Play button
        this.playButton.setEnabled(false);
        this.playButton.addActionListener((ActionEvent e) -> {
            recorder.playSavedSignal(currentIndex);
        });
        final Dimension playDimension = cancelDimension.getSize();
        this.playButton.setPreferredSize(playDimension);

        // Script chooser panel setting
        final JPanel scriptChooserPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints scriptChooserGbc = new GridBagConstraints();
        scriptChooserGbc.insets = Constant.insets;
        scriptChooserGbc.gridx = 0;
        scriptChooserPanel.add(this.prevButton, scriptChooserGbc);
        scriptChooserGbc.gridx++;
        scriptChooserPanel.add(this.indexLabel, scriptChooserGbc);
        scriptChooserGbc.gridx++;
        scriptChooserPanel.add(this.nextButton, scriptChooserGbc);

        // Recorder panel setting
        final JPanel recorderPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints recorderGbc = new GridBagConstraints();
        recorderGbc.insets = Constant.insets;
        recorderGbc.gridx = 0;
        recorderPanel.add(this.cancelButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(this.recordButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(this.playButton, recorderGbc);

        // main panel setting
        final JPanel[] xPanel = new JPanel[2];
        xPanel[0] = new JPanel(new GridBagLayout());
        xPanel[1] = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        xPanel[0].add(this.us, gbc);
        gbc.gridy = 0;
        xPanel[1].add(this.scriptPanel, gbc);
        gbc.gridy++;
        xPanel[1].add(this.indexSlider, gbc);
        gbc.gridy++;
        xPanel[1].add(scriptChooserPanel, gbc);
        gbc.insets = Constant.insets;
        gbc.gridy++;
        xPanel[1].add(this.prefButton, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        xPanel[1].add(recorderPanel, gbc);
        gbc.gridy++;
        xPanel[1].add(recorder.sse, gbc);
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final EmptyBorder blank = new EmptyBorder(15, 20, 15, 20);
        mainPanel.setBorder(blank);
        mainPanel.add(xPanel[0]);
        mainPanel.add(xPanel[1]);
        Utility.changeFont(mainPanel);
        this.add(mainPanel);

        // Window setting
        this.pack();
        this.defaultDimension = getSize();
        this.setMinimumSize(this.defaultDimension);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // initialize panel
        this.update();
    }

    private void update() {
        final File saveTo = AppConfig.getSavePath(currentIndex);
        if (this.recorder.isRecording) {
            this.recordButton.setText(recordingString);
            this.indexSlider.setEnabled(false);
            this.prefButton.setEnabled(false);
            this.prevButton.setEnabled(false);
            this.nextButton.setEnabled(false);
            this.playButton.setEnabled(false);
            this.recordButton.setSelected(true);
        } else {
            this.recordButton.setText(startButtonString);
            this.indexSlider.setEnabled(true);
            if (this.vlc.discover()
                && this.pp.list.length > currentIndex
                && this.pp.list[currentIndex].exists()
            ) {
                this.prefButton.setEnabled(true);
            }
            this.prevButton.setEnabled(true);
            this.nextButton.setEnabled(true);
            if (saveTo.exists()) {
                this.playButton.setEnabled(true);
            }
            this.recordButton.setSelected(false);
        }

        this.scriptTextArea.setText(this.sm.getScriptText(currentIndex));
        this.indexSlider.setValue(currentIndex);
        this.indexLabel.setText((currentIndex + 1) + " / " + this.sm.getScriptSize());

        if (saveTo.exists()) {
            this.scriptTextArea.setBackground(new Color(220, 255, 220));
        } else {
            this.scriptTextArea.setBackground(null);
        }

        this.us.update();
    }

    private void getIndexFromIndexViewer() {
        final int currentTemp = currentIndex;
        try {
            final String[] inputSt = indexLabel.getText()
                .replace(" ", "").split("/");
            currentIndex = Integer.parseInt(inputSt[0]) - 1;
            if (currentIndex < 0 || sm.getScriptSize() <= currentIndex) {
                throw new Exception("Too small or too big.");
            }
        } catch (Exception ex) {
            currentIndex = currentTemp;
        }
    }

    // private static double getScreenScalingFactor() {
    //     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    //     GraphicsDevice[] gs = ge.getScreenDevices();
    //     GraphicsConfiguration[] gc = gs[0].getConfigurations();
    //     return gc[0].getDefaultTransform().getScaleX();
    // }
}
