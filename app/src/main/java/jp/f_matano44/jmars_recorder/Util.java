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
// import java.net.URL;
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

    public static final void changeFont(Component component, final int fontSize) {
        Font font = null;
        try (
            final InputStream input = Main.class.getClassLoader()
                .getResourceAsStream("VL-PGothic-Regular.ttf")
        ) {
            font = Font.createFont(Font.TRUETYPE_FONT, input)
                .deriveFont(Font.BOLD, (float) fontSize);
        } catch (final FontFormatException | IOException e) {
            font = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
        }

        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                changeFont(child, fontSize);
            }
        }
    }

    public static final void setTextViewerSetting(JTextArea textArea) {
        Util.changeFont(textArea, AppConfig.fontSize);
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
            sb.insert(i, System.lineSeparator());
            i += 31;
        }
        return sb.toString();
    }

    public static void copyResource(
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
