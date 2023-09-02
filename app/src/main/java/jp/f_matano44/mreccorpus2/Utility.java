package jp.f_matano44.mreccorpus2;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        final URL resource = Utility.class.getClassLoader()
            .getResource("SourceHanCodeJP-Medium.otf");
        try (final InputStream is = resource.openStream()) {
            final Font font = Font.createFont(Font.TRUETYPE_FONT, is)
                .deriveFont(14f);
            component.setFont(font);
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    changeFont(child);
                }
            }
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    public static final void setTextAreaSetting(JTextArea textArea) {
        textArea.setWrapStyleWord(true);   
        textArea.setLineWrap(true);        
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBackground(null);
        textArea.setBorder(null);
        textArea.setAutoscrolls(false);
    }
}
