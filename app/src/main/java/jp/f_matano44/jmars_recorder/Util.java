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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.swing.JTextArea;
import javax.swing.UIManager;


final class Util {
    private Util() {
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
        final URL fontURL = Util.class.getClassLoader()
            .getResource("SourceHanCodeJP-Medium.otf");
        try (final InputStream is = fontURL.openStream()) {
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
        Util.changeFont(textArea);
        textArea.setWrapStyleWord(true);   
        textArea.setLineWrap(true);        
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBackground(null);
        textArea.setBorder(null);
        textArea.setAutoscrolls(false);
    }

    public static void appendLn(
        final StringBuilder sb, final String toAppend
    ) {
        sb.append(toAppend).append(System.lineSeparator());
    }

    public static String insertNewLines(
        final String text
    ) {
        final StringBuilder sb = new StringBuilder(text);
        int i = 30;
        while (i < sb.length()) {
            sb.insert(i, '\n');
            i += 31;
        }
        return sb.toString();
    }

    public static void copyResourceToFile(
        final String resourcePath, final String absolutePath
    ) throws IOException {
        try (
            InputStream inputStream = Util.class.getClassLoader()
                .getResourceAsStream(resourcePath);
            OutputStream outputStream = new FileOutputStream(absolutePath)
        ) {
            if (inputStream == null) {
                throw new IOException("リソースが見つかりません: " + resourcePath);
            }

            final byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
}
