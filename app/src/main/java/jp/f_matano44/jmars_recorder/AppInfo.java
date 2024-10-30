package jp.f_matano44.jmars_recorder;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


class AppInfo extends JFrame {
    public static final String name;
    public static final String version;
    public static final String buildBy;
    public static final String buildDate;
    public static final String gitHASH;
    public static final String copyright;
    public static final String license;


    static {
        String tempAppName = "unknown";
        String tempAppVersion = "unknown";
        String tempBuildBy = "unknown";
        String tempBuildDate = "unknown";
        String tempGitHEAD = "unknown";
        String tempCopyright = "unknown";
        String tempLicense = "unknown";

        try (InputStream input = AppInfo.class.getClassLoader()
            .getResourceAsStream("build-info.properties")
        ) {
            final Properties prop = new Properties();
            prop.load(input);
            tempAppName = prop.getProperty("app.name");
            tempAppVersion = prop.getProperty("app.version");
            tempBuildBy = prop.getProperty("build.by");
            tempBuildDate = prop.getProperty("build.date");
            tempGitHEAD = prop.getProperty("git.head");
            tempCopyright = prop.getProperty("copyright");
            tempLicense = prop.getProperty("license");
        } catch (final IOException e) {
            tempAppName = "unknown";
            tempAppVersion = "unknown";
            tempBuildBy = "unknown";
            tempBuildDate = "unknown";
            tempGitHEAD = "unknown";
            tempCopyright = "unknown";
            tempLicense = "unknown";
        }
        name = tempAppName;
        version = tempAppVersion;
        buildBy = tempBuildBy;
        buildDate = tempBuildDate;
        gitHASH = tempGitHEAD;
        copyright = tempCopyright;
        license = tempLicense;
    }


    public AppInfo() {
        super("");

        final StringBuilder sb = new StringBuilder();
        Util.appendLn(sb, AppInfo.name);
        Util.appendLn(sb, "");
        Util.appendLn(sb,
            "Version: " + AppInfo.version + " (" + AppInfo.gitHASH + ")");
        Util.appendLn(sb,
            "Build by: " + Util.insertNewLines(AppInfo.buildBy));
        Util.appendLn(sb, "Build date: " + AppInfo.buildDate);
        Util.appendLn(sb, "");
        Util.appendLn(sb, "License: " + AppInfo.license);
        sb.append(AppInfo.copyright);

        final JTextPane textPane = new JTextPane();
        textPane.setText(sb.toString());
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        Util.setFontRecursive(textPane, AppConfig.fontSize);
        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setBackground(null);
        textPane.setBorder(null);
        textPane.setAutoscrolls(false);
        final int blank = 20;
        textPane.setBorder(new EmptyBorder(blank, blank, blank, blank));
        this.add(textPane);

        // Window setting
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
