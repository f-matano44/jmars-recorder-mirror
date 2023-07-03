package jp.f_matano44.mreccorpus2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class mRecCorpus2 extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new mRecCorpus2());
    }

    public mRecCorpus2() {
        // Window title
        super("mRecCorpus2");

        // add panel
        try {
            // main panel setting
            final JPanel mainPanel = new JPanel(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();
            final EmptyBorder blank = new EmptyBorder(15, 20, 15, 20);
            mainPanel.setBorder(blank);

            final RecorderBody recorder = new RecorderBody();
            final FileInOutPanel fileio = new FileInOutPanel(recorder);
            final RecorderPanel rp = new RecorderPanel(recorder, fileio);
            // final JPanel sSection = recorder.speechSectionPanel();

            gbc.gridx = 0;
            gbc.gridy = 0;
            final JPanel[] xPanels = new JPanel[2];
            xPanels[0] = new JPanel(new GridBagLayout());
            xPanels[0].add(rp.ConfigPanel(), gbc);

            gbc.gridx = 0;
            gbc.gridy = 0;
            xPanels[1] = new JPanel(new GridBagLayout());
            xPanels[1].add(fileio.scriptViewerPanel(), gbc);
            gbc.gridy++;
            xPanels[1].add(fileio.scriptChooserPanel(), gbc);
            gbc.gridy++;
            xPanels[1].add(rp.ControlPanel(), gbc);
            gbc.gridy++;
            xPanels[1].add(fileio.selectSaveDirectory(), gbc);
            // gbc.gridy++;
            // xPanels[1].add(sSection, gbc);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            mainPanel.add(fileio.corpusLoadButton(), gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            mainPanel.add(xPanels[0], gbc);
            gbc.gridx++;
            mainPanel.add(xPanels[1], gbc);
            add(mainPanel);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                null, ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }

        // Window setting
        pack();
        setMinimumSize(getSize());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
