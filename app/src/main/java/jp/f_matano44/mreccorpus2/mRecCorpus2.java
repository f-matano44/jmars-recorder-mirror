/*
 * mRecCorpus2
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

package jp.f_matano44.mreccorpus2;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.sound.sampled.AudioFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class mRecCorpus2 extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new mRecCorpus2());
    }

    final int[] currentIndex = {0};
    final AppConfig conf = new AppConfig();
    final RecorderBody recorder = new RecorderBody(conf, currentIndex);
    final ScriptsManager sm = new ScriptsManager(recorder, conf, currentIndex);

    public mRecCorpus2() {
        // Window title
        super("mRecCorpus2");

        // Create panels
        try {
            // main panel setting
            final JPanel mainPanel = new JPanel(new GridBagLayout());
            final GridBagConstraints gbc = new GridBagConstraints();
            final EmptyBorder blank = new EmptyBorder(15, 20, 15, 20);
            mainPanel.setBorder(blank);

            final JPanel[] xPanels = new JPanel[2];
            xPanels[0] = new JPanel(new GridBagLayout());
            xPanels[1] = new JPanel(new GridBagLayout());

            gbc.gridx = 0;
            gbc.gridy = 0;
            xPanels[0].add(this.new ConfigPanel(conf.format), gbc);

            gbc.gridx = 0;
            gbc.gridy = 0;
            xPanels[1].add(sm.scriptsPathViewerArea, gbc);
            gbc.gridy++;
            xPanels[1].add(sm.scriptViewer, gbc);
            gbc.gridy++;
            xPanels[1].add(this.new ScriptChooserPanel(), gbc);
            gbc.gridy++;
            xPanels[1].add(new RecorderPanel(recorder), gbc);
            gbc.gridy++;
            xPanels[1].add(sm.saveToViewerArea, gbc);

            gbc.gridx = 0;
            gbc.gridy = 0;
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


    private class ScriptChooserPanel extends JPanel {
        public ScriptChooserPanel() {
            final JButton prevButton = new JButton("<< Prev");
            prevButton.addActionListener((ActionEvent e) -> sm.prevLine());
            final JButton nextButton = new JButton("Next >>");
            nextButton.addActionListener((ActionEvent e) -> sm.nextLine());

            this.setLayout(new GridLayout(1, 3));
            this.add(prevButton);
            this.add(sm.indexLabel);
            this.add(nextButton);
        }
    }


    private class ConfigPanel extends JPanel {
        public ConfigPanel(AudioFormat format) {
            final int fs = (int) format.getSampleRate();
            final int nbits = format.getSampleSizeInBits();
            final int channels = format.getChannels();

            final StringBuilder sb = new StringBuilder();
            sb.append("Sampling rate (Fs)\n");
            sb.append("> " + fs + "[Hz]\n");
            sb.append("\n");
            sb.append("Bit depth (nBits)\n");
            sb.append("> " + nbits + "[bit]\n");
            sb.append("\n");
            sb.append("Channels\n");
            sb.append("> " + channels);

            final JTextArea configViewer = new JTextArea(sb.toString());
            // panel setting
            configViewer.setEditable(false);   
            configViewer.setBackground(null);
            configViewer.setBorder(null);
            configViewer.setLineWrap(false);
            configViewer.setAutoscrolls(false);
            adjustTextAreaSize(configViewer);
            this.add(configViewer);

            final CompoundBorder border = new CompoundBorder(
                new EmptyBorder(0, 0, 0, 5),
                BorderFactory.createTitledBorder("Config")
            );
            this.setBorder(border);
        }

        private static void adjustTextAreaSize(JTextArea textArea) {
            final int width = (int) textArea.getPreferredSize().getWidth();
            final int height = textArea.getPreferredSize().height;

            // テキストエリアのサイズを設定
            textArea.setSize(width, height);
            textArea.setPreferredSize(new Dimension(width, height));
            textArea.setMinimumSize(new Dimension(width, height));
        }
    }
}
