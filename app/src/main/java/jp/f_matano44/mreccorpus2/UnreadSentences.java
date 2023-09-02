package jp.f_matano44.mreccorpus2;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

class UnreadSentences extends JPanel {
    private final int corpusSize;
    private final JTextArea textArea;
    private final JScrollPane scrollPane;

    public UnreadSentences(final int corpusSize) {
        this.corpusSize = corpusSize;

        this.textArea = new JTextArea();
        Utility.setTextAreaSetting(this.textArea);
        this.textArea.setColumns(15);
        this.textArea.setRows(25);
        this.scrollPane = new JScrollPane(this.textArea);
        this.scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.scrollPane.setBackground(null);
        this.scrollPane.getViewport().setBackground(null);
        SwingUtilities.invokeLater(
            () -> this.scrollPane.getVerticalScrollBar().setValue(0));

        this.update();
        this.add(this.scrollPane);
    }

    public void update() {
        final List<String> list = new ArrayList<>();
        for (int i = 0; i < this.corpusSize; i++) {
            final File check = AppConfig.getSavePath(i);
            if (!check.exists()) {
                list.add(String.valueOf(i + 1));
            }
        }

        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            "Rem: " + list.size() + "/" + corpusSize
        ));
        final StringBuilder sb = new StringBuilder("");
        for (final String st : list) {
            sb.append(st).append("\n");
        }

        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        final int oldMax = scrollBar.getMaximum();
        final int oldValue = scrollBar.getValue();
        this.textArea.setText(sb.toString());
        final int newMax = scrollBar.getMaximum();
        final int newValue = (int) (((double) oldValue / oldMax) * newMax);
        SwingUtilities.invokeLater(() -> scrollBar.setValue(newValue));
    }
}
