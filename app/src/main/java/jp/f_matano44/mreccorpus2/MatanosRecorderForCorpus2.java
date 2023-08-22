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

/** Main-Class. */
public final class MatanosRecorderForCorpus2 extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MatanosRecorderForCorpus2());
    }

    private final int[] currentIndex = {0};
    private final AppConfig conf = new AppConfig();
    private final PreferencePlayer pp = new PreferencePlayer(conf, currentIndex);
    private final RecorderBody recorder = new RecorderBody(conf, currentIndex);
    private final ScriptsManager sm = new ScriptsManager(conf, currentIndex);
    private final UnreadScripts us = new UnreadScripts(conf, sm.getScriptSize());
    final Dimension defaultDimension;

    private static final String startButtonString = "Start recording";
    private static final String recordingString   = " Stop and Save ";

    private final JTextArea scriptPathViewer;
    private final JTextArea scriptTextArea;
    private final JScrollPane scriptPanel;
    private final JSlider indexSlider;
    private final JButton prevButton;
    private final JTextField indexLabel;
    private final JButton nextButton;
    private final JButton prefButton;
    private final JToggleButton recordButton;
    private final JButton playButton;
    private final JTextArea saveToViewer;

    private MatanosRecorderForCorpus2() {
        // Window title
        super("mRecCorpus2");
        Utility.setLookAndFeel();

        // set menu-bar
        TopBarMenu menuBar = new TopBarMenu(conf, this);
        this.setJMenuBar(menuBar);

        final String scriptPathString = "Scripts: " + conf.script.getAbsolutePath();
        this.scriptPathViewer = new JTextArea(scriptPathString);
        Utility.setTextAreaSetting(this.scriptPathViewer);
        this.scriptPathViewer.setColumns(Constant.textAreaWidth);
        this.scriptPathViewer.setBorder(new EmptyBorder(0, 0, 5, 0));

        this.scriptTextArea = new JTextArea();
        Utility.setTextAreaSetting(this.scriptTextArea);
        this.scriptTextArea.setColumns(Constant.textAreaWidth);
        this.scriptTextArea.setRows(9); // ここの数字は決め打ち
        this.scriptTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.scriptPanel = new JScrollPane(this.scriptTextArea);
        this.scriptPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.scriptPanel.setBackground(null);
        this.scriptPanel.getViewport().setBackground(null);
        this.scriptPanel.setBorder(new LineBorder(Color.BLACK, 1));

        this.indexSlider = new JSlider(0, this.sm.getScriptSize() - 1);
        this.indexSlider.addChangeListener((ChangeEvent e) -> {
            this.currentIndex[0] = this.indexSlider.getValue();
            this.update();
        });
        final Dimension sliderSize = indexSlider.getPreferredSize();
        sliderSize.width = Constant.panelWidth;
        this.indexSlider.setPreferredSize(sliderSize);

        this.prevButton = new JButton("<< Prev");
        this.prevButton.addActionListener((ActionEvent e) -> {
            this.sm.prevLine();
            this.update();
        });

        this.indexLabel = new JTextField();
        this.indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.indexLabel.setColumns(Constant.textAreaWidth / 4);
        this.indexLabel.setBackground(null);
        this.indexLabel.setEditable(true);
        this.indexLabel.setBorder(new LineBorder(Color.BLACK, 1));
        this.indexLabel.addActionListener((ActionEvent e) -> {
            getIndexFromIndexViewer();
            update();
        });
        this.indexLabel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                getIndexFromIndexViewer();
                update();
            }
        });

        this.nextButton = new JButton("Next >>");
        this.nextButton.addActionListener((ActionEvent e) -> {
            this.sm.nextLine();
            this.update();
        });

        this.prefButton = new JButton("Play Preference Sound");
        this.prefButton.addActionListener((ActionEvent e) -> pp.playPreference());

        this.recordButton = new JToggleButton(startButtonString);
        final Dimension recordDimension = this.recordButton.getPreferredSize();
        recordDimension.height *= 2;
        recordDimension.width *= 1.3;
        this.recordButton.setPreferredSize(recordDimension);

        this.playButton = new JButton("Play");
        this.playButton.setEnabled(false);
        this.playButton.addActionListener((ActionEvent e) -> recorder.playSignal());
        final Dimension playDimension = this.playButton.getPreferredSize();
        playDimension.height *= 2;
        playDimension.width *= 1.3;
        this.playButton.setPreferredSize(playDimension);

        this.saveToViewer = new JTextArea();
        Utility.setTextAreaSetting(this.saveToViewer);
        this.saveToViewer.setColumns(Constant.textAreaWidth);

        // main panel setting
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final JPanel[] xPanel = new JPanel[2];
        xPanel[0] = new JPanel(new GridBagLayout());
        xPanel[1] = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        final EmptyBorder blank = new EmptyBorder(15, 20, 15, 20);
        mainPanel.setBorder(blank);
        gbc.gridy = 0;
        xPanel[0].add(this.us, gbc);
        gbc.gridy = 0;
        xPanel[1].add(this.scriptPathViewer, gbc);
        gbc.gridy++;
        xPanel[1].add(this.scriptPanel, gbc);
        gbc.gridy++;
        xPanel[1].add(this.indexSlider, gbc);
        gbc.gridy++;
        xPanel[1].add(this.scriptChooserPanel(), gbc);
        gbc.gridy++;
        xPanel[1].add(this.prefButton, gbc);
        gbc.gridy++;
        xPanel[1].add(this.recorderPanel(), gbc);
        gbc.gridy++;
        xPanel[1].add(recorder.sse, gbc);
        gbc.gridy++;
        xPanel[1].add(this.saveToViewer, gbc);
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

    // private static double getScreenScalingFactor() {
    //     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    //     GraphicsDevice[] gs = ge.getScreenDevices();
    //     GraphicsConfiguration[] gc = gs[0].getConfigurations();
    //     return gc[0].getDefaultTransform().getScaleX();
    // }

    private void update() {
        final int num = currentIndex[0] + 1;
        final File saveTo = this.conf.getSavePath(num);
        final String saveToString = saveTo.getAbsolutePath();
        this.indexSlider.setValue(currentIndex[0]);
        this.indexLabel.setText(this.indexStringBuilder(num));
        this.scriptTextArea.setText(this.sm.getScriptText());
        this.saveToViewer.setText("Save to: " + saveToString);
        if (saveTo.exists()) {
            this.scriptTextArea.setBackground(new Color(220, 255, 220));
        } else {
            this.scriptTextArea.setBackground(null);
        }
        this.us.update();
    }

    private void getIndexFromIndexViewer() {
        final int current = currentIndex[0];
        final String[] inputSt = indexLabel.getText()
            .replace(" ", "").split("/");
        try {
            currentIndex[0] = Integer.parseInt(inputSt[0]) - 1;
            if (currentIndex[0] < 0 || sm.getScriptSize() <= currentIndex[0]) {
                throw new Exception("小さすぎ若しくはデカすぎ");
            }
        } catch (Exception ex) {
            currentIndex[0] = current;
        }
        indexLabel.setText(indexStringBuilder(currentIndex[0]));
    }

    private String indexStringBuilder(int index) {
        return index + " / " + this.sm.getScriptSize();
    }

    private JPanel scriptChooserPanel() { // GridBagLayout は NG
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        panel.add(this.prevButton, gbc);
        gbc.gridx++;
        panel.add(this.indexLabel, gbc);
        gbc.gridx++;
        panel.add(this.nextButton, gbc);
        return panel;
    }

    private JPanel recorderPanel() {
        this.recordButton.addActionListener(
            (ActionEvent e) -> {
                try {
                    if (!this.recorder.isRecording) {
                        this.recorder.isRecording = true;
                        this.recordButton.setText(recordingString);
                        this.recorder.startRecording();
                    } else {
                        this.recorder.stopRecording();
                        this.recordButton.setText(startButtonString);
                        this.recorder.isRecording = false;
                        this.update();
                    }
                    // ボタンを有効化 or 無効化
                    this.prefButton.setEnabled(!this.recorder.isRecording);
                    this.playButton.setEnabled(!this.recorder.isRecording);
                    this.prevButton.setEnabled(!this.recorder.isRecording);
                    this.nextButton.setEnabled(!this.recorder.isRecording);
                } catch (Exception ex) {
                    this.prefButton.setEnabled(true);
                    this.recorder.enforceStopRecording();
                    this.recordButton.setText(startButtonString);
                    this.recordButton.setSelected(false);
                    this.playButton.setEnabled(false);
                    this.update();
                    JOptionPane.showMessageDialog(
                        null, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        );

        final JPanel panel = new JPanel(new GridBagLayout());
        // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        panel.add(this.recordButton, gbc);
        gbc.gridx++;
        panel.add(this.playButton, gbc);
        return panel;
    }
}
