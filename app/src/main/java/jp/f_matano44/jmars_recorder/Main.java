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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
public final class Main extends JFrame {
    /** main-function. */
    public static final void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Util.setLookAndFeel();
            new Main();
        });
    }


    // MARK: Components
    private final ReferencePlayer refPlayer = new ReferencePlayer();
    private final RecorderBody recorder = new RecorderBody();
    private final WaveFormViewer wfv = new WaveFormViewer();
    private final ScriptsManager sm = new ScriptsManager();

    private final JTextArea scriptTextArea = new JTextArea();
    private final JScrollPane scriptPanel = new JScrollPane();
    private final JSlider indexSlider = new JSlider();
    private final JTextField indexLabel = new JTextField("0 / 0");
    private final JButton nextButton = new JButton("Next >>");
    private final JButton refButton = new JButton("Play Ref.");
    private final JButton no001Button = new JButton("Play No.001");
    private final JToggleButton recordButton = new JToggleButton(startButtonString);
    private final JButton playButton = new JButton("Play Rec.");


    // MARK: Member variable
    static int currentIndex = 0;


    // MARK: Constants
    private static final String startButtonString = "Start recording";
    private static final String recordingString   = "Stop and Save";
    public static final int lineBorderThickness = 1;
    final Dimension defaultWindowDimension;
    public static final int oneRowHeight;
    public static final int textAreaWidth = 60;
    public static final int panelWidth = 750;
    public static final Insets defaultInsets = new Insets(0, 0, 0, 0);
    private static final int insetsNum = 4;
    public static final Insets insets
        = new Insets(insetsNum, insetsNum, insetsNum, insetsNum);


    // MARK: application info
    public static final String appName;
    public static final String appVersion;
    public static final String buildBy;
    public static final String buildDate;
    public static final String gitHEAD;
    public static final String copyright;
    public static final String license;


    // MARK: Static initializer
    static {
        final JTextArea sampleTextArea = new JTextArea("Sample string");
        Util.setTextAreaSetting(sampleTextArea);
        sampleTextArea.setRows(1);
        oneRowHeight = sampleTextArea.getFontMetrics(sampleTextArea.getFont()).getHeight();

        String tempAppName = "unknown";
        String tempAppVersion = "unknown";
        String tempBuildBy = "unknown";
        String tempBuildDate = "unknown";
        String tempGitHEAD = "Unknown";
        String tempCopyright = "unknown";
        String tempLicense = "unknown";
        try (InputStream input = Main.class.getClassLoader()
            .getResourceAsStream("build-info.properties")
        ) {
            final Properties prop = new Properties();
            prop.load(input);
            tempAppName = prop.getProperty("app.name");
            tempAppVersion = prop.getProperty("app.version");
            tempBuildBy = prop.getProperty("build.by");
            tempBuildDate = prop.getProperty("build.date");
            tempGitHEAD = prop.getProperty("git.head");
            tempCopyright = prop.getProperty("copyright");
            tempLicense = prop.getProperty("license");
        } catch (final IOException e) {
            e.printStackTrace();
        }
        appName = tempAppName;
        appVersion = tempAppVersion;
        buildBy = tempBuildBy;
        buildDate = tempBuildDate;
        gitHEAD = tempGitHEAD;
        copyright = tempCopyright;
        license = tempLicense;
    }


    // MARK: Constructor
    private Main() {
        // Window config
        super(Main.appName + " - " + Main.appVersion);
        this.setJMenuBar(new TopBarMenu(this));

        this.setComponentAction();

        // Script viewer
        Util.setTextAreaSetting(this.scriptTextArea);
        this.scriptTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        scriptPanel.setViewportView(this.scriptTextArea);
        scriptPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scriptPanel.setBorder(new LineBorder(Color.BLACK, lineBorderThickness));
        final int rows = 9; // ここの数字は決め打ち
        scriptPanel.setPreferredSize(
            new Dimension(Main.panelWidth, Main.oneRowHeight * rows));
        // this.scriptTextArea.setRows(rows);
        // this.scriptTextArea.setColumns(Main.textAreaWidth);

        // Index slider
        this.indexSlider.setMinimum(0);
        this.indexSlider.setMaximum(this.sm.getScriptSize() - 1);
        final Dimension sliderSize = indexSlider.getPreferredSize();
        sliderSize.width = Main.panelWidth;
        this.indexSlider.setPreferredSize(sliderSize);

        // Index viewer
        this.indexLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.indexLabel.setColumns(Main.textAreaWidth / 4);
        this.indexLabel.setFocusable(true);
        this.indexLabel.setBorder(new LineBorder(Color.BLACK, lineBorderThickness));

        // Recorder panel setting
        final JPanel recorderPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints recorderGbc = new GridBagConstraints();
        recorderGbc.insets = Main.insets;
        recorderGbc.gridx = 0;
        recorderPanel.add(this.no001Button, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(this.refButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(this.recordButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(this.playButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(this.nextButton, recorderGbc);

        // main panel setting
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        mainPanel.add(scriptPanel, gbc);
        gbc.gridy++;
        mainPanel.add(this.indexSlider, gbc);
        gbc.gridy++;
        mainPanel.add(this.indexLabel, gbc);
        gbc.gridy++;
        mainPanel.add(recorderPanel, gbc);
        gbc.gridy++;
        mainPanel.add(wfv, gbc);
        Util.changeFont(mainPanel, AppConfig.fontSize);
        this.add(mainPanel);

        // Component size setting
        final double buttonHeightRatio = 2.0;
        final double buttonWidthRatio = 1.1;
        final Dimension refDimension = this.refButton.getPreferredSize();
        refDimension.height *= buttonHeightRatio;
        refDimension.width *= buttonWidthRatio;
        this.refButton.setPreferredSize(refDimension);
        final Dimension myRefDimension = this.no001Button.getPreferredSize();
        myRefDimension.height *= buttonHeightRatio;
        myRefDimension.width *= buttonWidthRatio;
        this.no001Button.setPreferredSize(myRefDimension);
        final Dimension recordDimension = this.recordButton.getPreferredSize();
        recordDimension.height *= buttonHeightRatio;
        recordDimension.width *= buttonWidthRatio;
        this.recordButton.setPreferredSize(recordDimension);
        final Dimension playDimension = this.playButton.getPreferredSize();
        playDimension.height *= buttonHeightRatio;
        playDimension.width *= buttonWidthRatio;
        this.playButton.setPreferredSize(playDimension);
        final Dimension nextDimension = this.nextButton.getPreferredSize();
        nextDimension.height *= buttonHeightRatio;
        nextDimension.width *= buttonWidthRatio;
        this.nextButton.setPreferredSize(nextDimension);

        // Window setting
        this.pack();
        this.defaultWindowDimension = getSize();
        defaultWindowDimension.height *= 1.01;
        defaultWindowDimension.width *= 1.01;
        this.setMinimumSize(this.defaultWindowDimension);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // initialize panel
        this.indexSlider.setValue(0);
        this.update();
    }


    // MARK: Def of component


    // MARK: Method
    private void update() {
        this.scriptTextArea.setText(this.sm.getScriptText(currentIndex));
        final File saveTo = AppConfig.getSaveFile(currentIndex);
        final Color lightGreen = new Color(220, 255, 220);
        this.scriptTextArea.setBackground(saveTo.exists() ? lightGreen : null);

        this.indexSlider.setValue(currentIndex);
        this.indexSlider.setEnabled(!RecorderBody.isRecording());

        this.indexLabel.setText((currentIndex + 1) + " / " + this.sm.getScriptSize());
        this.indexLabel.setEditable(!RecorderBody.isRecording());
        this.indexLabel.setBackground(null);

        this.no001Button.setEnabled(
            !RecorderBody.isRecording()
            && this.refPlayer.isPlayerExist
            && this.refPlayer.isNo001Exist()
        );

        this.refButton.setEnabled(
            !RecorderBody.isRecording()
            && this.refPlayer.isPlayerExist
            && this.refPlayer.list.length > currentIndex
            && this.refPlayer.list[currentIndex].exists()
        );

        this.recordButton.setSelected(RecorderBody.isRecording());
        this.recordButton.setText(
            !RecorderBody.isRecording()
            ? startButtonString
            : recordingString
        );

        this.playButton.setEnabled(this.wfv.isDataExist() && !RecorderBody.isRecording());

        this.nextButton.setEnabled(!RecorderBody.isRecording());
    }


    private void setIndexFromIndexViewer() {
        final int currentIdxTemp = Main.currentIndex;
        try {
            final String[] inputSt = indexLabel.getText()
                .replace(" ", "").split("/");
            Main.currentIndex = Integer.parseInt(inputSt[0]) - 1;
            if (Main.currentIndex < 0 || sm.getScriptSize() <= Main.currentIndex) {
                throw new Exception("Too small or too big.");
            }
        } catch (final Exception e) {
            Main.currentIndex = currentIdxTemp;
        }
    }


    private void setComponentAction() {
        this.indexSlider.addChangeListener((ChangeEvent e) -> {
            Main.currentIndex = this.indexSlider.getValue();
            wfv.reset();
            this.update();
        });

        this.indexLabel.addActionListener((ActionEvent e) -> {
            this.setIndexFromIndexViewer();
            this.update();
        });
        this.indexLabel.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                setIndexFromIndexViewer();
                update();
            }
        });

        this.refButton.addActionListener((final ActionEvent e) ->
            refPlayer.playReference(currentIndex)
        );

        this.no001Button.addActionListener((final ActionEvent e) ->
            refPlayer.playNumber001()
        );

        this.recordButton.addActionListener((final ActionEvent e) -> {
            try {
                if (!RecorderBody.isRecording()) {
                    this.recorder.startRecording();
                } else {
                    this.recorder.stopRecording();
                    wfv.add(recorder);
                }
                this.update();
            } catch (final Exception ex) {
                this.recorder.enforceStopRecording();
                this.update();
                ex.printStackTrace(AppConfig.logTargetStream);
                JOptionPane.showMessageDialog(
                    null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        });

        this.playButton.addActionListener((ActionEvent e) -> {
            wfv.playSignal();
        });

        this.nextButton.addActionListener((ActionEvent e) -> {
            currentIndex = this.sm.nextLine(currentIndex);
            wfv.reset();
            this.update();
        });
    }
}
