package jp.f_matano44.mreccorpus2;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.UIManager;


final class Utility {
    private Utility() {
        /* Nothing to do. */
    }

    public static final void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void changeFont(Component component) {
        final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                changeFont(child);
            }
        }
    }

    public static final void setTextAreaSetting(JTextArea textArea) {
        textArea.setWrapStyleWord(true);   
        textArea.setLineWrap(true);        
        textArea.setEditable(false);   
        textArea.setBackground(null);
        textArea.setBorder(null);
        textArea.setAutoscrolls(false);
    }
}
