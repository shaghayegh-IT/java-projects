package memorywall.ui;

import java.awt.Color;

/** All colors used to render the LED wall. */
public final class LedPalette {

    private LedPalette() {
    }

    public static final Color BACKGROUND_TOP = new Color(15, 5, 40);
    public static final Color BACKGROUND_BOTTOM = new Color(5, 0, 15);
    public static final Color PANEL_BACKGROUND = new Color(10, 5, 25);

    public static final Color TARGET_ON = new Color(100, 80, 255);
    public static final Color PLAYER_ON = new Color(255, 60, 160);
    public static final Color CORRECT = new Color(50, 230, 100);
    public static final Color WRONG = new Color(255, 50, 50);
    public static final Color MISSED = new Color(255, 165, 0);
    public static final Color OFF = new Color(15, 10, 35);
    public static final Color RING_BODY = new Color(30, 20, 60);
    public static final Color RING_OUTLINE = new Color(45, 30, 75);

    public static final Color COUNTDOWN_GLOW = new Color(80, 60, 200);
    public static final Color INFO_BAR_TOP = new Color(25, 15, 60);
    public static final Color INFO_BAR_BOTTOM = new Color(15, 8, 40);
    public static final Color INFO_BAR_DEFAULT_BORDER = new Color(100, 80, 200);
    public static final Color SCORE_TEXT = new Color(220, 200, 255);
    public static final Color SUBTITLE_TEXT = new Color(200, 185, 240);
    public static final Color DETAIL_TEXT = new Color(160, 150, 200);

    public static final Color ACTION_BUTTON_IDLE = new Color(110, 55, 210);
    public static final Color ACTION_BUTTON_INACTIVE = new Color(45, 35, 75);
    public static final Color ACTION_BUTTON_FINISH = new Color(40, 170, 70);

    /** Colors cycled through for the idle-screen rainbow wave animation. */
    public static final Color[] RAINBOW = {
            new Color(255, 50, 50),
            new Color(255, 150, 0),
            new Color(255, 230, 0),
            new Color(50, 220, 80),
            new Color(0, 180, 255),
            new Color(100, 80, 255),
            new Color(200, 50, 255),
            new Color(255, 60, 160),
    };
}
