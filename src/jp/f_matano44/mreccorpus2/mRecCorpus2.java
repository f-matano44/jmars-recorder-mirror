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
        super("mRecCorpus");

        // add panel
        try {
            final RecorderBody recorder = new RecorderBody();
            final RecorderPanel rp = new RecorderPanel(recorder);
            final JPanel mainPanel = new JPanel(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();
            final var blank = new EmptyBorder(10, 15, 10, 15);
            mainPanel.setBorder(blank);

            gbc.gridx = 0; gbc.gridy = 0;
            mainPanel.add(rp.ConfigPanel(), gbc);
            gbc.gridx = 0; gbc.gridy = 1;
            mainPanel.add(new CorpusReaderPanel(), gbc);
            gbc.gridx = 0; gbc.gridy = 2;
            mainPanel.add(rp.ControlPanel(), gbc);
    
            add(mainPanel);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                null, ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }

        // Window setting
        pack();
        setSize(800, getSize().height);
        setMinimumSize(getSize());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
