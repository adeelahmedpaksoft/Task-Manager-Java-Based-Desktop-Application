package taskmanager.ui.components;

import java.awt.*;

/**
 * Centralised dark-theme colour palette and font definitions.
 */
public final class Theme {

    private Theme() {}

    // ── Backgrounds ──────────────────────────────────────────────────────────
    public static final Color BG_DARKEST  = new Color(10,  12,  20);
    public static final Color BG_DARK     = new Color(15,  18,  30);
    public static final Color BG_CARD     = new Color(22,  27,  44);
    public static final Color BG_ELEVATED = new Color(30,  36,  58);
    public static final Color BG_HOVER    = new Color(38,  46,  72);
    public static final Color BG_SIDEBAR  = new Color(13,  16,  26);

    // ── Accents ──────────────────────────────────────────────────────────────
    public static final Color ACCENT_BLUE    = new Color(99,  179, 237);
    public static final Color ACCENT_PURPLE  = new Color(159, 122, 234);
    public static final Color ACCENT_GREEN   = new Color(72,  199, 142);
    public static final Color ACCENT_ORANGE  = new Color(246, 173, 85);
    public static final Color ACCENT_RED     = new Color(252, 129, 74);
    public static final Color ACCENT_CYAN    = new Color(76,  201, 240);
    public static final Color ACCENT_PINK    = new Color(237, 100, 166);

    // ── Text ─────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(237, 242, 247);
    public static final Color TEXT_SECONDARY = new Color(160, 174, 192);
    public static final Color TEXT_MUTED     = new Color(100, 116, 139);
    public static final Color TEXT_INVERSE   = new Color(15,  18,  30);

    // ── Priority colours ─────────────────────────────────────────────────────
    public static final Color PRIORITY_CRITICAL = new Color(252, 129,  74);
    public static final Color PRIORITY_HIGH     = new Color(246, 173,  85);
    public static final Color PRIORITY_MEDIUM   = new Color(99,  179, 237);
    public static final Color PRIORITY_LOW      = new Color(72,  199, 142);

    // ── Status colours ───────────────────────────────────────────────────────
    public static final Color STATUS_TODO        = new Color(100, 116, 139);
    public static final Color STATUS_IN_PROGRESS = new Color(99,  179, 237);
    public static final Color STATUS_REVIEW      = new Color(159, 122, 234);
    public static final Color STATUS_DONE        = new Color(72,  199, 142);

    // ── Borders ──────────────────────────────────────────────────────────────
    public static final Color BORDER         = new Color(38,  46,  72);
    public static final Color BORDER_FOCUS   = new Color(99,  179, 237);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font  FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font  FONT_HEADING   = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font  FONT_SUBHEAD   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font  FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font  FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font  FONT_MONO      = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font  FONT_LABEL     = new Font("Segoe UI", Font.BOLD,  11);

    // ── Helpers ──────────────────────────────────────────────────────────────
    public static Color priorityColor(taskmanager.model.Task.Priority p) {
        return switch (p) {
            case CRITICAL -> PRIORITY_CRITICAL;
            case HIGH     -> PRIORITY_HIGH;
            case MEDIUM   -> PRIORITY_MEDIUM;
            case LOW      -> PRIORITY_LOW;
        };
    }

    public static Color statusColor(taskmanager.model.Task.Status s) {
        return switch (s) {
            case TODO        -> STATUS_TODO;
            case IN_PROGRESS -> STATUS_IN_PROGRESS;
            case REVIEW      -> STATUS_REVIEW;
            case DONE        -> STATUS_DONE;
        };
    }

    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
