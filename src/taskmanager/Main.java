package taskmanager;

import taskmanager.ui.MainFrame;
import taskmanager.ui.components.Theme;

import javax.swing.*;

/**
 * Application entry point — SET11103 Coursework v2.
 */
public class Main {

    public static void main(String[] args) {
        // Force dark L&F baseline
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global dark UI defaults
        UIManager.put("Panel.background",          Theme.BG_DARK);
        UIManager.put("OptionPane.background",     Theme.BG_CARD);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY);
        UIManager.put("Button.background",         Theme.BG_ELEVATED);
        UIManager.put("Button.foreground",         Theme.TEXT_PRIMARY);
        UIManager.put("Label.foreground",          Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.background",       Theme.BG_ELEVATED);
        UIManager.put("ComboBox.foreground",       Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", Theme.ACCENT_BLUE);
        UIManager.put("ComboBox.selectionForeground", Theme.TEXT_INVERSE);
        UIManager.put("TextField.background",      Theme.BG_ELEVATED);
        UIManager.put("TextField.foreground",      Theme.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", Theme.ACCENT_BLUE);
        UIManager.put("TextArea.background",       Theme.BG_ELEVATED);
        UIManager.put("TextArea.foreground",       Theme.TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",     Theme.BG_DARK);
        UIManager.put("Viewport.background",       Theme.BG_DARK);
        UIManager.put("Table.background",          Theme.BG_DARK);
        UIManager.put("Table.foreground",          Theme.TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", Theme.BG_HOVER);
        UIManager.put("Table.selectionForeground", Theme.TEXT_PRIMARY);
        UIManager.put("TableHeader.background",    Theme.BG_SIDEBAR);
        UIManager.put("TableHeader.foreground",    Theme.TEXT_MUTED);
        UIManager.put("Slider.background",         Theme.BG_DARK);
        UIManager.put("CheckBox.background",       Theme.BG_CARD);
        UIManager.put("CheckBox.foreground",       Theme.TEXT_PRIMARY);
        UIManager.put("ProgressBar.background",    Theme.BG_ELEVATED);
        UIManager.put("ProgressBar.foreground",    Theme.ACCENT_BLUE);
        UIManager.put("ToolTip.background",        Theme.BG_ELEVATED);
        UIManager.put("ToolTip.foreground",        Theme.TEXT_PRIMARY);
        UIManager.put("PopupMenu.background",      Theme.BG_ELEVATED);
        UIManager.put("MenuItem.background",       Theme.BG_ELEVATED);
        UIManager.put("MenuItem.foreground",       Theme.TEXT_PRIMARY);
        UIManager.put("MenuItem.selectionBackground", Theme.ACCENT_BLUE);
        UIManager.put("MenuItem.selectionForeground", Theme.TEXT_INVERSE);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
