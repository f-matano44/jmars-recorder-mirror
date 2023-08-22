package jp.f_matano44.mreccorpus2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

class UnreadScripts extends JPanel {
    private final AppConfig conf;
    private final int corpusSize;
    private final JTextArea textArea;
    private final JScrollPane scrollPane;

    public UnreadScripts(AppConfig conf, int corpusSize) {
        Utility.setLookAndFeel();
        this.conf = conf;
        this.corpusSize = corpusSize;

        this.textArea = new JTextArea();
        Utility.setTextAreaSetting(this.textArea);
        this.textArea.setColumns(15);
        this.textArea.setRows(30);
        this.scrollPane = new JScrollPane(this.textArea);
        this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.scrollPane.setBackground(null);
        this.scrollPane.getViewport().setBackground(null);
        SwingUtilities.invokeLater(() -> this.scrollPane.getVerticalScrollBar().setValue(0));

        this.update();
        this.add(this.scrollPane);
    }

    public void update() {
        final List<String> list = new ArrayList<>();
        for (int i = 1; i <= this.corpusSize; i++) {
            final File check = conf.getSavePath(i);
            if (!check.exists()) {
                list.add(String.valueOf(i));
            }
        }

        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        final int oldMax = scrollBar.getMaximum();
        final int oldValue = scrollBar.getValue();
        scrollPane.setBorder(new TitledBorder("Rem: " + list.size() + "/" + corpusSize));
        String sb = "";
        for (String st : list) {
            sb += st + "\n";
        }
        this.textArea.setText(sb);
        final int newMax = scrollBar.getMaximum();
        final int newValue = (int) ((double) oldValue / oldMax * newMax);
        SwingUtilities.invokeLater(() -> scrollBar.setValue(newValue));
    }
}
