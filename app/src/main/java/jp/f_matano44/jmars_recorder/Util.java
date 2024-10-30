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
import javax.swing.JTextArea;


final class Util {
    private static final Font font;

    private Util() {
        /* Nothing to do. */
    }

    static {
        Font tempFont = null;
        try (
            final InputStream input = Util.class.getClassLoader()
                .getResourceAsStream("VL-PGothic-Regular.ttf")
        ) {
            tempFont = Font.createFont(Font.TRUETYPE_FONT, input)
                .deriveFont(Font.BOLD, (float) AppConfig.fontSize);
        } catch (final FontFormatException | IOException e) {
            tempFont = new Font(Font.MONOSPACED, Font.PLAIN, (int) AppConfig.fontSize);
        }
        font = tempFont;
    }

    public static final void setFontRecursive(final Component component, final float fontSize) {
        component.setFont(font.deriveFont(fontSize));
        if (component instanceof Container) {
            for (final Component child : ((Container) component).getComponents()) {
                setFontRecursive(child, fontSize);
            }
        }
    }

    public static class UneditableTextArea extends JTextArea {
        public UneditableTextArea() {
            super();
            this.setAllConfig();
        }

        public UneditableTextArea(final String str) {
            super(str);
            this.setAllConfig();
        }

        private void setAllConfig() {
            // 文字を単語単位で折返す（日本語での恩恵は多分無い）
            this.setWrapStyleWord(true);
            this.setLineWrap(true);
            // 変更できるか？
            this.setEditable(false);
            this.setFocusable(false);
            // TextArea のデザイン
            this.setBackground(null);
            this.setBorder(null);
            // その他
            this.setAutoscrolls(false);
        }
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
