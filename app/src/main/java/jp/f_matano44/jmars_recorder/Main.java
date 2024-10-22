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
import java.util.Arrays;
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
    // Swing
    private final ScriptPanel scriptPanel = new ScriptPanel();
    private final IndexSlider indexSlider = new IndexSlider(this.sm.getScriptSize() - 1);
    private final IndexLabel indexLabel = new IndexLabel(this.sm.getScriptSize());
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
        Util.setTextViewerSetting(sampleTextArea);
        sampleTextArea.setRows(1);
        oneRowHeight = sampleTextArea.getFontMetrics(sampleTextArea.getFont()).getHeight();

        String tempAppName = "unknown";
        String tempAppVersion = "unknown";
        String tempBuildBy = "unknown";
        String tempBuildDate = "unknown";
        String tempGitHEAD = "unknown";
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

        // add button, slider etc. actions
        this.setComponentAction();

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
        mainPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = Main.defaultInsets;
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
        Util.changeFont(scriptPanel, AppConfig.fontSize + 1);
        this.add(mainPanel);

        // Component size setting
        // Buttons
        final int buttonHeight = this.recordButton.getPreferredSize().height * 2;
        // Set dimention: Start recording
        final double buttonWidthRatio = 1.1;
        final Dimension recordDimension = new Dimension();
        recordDimension.height = buttonHeight;
        recordDimension.width = Math.max(
            (int) (this.recordButton.getPreferredSize().width * buttonWidthRatio),
            this.indexLabel.getPreferredSize().width
        );
        this.recordButton.setPreferredSize(recordDimension);
        // Get other button width
        final int[] widthList = new int[4];
        widthList[0] = this.refButton.getPreferredSize().width;
        widthList[1] = this.no001Button.getPreferredSize().width;
        widthList[2] = this.playButton.getPreferredSize().width;
        widthList[3] = this.nextButton.getPreferredSize().width;
        final int buttonWidth = Arrays.stream(widthList).max().getAsInt();
        // Set dimention: Others
        final Dimension buttonDimension = new Dimension(buttonWidth, buttonHeight);
        this.refButton.setPreferredSize(buttonDimension);
        this.no001Button.setPreferredSize(buttonDimension);
        this.playButton.setPreferredSize(buttonDimension);
        this.nextButton.setPreferredSize(buttonDimension);

        // Window setting
        this.pack();
        this.defaultWindowDimension = getSize();
        defaultWindowDimension.height += 5;
        defaultWindowDimension.width += 5;
        this.setMinimumSize(this.defaultWindowDimension);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // initialize panel
        this.indexSlider.setValue(0);
        this.update();
    }


    // MARK: Def of component
    private static class ScriptPanel extends JScrollPane {
        private final JTextArea scriptTextArea = new JTextArea();

        public ScriptPanel() {
            Util.setTextViewerSetting(this.scriptTextArea);
            this.scriptTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
            this.setViewportView(this.scriptTextArea);
            this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.setBorder(new LineBorder(Color.BLACK, lineBorderThickness));
            final int rows = 10; // ここの数字は決め打ち
            this.setPreferredSize(new Dimension(Main.panelWidth, Main.oneRowHeight * rows));
            // this.scriptTextArea.setRows(rows);
            // this.scriptTextArea.setColumns(Main.textAreaWidth);
        }

        public void updateText(final String str) {
            this.scriptTextArea.setText(str);
        }

        public void updateColor(final Color color) {
            this.scriptTextArea.setBackground(color);
        }
    }


    private static class IndexSlider extends JSlider {
        public IndexSlider(final int sliderMax) {
            this.setMinimum(0);
            this.setMaximum(sliderMax);
            final Dimension sliderSize = this.getPreferredSize();
            sliderSize.width = Main.panelWidth;
            this.setPreferredSize(sliderSize);
        }
    }


    private static class IndexLabel extends JTextField {
        private final int labelMax;

        public IndexLabel(final int labelMax) {
            super("0 / 0");
            this.labelMax = labelMax;
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setColumns(Main.textAreaWidth / 4);
            this.setFocusable(true);
            this.setBorder(new LineBorder(Color.BLACK, lineBorderThickness));
        }

        public int getIndexNumber() {
            final int currentIdxTemp = Main.currentIndex;
            try {
                final String[] inputSt = this.getText().replace(" ", "").split("/");
                final int ans = Integer.parseInt(inputSt[0]) - 1;
                if (ans < 0 || labelMax <= ans) {
                    throw new Exception("Too small or too big.");
                }
                return ans;
            } catch (final Exception e) {
                return currentIdxTemp;
            }
        }

        public void update(final int currentIdx, final int maxIdx) {
            final boolean isRecording = RecorderBody.isRecording();
            this.setText(isRecording ? "RECORDING" : ((currentIndex + 1) + " / " + maxIdx));
            this.setEditable(!isRecording);
            this.setFocusable(!isRecording);
            this.setForeground(isRecording ? Color.WHITE : Color.BLACK);
            this.setBackground(isRecording ? Color.RED : null);
        }
    }


    // MARK: Methods
    private void update() {
        this.scriptPanel.updateText(this.sm.getScriptText(currentIndex));
        final File saveTo = AppConfig.getSaveFile(currentIndex);
        final Color lightGreen = new Color(220, 255, 220);
        this.scriptPanel.updateColor(saveTo.exists() ? lightGreen : null);

        this.indexSlider.setValue(currentIndex);
        this.indexSlider.setEnabled(!RecorderBody.isRecording());

        this.indexLabel.update(currentIndex, this.sm.getScriptSize());

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


    private void setComponentAction() {
        this.indexSlider.addChangeListener((ChangeEvent e) -> {
            Main.currentIndex = this.indexSlider.getValue();
            wfv.reset();
            this.update();
        });

        this.indexLabel.addActionListener((ActionEvent e) -> {
            Main.currentIndex = this.indexLabel.getIndexNumber();
            this.update();
        });
        this.indexLabel.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                Main.currentIndex = indexLabel.getIndexNumber();
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
