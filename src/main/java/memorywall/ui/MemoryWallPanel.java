package memorywall.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

import memorywall.game.GameBoard;
import memorywall.game.PatternLibrary;

/**
 * Swing glue: owns the {@link GameBoard}, drives it with a ~60fps timer, and
 * paints it with a {@link BoardRenderer}. Contains no game rules itself.
 */
public final class MemoryWallPanel extends JPanel {

    private static final int FRAME_INTERVAL_MS = 16;

    private final GameBoard board = new GameBoard(new PatternLibrary(), new Random());
    private final BoardRenderer renderer = new BoardRenderer();
    private final long startTime = System.currentTimeMillis();

    public MemoryWallPanel() {
        setPreferredSize(BoardLayout.panelSize());
        setBackground(LedPalette.PANEL_BACKGROUND);

        InputHandler inputHandler = new InputHandler(board);
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);

        Timer timer = new Timer(FRAME_INTERVAL_MS, e -> {
            board.tick(System.currentTimeMillis());
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        long elapsed = System.currentTimeMillis() - startTime;
        Dimension size = getSize();
        renderer.draw(g2, board, size.width, size.height, elapsed);
    }
}
