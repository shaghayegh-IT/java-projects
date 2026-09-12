package memorywall.game;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameBoardTest {

    private GameBoard board;

    @BeforeEach
    void setUp() {
        // Seeded RNG makes pattern selection reproducible for these tests.
        board = new GameBoard(new PatternLibrary(), new Random(1));
    }

    @Test
    void newBoardStartsInIdlePhase() {
        assertEquals(GamePhase.IDLE, board.phase());
    }

    @Test
    void startRoundEntersCountdownWithAClearPlayerGrid() {
        board.startRound();

        assertEquals(GamePhase.COUNTDOWN, board.phase());
        for (int r = 0; r < GameConfig.ROWS; r++) {
            for (int c = 0; c < GameConfig.COLS; c++) {
                assertFalse(board.isPlayerOn(r, c));
            }
        }
    }

    @Test
    void countdownTicksDownToZeroThenEntersShowPhase() {
        board.startRound();
        long now = System.currentTimeMillis();

        // 3 -> 2 -> 1 -> 0, one COUNTDOWN_TICK_MS apart, matching GameBoard's own pacing.
        for (int i = 0; i < GameConfig.INITIAL_COUNTDOWN; i++) {
            now += GameConfig.COUNTDOWN_TICK_MS;
            board.tick(now);
        }

        assertEquals(GamePhase.SHOW, board.phase());
    }

    @Test
    void showPhaseEventuallyLeadsToDrawPhaseAfterHideDelay()  {
        board.startRound();
        long now = System.currentTimeMillis();

        for (int i = 0; i < GameConfig.INITIAL_COUNTDOWN; i++) {
            now += GameConfig.COUNTDOWN_TICK_MS;
            board.tick(now);
        }
        assertEquals(GamePhase.SHOW, board.phase());

        int showSeconds = board.showTick();
        for (int i = 0; i < showSeconds; i++) {
            now += GameConfig.SHOW_TICK_MS;
            board.tick(now);
        }
        assertEquals(GamePhase.HIDE, board.phase());

        now += GameConfig.HIDE_DURATION_MS;
        board.tick(now);
        assertEquals(GamePhase.DRAW, board.phase());
    }

    @Test
    void touchCellIsIgnoredOutsideOfDrawPhase() {
        assertEquals(GamePhase.IDLE, board.phase());
        board.touchCell(0, 0, true);
        assertFalse(board.isPlayerOn(0, 0));
    }

    @Test
    void touchCellTogglesDuringDrawPhase() {
        forcePhaseToDraw();

        board.touchCell(2, 3, true);
        assertTrue(board.isPlayerOn(2, 3));

        board.touchCell(2, 3, true);
        assertFalse(board.isPlayerOn(2, 3));
    }

    @Test
    void touchCellWithoutToggleAlwaysSetsTheCell() {
        forcePhaseToDraw();

        board.touchCell(1, 1, false);
        board.touchCell(1, 1, false);

        assertTrue(board.isPlayerOn(1, 1));
    }

    @Test
    void evaluateComputesAccuracyAndAwardsScore() {
        forcePhaseToDraw();

        // Mark every target cell as drawn correctly, and nothing else -> 100% accuracy.
        for (int r = 0; r < GameConfig.ROWS; r++) {
            for (int c = 0; c < GameConfig.COLS; c++) {
                if (board.isTargetOn(r, c)) {
                    board.touchCell(r, c, false);
                }
            }
        }

        board.handleActionButtonPressed(); // DRAW phase -> evaluate()

        assertEquals(GamePhase.RESULT, board.phase());
        assertEquals(100.0, board.accuracy());
        assertEquals(0, board.wrongCells());
        assertEquals(0, board.missedCells());
        assertTrue(board.score() > 0);
    }

    @Test
    void evaluateLevelsUpOnlyWhenAccuracyMeetsThreshold() {
        forcePhaseToDraw();
        // Player draws nothing at all -> 0% accuracy (unless the target pattern is empty, which never happens).
        board.handleActionButtonPressed();

        assertEquals(1, board.level(), "Level should not increase below the level-up threshold");
    }

    @Test
    void actionButtonStartsARoundFromIdleAndResult() {
        board.handleActionButtonPressed();
        assertEquals(GamePhase.COUNTDOWN, board.phase());
    }

    private void forcePhaseToDraw() {
        board.startRound();
        long now = System.currentTimeMillis();
        for (int i = 0; i < GameConfig.INITIAL_COUNTDOWN; i++) {
            now += GameConfig.COUNTDOWN_TICK_MS;
            board.tick(now);
        }
        int showSeconds = board.showTick();
        for (int i = 0; i < showSeconds; i++) {
            now += GameConfig.SHOW_TICK_MS;
            board.tick(now);
        }
        now += GameConfig.HIDE_DURATION_MS;
        board.tick(now);
    }
}
