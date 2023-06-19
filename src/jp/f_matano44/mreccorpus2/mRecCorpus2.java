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
            final DataClass dat = new DataClass();
            final JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();

            // mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            gbc.gridx = 0; gbc.gridy = 0;
            mainPanel.add(new CorpusReaderPanel(), gbc);
            gbc.gridx = 0; gbc.gridy = 1;
            mainPanel.add(new Recorder(dat), gbc);
            mainPanel.setBorder(
                new EmptyBorder(10, 15, 10, 15)
            );
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
        // Allow to change width only --------------------------------------------
        setMinimumSize(getSize());
        // setMaximumSize(new Dimension(Short.MAX_VALUE, getSize().height));
        // Allow to change width only --------------------------------------------
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
