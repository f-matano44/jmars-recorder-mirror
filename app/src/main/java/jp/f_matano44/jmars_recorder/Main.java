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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import jp.f_matano44.jmars_recorder.ScriptManager.IndexSlider;
import jp.f_matano44.jmars_recorder.ScriptManager.IndexViewer;


/** Main-Class. */
public final class Main extends JFrame {
    /** main-function. */
    public static final void main(String[] args) throws IOException {
        // Set logger file path
        final String homeDir = System.getProperty("user.home");
        final Path logDirPath = AppInfo.os.contains("win")
            ? Paths.get(homeDir, "AppData", "Local", AppInfo.name, "logs")
            : Paths.get(homeDir, ".local", "share", AppInfo.name, "logs");
        try {
            Files.createDirectories(logDirPath);
        } catch (final FileAlreadyExistsException e) {
            e.printStackTrace();
            final String errorMessage =
                "There is a file in the directory where the log is saved."
                + System.lineSeparator()
                + "`" + logDirPath.toString() + "`"
                + System.lineSeparator()
                + "Please delete it.";
            JOptionPane.showMessageDialog(
                null, errorMessage,
                "Error", JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        final String logFileName = AppInfo.name + ".log";
        // Logger setting
        final Path logFilePath = logDirPath.resolve(logFileName);
        final Handler handler = new FileHandler(logFilePath.toString());
        Main.logger.setLevel(Level.INFO);
        Main.logger.addHandler(handler);
        handler.setFormatter(new SimpleFormatter());


        // Swing
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                new Main();
            } catch (final Exception e) {
                JOptionPane.showMessageDialog(
                    null, e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }


    // MARK: Instances
    // private final JFrame mainFrame = this;
    static final RecorderBody recorder = new RecorderBody();
    static final WaveFormViewer wfv = new WaveFormViewer();
    // Swing components
    private final JScrollPane scriptPanel = ScriptManager.scriptPanel;
    private final JButton miniNextButton = ScriptManager.miniNextButton;
    private final JButton miniPrevButton = ScriptManager.miniPrevButton;
    private final IndexSlider indexSlider = ScriptManager.indexSlider;
    private final IndexViewer indexLabel = ScriptManager.indexLabel;
    private final JButton nextButton = ScriptManager.nextButton;
    private final JButton refButton = ReferencePlayer.refButton;
    private final JButton no001Button = ReferencePlayer.no001Button;
    private final JToggleButton recordButton = RecorderBody.recordButton;
    private final JButton playButton = RecorderBody.playButton;


    // MARK: Constants
    static final int lineBorderThickness = 1;
    private final Dimension defaultWindowDimension;
    static final int oneRowHeight;
    static final int panelWidth = 750;
    static final Insets defaultInsets = new Insets(4, 4, 4, 4);


    // MARK: Logger
    static final Logger logger = Logger.getLogger(Main.class.getName());


    // MARK: Static initializer
    static {
        final JTextArea sampleTextArea = new JTextArea("Sample string");
        Util.setFontRecursive(sampleTextArea, AppConfig.scriptFontSize);
        sampleTextArea.setRows(1);
        oneRowHeight = sampleTextArea.getFontMetrics(sampleTextArea.getFont()).getHeight();
    }


    // MARK: Constructor
    private Main() {
        // Window config
        super(AppInfo.name + " - " + AppInfo.version);
        this.setJMenuBar(new TopBarMenu(this));

        // add button, slider etc. actions
        this.setComponentAction();

        // Slider panel
        final JPanel sliderPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints sliderGbc = new GridBagConstraints();
        sliderGbc.gridx = 0;
        sliderPanel.add(miniPrevButton, sliderGbc);
        sliderGbc.gridx++;
        sliderPanel.add(indexSlider, sliderGbc);
        sliderGbc.gridx++;
        sliderPanel.add(miniNextButton, sliderGbc);

        // Recorder panel
        final JPanel recorderPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints recorderGbc = new GridBagConstraints();
        recorderGbc.insets = Main.defaultInsets;
        recorderGbc.gridx = 0;
        recorderPanel.add(no001Button, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(refButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(recordButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(playButton, recorderGbc);
        recorderGbc.gridx++;
        recorderPanel.add(nextButton, recorderGbc);

        // main panel setting
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 10, 15, 10));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        mainPanel.add(scriptPanel, gbc);
        gbc.gridy++;
        mainPanel.add(sliderPanel, gbc);
        gbc.gridy++;
        mainPanel.add(indexLabel, gbc);
        gbc.gridy++;
        mainPanel.add(recorderPanel, gbc);
        gbc.gridy++;
        mainPanel.add(wfv, gbc);
        Util.setFontRecursive(mainPanel, AppConfig.fontSize);
        Util.setFontRecursive(scriptPanel, AppConfig.scriptFontSize);
        this.add(mainPanel);

        // Component size setting
        // Script panel
        final int rows = 10; // ここの数字は決め打ち
        scriptPanel.setPreferredSize(
            new Dimension(Main.panelWidth, Main.oneRowHeight * rows));
        // Index Label
        final Dimension indexLabelDimension = indexLabel.getPreferredSize();
        indexLabelDimension.width = Main.panelWidth / 3;
        indexLabel.setPreferredSize(indexLabelDimension);
        // Slider
        final Dimension miniButtonSize = miniNextButton.getPreferredSize();
        final Dimension sliderSize = indexSlider.getPreferredSize();
        miniButtonSize.height = sliderSize.height;
        sliderSize.width = Main.panelWidth - (miniButtonSize.width * 2);
        miniPrevButton.setPreferredSize(miniButtonSize);
        indexSlider.setPreferredSize(sliderSize);
        miniNextButton.setPreferredSize(miniButtonSize);
        // Buttons
        final int buttonHeight = recordButton.getPreferredSize().height * 2;
        // Set dimension: Start recording
        final double buttonWidthRatio = 1.1;
        final Dimension recordDimension = recordButton.getPreferredSize();
        recordDimension.height = buttonHeight;
        recordDimension.width *= buttonWidthRatio;
        recordButton.setPreferredSize(recordDimension);
        // Get other button width
        final int[] widthList = new int[4];
        widthList[0] = refButton.getPreferredSize().width;
        widthList[1] = no001Button.getPreferredSize().width;
        widthList[2] = playButton.getPreferredSize().width;
        widthList[3] = nextButton.getPreferredSize().width;
        final int buttonWidth = Arrays.stream(widthList).max().getAsInt();
        // Set dimension: Others
        final Dimension buttonDimension = new Dimension(buttonWidth, buttonHeight);
        refButton.setPreferredSize(buttonDimension);
        no001Button.setPreferredSize(buttonDimension);
        playButton.setPreferredSize(buttonDimension);
        nextButton.setPreferredSize(buttonDimension);

        // Window setting
        this.pack();
        defaultWindowDimension = this.getPreferredSize();
        defaultWindowDimension.height += 5;
        defaultWindowDimension.width += 5;
        this.setMinimumSize(defaultWindowDimension);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        // initialize panel
        indexSlider.setValue(0);
        recordButton.requestFocusInWindow();
        Main.updateAllGUI();

        Main.logger.info("jMARS Recorder has started.");
    }


    // MARK: Update
    private static void updateAllGUI() {
        ScriptManager.updateGUI();
        ReferencePlayer.updateGUI();
        RecorderBody.updateGUI();
    }


    // MARK: setComponentAction
    private void setComponentAction() {
        indexSlider.addChangeListener((ChangeEvent e) -> {
            indexSlider.updateIndex();
            WaveFormViewer.resetThis();
            Main.updateAllGUI();
        });

        indexLabel.addActionListener((ActionEvent e) -> {
            indexLabel.updateIndex();
            Main.updateAllGUI();
        });
        indexLabel.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                indexLabel.updateIndex();
                Main.updateAllGUI();
            }
        });

        recordButton.addActionListener((final ActionEvent e) -> {
            try {
                if (!RecorderBody.isRecording()) {
                    recorder.startRecording();
                } else {
                    recorder.stopRecording();
                    wfv.add(recorder);
                }
                Main.updateAllGUI();
            } catch (final Exception ex) {
                recorder.enforceStopRecording();
                Main.updateAllGUI();
                ex.printStackTrace(AppConfig.logTargetStream);
                JOptionPane.showMessageDialog(
                    null, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        });

        playButton.addActionListener((ActionEvent e) -> {
            wfv.playSignal();
        });

        nextButton.addActionListener((ActionEvent e) -> {
            ScriptManager.nextLine();
            WaveFormViewer.resetThis();
            Main.updateAllGUI();
        });

        miniPrevButton.addActionListener((ActionEvent e) -> {
            ScriptManager.prevLine();
            WaveFormViewer.resetThis();
            Main.updateAllGUI();
        });
        miniNextButton.addActionListener((ActionEvent e) -> {
            ScriptManager.nextLine();
            WaveFormViewer.resetThis();
            Main.updateAllGUI();
        });
    }

    // MARK: Reset Size
    void resetSize() {
        this.setSize(defaultWindowDimension);
    }
}
