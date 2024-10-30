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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import jp.f_matano44.jmars_recorder.Util.UneditableTextArea;


final class TopBarMenu extends JMenuBar {
    private final Main mainFrame;

    public TopBarMenu(Main mainFrame) {
        this.mainFrame = mainFrame;
        this.add(new FileTab());
        this.add(new WindowTab());
        this.add(new HelpTab());
    }

    private class FileTab extends JMenu {
        public FileTab() {
            super("File");

            final JMenuItem openScriptDirItem = new JMenuItem("Open Script File");
            openScriptDirItem.addActionListener((ActionEvent e) -> {
                try {
                    Desktop.getDesktop().open(new File(AppConfig.script.getParent()));
                } catch (final Exception ex) {
                    ex.printStackTrace(AppConfig.logTargetStream);
                }
            });
            this.add(openScriptDirItem);

            final JMenuItem openRefDirItem = new JMenuItem("Open Reference Directory");
            openRefDirItem.addActionListener((ActionEvent e) -> {
                try {
                    Desktop.getDesktop().open(AppConfig.reference);
                } catch (final Exception ex) {
                    ex.printStackTrace(AppConfig.logTargetStream);
                }
            });
            this.add(openRefDirItem);

            final JMenuItem openSaveDirItem = new JMenuItem("Open Save Directory");
            openSaveDirItem.addActionListener((ActionEvent e) -> {
                try {
                    Desktop.getDesktop().open(AppConfig.saveTo);
                } catch (final Exception ex) {
                    ex.printStackTrace(AppConfig.logTargetStream);
                }
            });
            this.add(openSaveDirItem);

            final JMenuItem openNewWindowItem = new JMenuItem("Configuration...");
            openNewWindowItem.addActionListener((ActionEvent e) -> new AppConfig());
            this.add(openNewWindowItem);

            final JMenuItem quitItem = new JMenuItem("Quit " + Main.appName);
            quitItem.addActionListener((ActionEvent e) -> System.exit(0));
            this.add(quitItem);
        }
    }

    private class WindowTab extends JMenu {
        public WindowTab() {
            super("Window");

            final JMenuItem resetWindowSize = new JMenuItem("Reset window size");
            resetWindowSize.addActionListener((ActionEvent e)
                -> mainFrame.setSize(mainFrame.defaultWindowDimension));
            this.add(resetWindowSize);
        }
    }

    private class HelpTab extends JMenu {
        public HelpTab() {
            super("Help");

            final JMenuItem appInfoItem = new JMenuItem("About " + Main.appName);
            appInfoItem.addActionListener((ActionEvent e) -> new AppInfo());
            this.add(appInfoItem);

            final JMenuItem noticeItem = new JMenuItem("3rd-party NOTICEs");
            noticeItem.addActionListener((ActionEvent e) -> new ThirdPartyNotice());
            this.add(noticeItem);
        }
    }

    private class AppInfo extends JFrame {
        public AppInfo() {
            super("");

            final StringBuilder sb = new StringBuilder();
            Util.appendLn(sb, Main.appName);
            Util.appendLn(sb, "");
            Util.appendLn(sb,
                "Version: " + Main.appVersion + " (" + Main.gitHEAD + ")");
            Util.appendLn(sb,
                "Build by: " + Util.insertNewLines(Main.buildBy));
            Util.appendLn(sb, "Build date: " + Main.buildDate);
            Util.appendLn(sb, "");
            Util.appendLn(sb, "License: " + Main.license);
            sb.append(Main.copyright);

            final JTextPane textPane = new JTextPane();
            textPane.setText(sb.toString());
            StyledDocument doc = textPane.getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
            Util.setFontRecursive(textPane, AppConfig.fontSize);
            textPane.setEditable(false);
            textPane.setFocusable(false);
            textPane.setBackground(null);
            textPane.setBorder(null);
            textPane.setAutoscrolls(false);
            final int blank = 20;
            textPane.setBorder(new EmptyBorder(blank, blank, blank, blank));
            this.add(textPane);

            // Window setting
            this.pack();
            this.setPreferredSize(getPreferredSize());
            this.setResizable(false);
            this.setMinimumSize(getSize());
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }
    }

    private class ThirdPartyNotice extends JFrame {
        public ThirdPartyNotice() {
            super("3rd-Party NOTICEs");

            final StringBuilder sb = new StringBuilder();
            final String[] libs
                = {"jFloatWavIO", "LICENSE_E.mplus", "ROHAN",
                    "SnakeYAML", "vlcj", "VLGothic.en"};
            for (final String lib : libs) {
                final InputStream is = TopBarMenu.class.getClassLoader()
                    .getResourceAsStream("3rdPartyNOTICEs/" + lib + ".txt");

                Util.appendLn(sb, lib);
                try (final Scanner sc = new Scanner(is)) {
                    while (sc.hasNextLine()) {
                        Util.appendLn(sb, sc.nextLine());
                    }
                }
                Util.appendLn(sb, "");
                Util.appendLn(sb, "");
            }

            final JTextArea textArea = new UneditableTextArea(sb.toString());
            textArea.setColumns(80);
            textArea.setRows(20);
            final JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setBackground(null);
            scrollPane.setAutoscrolls(true);
            scrollPane.getViewport().setBackground(null);
            SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(0));
            final int blank = 20;
            scrollPane.setBorder(new EmptyBorder(blank, blank, blank, blank));

            Util.setFontRecursive(scrollPane, AppConfig.fontSize);
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
