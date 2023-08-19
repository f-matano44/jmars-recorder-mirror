package jp.f_matano44.mreccorpus2;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

class TopBarMenu extends JMenuBar {
    public TopBarMenu(AppConfig conf) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JMenuItem openNewWindowItem = new JMenuItem("Show recording configuration");
        openNewWindowItem.addActionListener(e -> conf.setVisible(true));
        
        JMenu menu = new JMenu("Help");
        menu.add(openNewWindowItem);
        this.add(menu);
    }
}
