package memorywall.ui;

import java.awt.Dimension;
import java.awt.Rectangle;

import memorywall.game.GameConfig;

/**
 * Converts between pixel coordinates and grid cells, and defines where the
 * action button sits. Used by both {@link BoardRenderer} (drawing) and
 * {@link InputHandler} (hit-testing), so the two never disagree on where
 * things are on screen.
 */
public final class BoardLayout {

    private BoardLayout() {
    }

    public static final int CELL_SIZE = 42;
    public static final int MARGIN = 20;
    public static final int INFO_BAR_HEIGHT = 110;

    private static final int ACTION_BUTTON_WIDTH = 115;
    private static final int ACTION_BUTTON_HEIGHT = 32;
    private static final int ACTION_BUTTON_MARGIN_RIGHT = 12;
    private static final int ACTION_BUTTON_MARGIN_BOTTOM = 8;

    public static Dimension panelSize() {
        int width = GameConfig.COLS * CELL_SIZE + 2 * MARGIN;
        int height = GameConfig.ROWS * CELL_SIZE + 2 * MARGIN + INFO_BAR_HEIGHT;
        return new Dimension(width, height);
    }

    public static int cellLeft(int col) {
        return MARGIN + col * CELL_SIZE;
    }

    public static int cellTop(int row) {
        return MARGIN + row * CELL_SIZE;
    }

    public static int columnAt(int x) {
        return (x - MARGIN) / CELL_SIZE;
    }

    public static int rowAt(int y) {
        return (y - MARGIN) / CELL_SIZE;
    }

    public static int infoBarTop() {
        return MARGIN + GameConfig.ROWS * CELL_SIZE + 6;
    }

    public static int infoBarWidth() {
        return GameConfig.COLS * CELL_SIZE;
    }

    public static int infoBarHeight() {
        return INFO_BAR_HEIGHT - 8;
    }

    public static Rectangle actionButtonBounds() {
        int barY = infoBarTop();
        int barW = infoBarWidth();
        int bx = MARGIN + barW - ACTION_BUTTON_WIDTH - ACTION_BUTTON_MARGIN_RIGHT;
        int by = barY + infoBarHeight() - ACTION_BUTTON_HEIGHT - ACTION_BUTTON_MARGIN_BOTTOM;
        return new Rectangle(bx, by, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);
    }
}
