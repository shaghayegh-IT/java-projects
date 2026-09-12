package memorywall.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import memorywall.game.GameBoard;
import memorywall.game.GameConfig;

/**
 * Translates raw mouse events into semantic {@link GameBoard} calls
 * (button press, cell touch). Contains only pixel math and no game rules.
 */
public final class InputHandler extends MouseAdapter {

    private final GameBoard board;

    public InputHandler(GameBoard board) {
        this.board = board;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (BoardLayout.actionButtonBounds().contains(e.getX(), e.getY())) {
            board.handleActionButtonPressed();
        }
        touchCellAt(e.getX(), e.getY(), true);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        touchCellAt(e.getX(), e.getY(), false);
    }

    private void touchCellAt(int x, int y, boolean toggle) {
        int col = BoardLayout.columnAt(x);
        int row = BoardLayout.rowAt(y);
        if (row >= 0 && row < GameConfig.ROWS && col >= 0 && col < GameConfig.COLS) {
            board.touchCell(row, col, toggle);
        }
    }
}
