package taskmanager.ui;

import taskmanager.auth.AuthService;
import taskmanager.model.User;
import taskmanager.singleton.AppConfig;
import taskmanager.ui.components.Theme;
import taskmanager.ui.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Settings panel - admin only view.
 */
public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setBorder(new EmptyBorder(24, 28, 16, 28));
        header.add(UI.label("Settings", Theme.FONT_TITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_DARK);
        content.setBorder(new EmptyBorder(0, 20, 20, 20));

        // App config card
        JPanel cfgCard = UI.card(new GridBagLayout());
        cfgCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        cfgCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1; gc.insets = new Insets(6,0,6,0);

        AppConfig cfg = AppConfig.getInstance();

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2;
        cfgCard.add(UI.label("Application Configuration", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), gc);
        gc.gridwidth=1;

        gc.gridy=1; gc.gridx=0; gc.weightx=0;
        cfgCard.add(UI.label("Data File:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        JTextField dataField = UI.textField(cfg.getDataFilePath());
        dataField.setPreferredSize(new Dimension(0, 36));
        cfgCard.add(dataField, gc);

        gc.gridy=2; gc.gridx=0; gc.weightx=0;
        cfgCard.add(UI.label("Log File:", Theme.FONT_SUBHEAD, Theme.TEXT_SECONDARY), gc);
        gc.gridx=1; gc.weightx=1;
        JTextField logField = UI.textField(cfg.getLogFilePath());
        logField.setEditable(false); logField.setForeground(Theme.TEXT_MUTED);
        cfgCard.add(logField, gc);

        gc.gridy=3; gc.gridx=0; gc.weightx=0; gc.gridwidth=2;
        JButton saveBtn = UI.button("Save Settings", Theme.ACCENT_BLUE);
        saveBtn.setPreferredSize(new Dimension(150, 38));
        saveBtn.addActionListener(e -> {
            cfg.setDataFilePath(dataField.getText().trim());
            JOptionPane.showMessageDialog(this, "Settings saved!", "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        JPanel saveBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtnWrap.setOpaque(false);
        saveBtnWrap.add(saveBtn);
        cfgCard.add(saveBtnWrap, gc);

        content.add(cfgCard);
        content.add(Box.createVerticalStrut(16));

        // Users card
        JPanel usersCard = UI.card(new BorderLayout(0, 12));
        usersCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        usersCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        usersCard.add(UI.label("User Accounts", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), BorderLayout.NORTH);

        JPanel usersGrid = new JPanel(new GridLayout(0, 1, 0, 8));
        usersGrid.setOpaque(false);
        for (User u : AuthService.getInstance().getAllUsers()) {
            JPanel row = new JPanel(new BorderLayout(14, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(8, 10, 8, 10));
            row.setBackground(Theme.BG_ELEVATED);

            // Avatar
            JPanel av = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color c = u.isAdmin() ? Theme.ACCENT_ORANGE : Theme.ACCENT_PURPLE;
                    g2.setColor(Theme.withAlpha(c, 35));
                    g2.fillOval(0,0,getWidth(),getHeight());
                    g2.setColor(c); g2.setFont(new Font("Segoe UI",Font.BOLD,11));
                    FontMetrics fm = g2.getFontMetrics();
                    String i = u.getAvatarInitials();
                    g2.drawString(i,(getWidth()-fm.stringWidth(i))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            av.setPreferredSize(new Dimension(32, 32)); av.setOpaque(false);

            JLabel name = UI.label(u.getDisplayName(), Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
            JLabel user = UI.label("@" + u.getUsername(), Theme.FONT_SMALL, Theme.TEXT_MUTED);
            JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setOpaque(false);
            info.add(name); info.add(user);

            Color rc = u.isAdmin() ? Theme.ACCENT_ORANGE : Theme.ACCENT_PURPLE;
            JLabel role = UI.badge(u.getRole().toString(), rc);

            row.add(av,   BorderLayout.WEST);
            row.add(info, BorderLayout.CENTER);
            row.add(role, BorderLayout.EAST);
            usersGrid.add(row);
        }
        usersCard.add(usersGrid, BorderLayout.CENTER);
        content.add(usersCard);
        content.add(Box.createVerticalStrut(16));

        // About card
        JPanel aboutCard = UI.card(new GridBagLayout());
        aboutCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        aboutCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        gc = new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; gc.gridy=0; gc.insets=new Insets(4,0,4,0);
        aboutCard.add(UI.label("About", Theme.FONT_HEADING, Theme.TEXT_PRIMARY), gc);
        gc.gridy=1; aboutCard.add(UI.label("TaskManager Pro — SET11103 Coursework", Theme.FONT_BODY, Theme.TEXT_SECONDARY), gc);
        gc.gridy=2; aboutCard.add(UI.label("Version " + cfg.getVersion() + " | Patterns: Singleton · Observer · Strategy · Command", Theme.FONT_SMALL, Theme.TEXT_MUTED), gc);
        content.add(aboutCard);

        add(header, BorderLayout.NORTH);
        add(UI.scroll(content), BorderLayout.CENTER);
    }
}
