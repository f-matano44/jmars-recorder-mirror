package jp.f_matano44.mreccorpus2;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

class InfoTextArea extends javax.swing.JTextArea {
    public InfoTextArea(final String title, final String st) {
        this.setText(st);
        this.setEditable(false);   
        this.setBackground(null);
        this.setBorder(null);
        this.setLineWrap(false);
        this.setAutoscrolls(false);
        this.setFont(AppConfig.fontSet);

        final Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 1);
        final CompoundBorder outerBorder = new CompoundBorder(
            new EmptyBorder(0, 0, 0, 5),
            BorderFactory.createTitledBorder(lineBorder, title)
        );
        final CompoundBorder border = new CompoundBorder(
            outerBorder,
            new EmptyBorder(0, 3, 3, 3)
        );
        this.setBorder(border);
    }
}
