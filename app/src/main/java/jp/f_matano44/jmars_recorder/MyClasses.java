package jp.f_matano44.jmars_recorder;

import javax.swing.JTextArea;
import javax.swing.JTextField;

class MyClasses {
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


    public abstract static class SuperIndexViewer extends JTextField {

    }
}
