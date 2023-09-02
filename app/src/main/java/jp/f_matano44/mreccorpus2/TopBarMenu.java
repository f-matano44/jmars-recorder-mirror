package jp.f_matano44.mreccorpus2;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

final class TopBarMenu extends JMenuBar {
    private final AppConfig conf;
    private final MatanosRecorderForCorpus2 mainFrame;

    public TopBarMenu(AppConfig conf, MatanosRecorderForCorpus2 mainFrame) {
        this.conf = conf;
        this.mainFrame = mainFrame;
        this.add(this.new JMenumRecCorpus2());
        this.add(this.new JMenuHelp());
    }

    private class JMenumRecCorpus2 extends JMenu {
        public JMenumRecCorpus2() {
            super("mRecCorpus2");

            final JMenuItem openNewWindowItem = new JMenuItem("Preferences...");
            openNewWindowItem.addActionListener((ActionEvent e) -> conf.setVisible(true));
            this.add(openNewWindowItem);

            final JMenuItem quitItem = new JMenuItem("Quit mRecCorpus2");
            quitItem.addActionListener((ActionEvent e) -> System.exit(0));
            this.add(quitItem);
        }
    }

    private class JMenuHelp extends JMenu {
        public JMenuHelp() {
            super("Help");

            final JMenuItem resetWindowSize = new JMenuItem("Reset window size");
            resetWindowSize.addActionListener((ActionEvent e)
                -> mainFrame.setSize(mainFrame.defaultDimension));
            this.add(resetWindowSize);

            final JMenuItem noticeItem = new JMenuItem("3rd-party NOTICEs");
            noticeItem.addActionListener((ActionEvent e) -> new ThirdPartyNotice());
            this.add(noticeItem);
        }

        private class ThirdPartyNotice extends JFrame {
            public ThirdPartyNotice() {
                super("3rd-Party NOTICEs");

                final StringBuilder sb = new StringBuilder();
                final String[] libs = {"jFloatWavIO", "SourceHanCodeJP", "vlcj"};
                for (final String lib : libs) {
                    final InputStream is = TopBarMenu.class.getClassLoader()
                        .getResourceAsStream("3rdPartyNOTICEs/" + lib + ".txt");

                    sb.append(lib).append("\n\n");
                    try (Scanner sc = new Scanner(is)) {
                        while (sc.hasNextLine()) {
                            sb.append(sc.nextLine()).append("\n");
                        }
                    }
                    sb.append("\n").append("\n");
                }

                final JTextArea textArea = new JTextArea(sb.toString());
                Utility.setTextAreaSetting(textArea);
                textArea.setColumns(80);
                textArea.setRows(20);
                final JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setBackground(null);
                scrollPane.setAutoscrolls(true);
                scrollPane.getViewport().setBackground(null);
                SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
                final int blank = 20;
                scrollPane.setBorder(new EmptyBorder(blank, blank, blank, blank));

                Utility.changeFont(scrollPane);
                this.add(scrollPane);

                // Window setting
                this.pack();
                this.setResizable(false);
                this.setMinimumSize(getSize());
                this.setLocationRelativeTo(null);
                this.setVisible(true);
            }
        }
    }
}
