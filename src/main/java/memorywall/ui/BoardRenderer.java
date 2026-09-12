package memorywall.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import memorywall.game.GameBoard;
import memorywall.game.GameConfig;
import memorywall.game.GamePhase;

/**
 * Draws the LED grid and the info bar for a given {@link GameBoard} state.
 * Holds no game state itself; everything it needs is read from the board.
 */
public final class BoardRenderer {

    public void draw(Graphics2D g2, GameBoard board, int panelWidth, int panelHeight, long elapsedMillis) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2, panelWidth, panelHeight);

        for (int r = 0; r < GameConfig.ROWS; r++) {
            for (int c = 0; c < GameConfig.COLS; c++) {
                drawLed(g2, board, r, c, elapsedMillis);
            }
        }

        drawInfoBar(g2, board, elapsedMillis);
    }

    private void drawBackground(Graphics2D g2, int width, int height) {
        g2.setPaint(new GradientPaint(0, 0, LedPalette.BACKGROUND_TOP, 0, height, LedPalette.BACKGROUND_BOTTOM));
        g2.fillRect(0, 0, width, height);
    }

    private void drawLed(Graphics2D g2, GameBoard board, int row, int col, long t) {
        int x = BoardLayout.cellLeft(col);
        int y = BoardLayout.cellTop(row);
        int size = BoardLayout.CELL_SIZE - 4;

        boolean isTarget = board.isTargetOn(row, col);
        boolean isPlayer = board.isPlayerOn(row, col);
        Color color = LedPalette.OFF;
        float brightness = 0f;

        switch (board.phase()) {
            case IDLE -> {
                double wave = Math.sin((row + col) * 0.5 - t * 0.004);
                if (wave > 0.3) {
                    color = LedPalette.RAINBOW[(int) Math.abs((row + col + t / 120)) % LedPalette.RAINBOW.length];
                    brightness = (float) ((wave - 0.3) / 0.7) * 0.65f;
                }
            }
            case COUNTDOWN -> {
                color = LedPalette.COUNTDOWN_GLOW;
                brightness = (float) (Math.sin(t * 0.006) * 0.5 + 0.5) * 0.45f;
            }
            case SHOW -> {
                if (isTarget) {
                    color = LedPalette.TARGET_ON;
                    brightness = 1f;
                }
            }
            case HIDE -> {
                // Pattern is hidden; nothing lit.
            }
            case DRAW -> {
                if (isPlayer) {
                    color = LedPalette.PLAYER_ON;
                    brightness = 0.95f;
                }
            }
            case RESULT -> {
                if (isTarget && isPlayer) {
                    color = LedPalette.CORRECT;
                    brightness = 1f;
                } else if (!isTarget && isPlayer) {
                    color = LedPalette.WRONG;
                    brightness = (t / 300) % 2 == 0 ? 0.9f : 0.2f; // blink
                } else if (isTarget) {
                    color = LedPalette.MISSED;
                    brightness = 0.7f;
                }
            }
        }

        g2.setColor(LedPalette.RING_BODY);
        g2.fillOval(x + 1, y + 1, size, size);

        if (brightness > 0) {
            for (int glow = 5; glow >= 1; glow--) {
                float alpha = brightness * (0.22f - glow * 0.035f);
                if (alpha <= 0) continue;
                g2.setColor(new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, Math.min(1f, alpha)));
                g2.fillOval(x - glow, y - glow, size + glow * 2, size + glow * 2);
            }
            g2.setColor(color);
            g2.fillOval(x + 4, y + 4, size - 8, size - 8);

            g2.setColor(new Color(255, 255, 255, (int) (brightness * 190)));
            g2.fillOval(x + size / 2 - 3, y + size / 2 - 3, 6, 6);
            g2.setColor(new Color(255, 255, 255, (int) (brightness * 80)));
            g2.fillOval(x + size / 2 - 7, y + size / 2 - 7, 14, 14);
        }

        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(brightness > 0 ? color.darker() : LedPalette.RING_OUTLINE);
        g2.drawOval(x + 1, y + 1, size, size);
    }

    private void drawInfoBar(Graphics2D g2, GameBoard board, long t) {
        int barY = BoardLayout.infoBarTop();
        int barW = BoardLayout.infoBarWidth();
        int barH = BoardLayout.infoBarHeight();

        g2.setPaint(new GradientPaint(BoardLayout.MARGIN, barY, LedPalette.INFO_BAR_TOP, BoardLayout.MARGIN, barY + barH, LedPalette.INFO_BAR_BOTTOM));
        g2.fillRoundRect(BoardLayout.MARGIN, barY, barW, barH, 15, 15);

        GamePhase phase = board.phase();
        Color border = switch (phase) {
            case SHOW -> LedPalette.TARGET_ON;
            case DRAW -> LedPalette.PLAYER_ON;
            case RESULT -> board.accuracy() >= GameConfig.LEVEL_UP_ACCURACY_THRESHOLD ? LedPalette.CORRECT : LedPalette.WRONG;
            default -> LedPalette.INFO_BAR_DEFAULT_BORDER;
        };
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(BoardLayout.MARGIN, barY, barW, barH, 15, 15);

        String phaseText = switch (phase) {
            case IDLE -> "✨ BEREIT";
            case COUNTDOWN -> "🔢  " + board.countdown() + "...";
            case SHOW -> "👁️  EINPRÄGEN — " + board.showTick() + "s";
            case HIDE -> "🙈  VERSTECKT";
            case DRAW -> "✏️  ZEICHNEN";
            case RESULT -> board.accuracy() >= GameConfig.LEVEL_UP_ACCURACY_THRESHOLD ? "🏆  SUPER!" : "📊  ERGEBNIS";
        };
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(border);
        g2.drawString(phaseText, BoardLayout.MARGIN + 14, barY + 24);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(LedPalette.SCORE_TEXT);
        String scoreText = "⭐ " + board.score() + "   🎯 Level " + board.level();
        g2.drawString(scoreText, BoardLayout.MARGIN + barW - g2.getFontMetrics().stringWidth(scoreText) - 14, barY + 24);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        g2.drawString(board.statusTitle(), BoardLayout.MARGIN + 14, barY + 48);

        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(LedPalette.SUBTITLE_TEXT);
        g2.drawString(board.statusSubtitle(), BoardLayout.MARGIN + 14, barY + 67);

        if (!board.statusDetail().isEmpty()) {
            g2.setColor(LedPalette.DETAIL_TEXT);
            g2.drawString(board.statusDetail(), BoardLayout.MARGIN + 14, barY + 85);
        }

        drawActionButton(g2, phase, t);
    }

    private void drawActionButton(Graphics2D g2, GamePhase phase, long t) {
        boolean active = phase == GamePhase.IDLE || phase == GamePhase.RESULT || phase == GamePhase.DRAW;
        String label = phase == GamePhase.DRAW ? "✅  FERTIG!" : "▶  START";
        Color color = phase == GamePhase.DRAW
                ? LedPalette.ACTION_BUTTON_FINISH
                : active ? LedPalette.ACTION_BUTTON_IDLE : LedPalette.ACTION_BUTTON_INACTIVE;

        Rectangle bounds = BoardLayout.actionButtonBounds();

        if (active) {
            float pulse = (float) (Math.sin(t * 0.005) * 0.5 + 0.5);
            g2.setColor(new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.25f + pulse * 0.25f));
            g2.fillRoundRect(bounds.x - 5, bounds.y - 5, bounds.width + 10, bounds.height + 10, 14, 14);
        }
        g2.setColor(color);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        g2.setColor(color.brighter());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, bounds.x + (bounds.width - fm.stringWidth(label)) / 2, bounds.y + bounds.height / 2 + 5);
    }
}
