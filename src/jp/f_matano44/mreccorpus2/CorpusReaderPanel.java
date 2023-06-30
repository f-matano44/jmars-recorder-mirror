package jp.f_matano44.mreccorpus2;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;


class CorpusReaderPanel extends JPanel {
    private List<String> lines;
    private int currentIndex;
    private JLabel script;
    private JLabel index;

    public CorpusReaderPanel() {
        lines = new ArrayList<>();
        currentIndex = 0;
        index = new JLabel();
        script = new JLabel();

        // set default value
        index.setText("1");
        script.setText("Restart and Select corpus file.");

        // script panel
        final JPanel scriptPanel = new JPanel(new FlowLayout());
        scriptPanel.add(script);

        // control panel
        final JButton prevButton = new JButton("<< Prev");
        prevButton.addActionListener((ActionEvent e) -> prevLine());
        final JButton nextButton = new JButton("Next >>");
        nextButton.addActionListener((ActionEvent e) -> nextLine());
        final JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(prevButton);
        controlPanel.add(index);
        controlPanel.add(nextButton);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scriptPanel);
        add(controlPanel);

        chooseFile();
    }

    private void nextLine() {
        currentIndex++;
        if(currentIndex >= lines.size()) {
            currentIndex = 0;
        }

        index.setText(String.valueOf(currentIndex+1));
        script.setText(lines.get(currentIndex));
    }

    private void prevLine() {
        if (currentIndex <= 0) {
            currentIndex = lines.size();
        }
        currentIndex--;

        index.setText(String.valueOf(currentIndex+1));
        script.setText(lines.get(currentIndex));
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
            while ((line = reader.readLine()) != null) {
                final String[] parts = line.split("[\t,:]");
                final String lineHTML;
                if (parts.length <= 2) {
                    lineHTML = "<html><center>\u3000" + parts[1] 
                        + "</center></html>";
                } else {
                    lineHTML = "<html><center>\u3000" + parts[1]
                        + "<br>\u3000" + parts[2] + "</center></html>";
                }
                lines.add(lineHTML);
            }
        } catch (IOException e) {
            script.setText("Error reading file");
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }

        if (!lines.isEmpty()) {
            script.setText(lines.get(currentIndex));
        } else {
            script.setText("No lines in the file");
        }
    }
}
