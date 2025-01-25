package jp.f_matano44.jmars_recorder;

import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

final class MyClasses {
    public static final class UneditableTextArea extends JTextArea {
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


    public static final class MyStringBuilder {
        final StringBuilder sb = new StringBuilder();

        public void append(final String s) {
            this.sb.append(s);
        }

        public void appendLn(final String s) {
            this.sb.append(s).append(System.lineSeparator());
        }

        @Override public String toString() {
            return sb.toString();
        }
    }


    public abstract static class SuperIndexViewer extends JTextField {
        private int maxOfIndex = 0;
        private final int minOfIndex = 0;

        public SuperIndexViewer(final int defaultMax) {
            super("0 / 0");
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setBackground(null);
            this.setEditable(true);
            this.setFocusable(true);
            this.setBorder(new LineBorder(Color.BLACK, Main.lineBorderThickness));

            maxOfIndex = defaultMax;
        }

        public abstract void updateThisObj();

        public abstract void updateIndex();

        public void resetThis() {
            maxOfIndex = 0;
            this.setText("0 / 0");
        }

        public void updateMaxOfIndex(int newMax) {
            maxOfIndex = newMax;
        }

        protected final int getIndexFromText() throws NumberFormatException {
            final String[] sepString = this.getText().replace(" ", "").split("/");
            final int ansIndex = Integer.parseInt(sepString[0]) - 1;
            if (ansIndex < minOfIndex || maxOfIndex < ansIndex) {
                throw new NumberFormatException("Too small or too big.");
            } else {
                return ansIndex;
            }
        }
    }
}
