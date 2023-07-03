package jp.f_matano44.mreccorpus2;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;


class FileInOutPanel extends JPanel {
    private final RecorderBody recorder;

    private JLabel index;
    private final JTextArea scriptViewer;
    private final JButton loadButton;
    private final JButton saveToButton;
    public final JButton saveButton;
    private final JTextArea saveToViewerArea;

    private final int textAreaWidth = 40;
    private List<String> lines;
    private int currentIndex;
    private File saveTo;

    public FileInOutPanel(RecorderBody recorder) {
        this.recorder = recorder;

        this.lines = new ArrayList<>(Arrays.asList("Select corpus file using above button."));
        this.currentIndex = 0;
        this.index = new JLabel();
        this.index.setHorizontalAlignment(SwingConstants.CENTER);
        this.scriptViewer = new JTextArea();
        this.saveTo = new File(System.getProperty("user.home"));

        // コーパス読み込みボタン
        this.loadButton = new JButton("Select corpus to Load");
        this.loadButton.addActionListener((ActionEvent e) -> chooseFile());
        this.loadButton.setAlignmentX(CENTER_ALIGNMENT);

        // save signal
        this.saveToButton = new JButton("Save to");
        this.saveToViewerArea = new JTextArea("");
        this.saveToButton.addActionListener((ActionEvent e) -> directoryChooser());
        this.saveButton = new JButton("Save");
        this.saveButton.addActionListener((ActionEvent e) -> saveAsWav());

        // set default value
        this.updateText();
    }

    private File getSaveToFile() {
        return new File(
            saveTo, 
            "corpus_" + String.format("%04d", currentIndex+1) + ".wav"
        );
    }

    private void updateText() {
        index.setText(String.valueOf(currentIndex+1));
        scriptViewer.setText(lines.get(currentIndex));
        saveToViewerArea.setText(this.getSaveToFile().getAbsolutePath());
    }

    public JButton corpusLoadButton() {
        return loadButton;
    }

    public JTextArea scriptViewerPanel() {
        scriptViewer.setWrapStyleWord(true);   
        scriptViewer.setLineWrap(true);        
        scriptViewer.setEditable(false);   
        scriptViewer.setBackground(null);
        scriptViewer.setBorder(new LineBorder(Color.GRAY, 1, true));
        // ここの数字は決め打ち
        scriptViewer.setColumns(textAreaWidth);
        scriptViewer.setRows(8);
        return scriptViewer;
    }

    public JPanel scriptChooserPanel() {
        final JButton prevButton = new JButton("<< Prev");
        prevButton.addActionListener((ActionEvent e) -> prevLine());
        final JButton nextButton = new JButton("Next >>");
        nextButton.addActionListener((ActionEvent e) -> nextLine());
        final JPanel controlPanel = new JPanel(new GridLayout(1, 3));
        controlPanel.add(prevButton);
        controlPanel.add(index);
        controlPanel.add(nextButton);
        return controlPanel;
    }

    private void nextLine() {
        currentIndex++;
        if(currentIndex >= lines.size()) {
            currentIndex = 0;
        }
        updateText();
    }

    private void prevLine() {
        if (currentIndex <= 0) {
            currentIndex = lines.size();
        }
        currentIndex--;
        updateText();
    }

    private void chooseFile() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(
            "Select corpus that you want to record.");

        final FileNameExtensionFilter filter =
            new FileNameExtensionFilter("TXT files", "txt");
        fileChooser.setFileFilter(filter);

        final int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            readFile(filename);
        }
    }

    private void readFile(String filename) {
        try (
            final var reader = new BufferedReader(new FileReader(filename))
        ) {
            String line;
            lines = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                final String[] parts = line.split("[\t,:]");
                final String lineHTML;
                if (parts.length <= 2) {
                    lineHTML = parts[1];
                } else {
                    lineHTML = parts[1] + "\n" + parts[2];
                }
                lines.add(lineHTML);
            }
        } catch (IOException e) {
            scriptViewer.setText("Error in reading file");
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }

        if (!lines.isEmpty()) {
            updateText();
        } else {
            scriptViewer.setText("No lines in the file");
        }
    }

    public JPanel selectSaveDirectory() {
        final JPanel builder = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        builder.add(saveToButton, gbc);
        gbc.gridx++;
        builder.add(saveToViewerPanel(), gbc);

        return builder;
    }

    public JTextArea saveToViewerPanel() {
        saveToViewerArea.setWrapStyleWord(true);   
        saveToViewerArea.setLineWrap(true);        
        saveToViewerArea.setEditable(false);   
        saveToViewerArea.setBackground(null);
        saveToViewerArea.setBorder(new LineBorder(Color.BLACK, 1, true));
        // ここの数字は決め打ち
        saveToViewerArea.setColumns(textAreaWidth - 7);
        saveToViewerArea.setRows(1);
        return saveToViewerArea;
    }

    private void directoryChooser() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            saveTo = chooser.getSelectedFile();
            saveToViewerArea.setText(this.getSaveToFile().getAbsolutePath());
        }
    }

    public void saveAsWav(){
        final File file = this.getSaveToFile();
        final byte[] audio = this.recorder.getByteSignal();
        final AudioFormat format = this.recorder.getFormat();

        try (
            final AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audio),
                format, audio.length / format.getFrameSize()
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
