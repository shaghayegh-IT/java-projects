package memorywall.game;

import java.util.Arrays;
import java.util.Random;

import static memorywall.game.GameConfig.COLS;
import static memorywall.game.GameConfig.ROWS;

/**
 * Holds all game state and rules: the current phase, the target and player
 * grids, score/level, and the round timing. Has no Swing/AWT dependency, so
 * it can be driven and tested without opening a window.
 */
public final class GameBoard {

    private final PatternLibrary patternLibrary;
    private final Random random;

    private GamePhase phase = GamePhase.IDLE;

    private final boolean[][] target = new boolean[ROWS][COLS];
    private final boolean[][] player = new boolean[ROWS][COLS];

    private int score = 0;
    private int level = 1;
    private int countdown = GameConfig.INITIAL_COUNTDOWN;
    private int showSeconds = GameConfig.BASE_SHOW_SECONDS;
    private int showTick = 0;
    private long lastTick = 0;

    private int correctCells = 0;
    private int wrongCells = 0;
    private int missedCells = 0;
    private double accuracy = 0;

    private String statusTitle = "Bereit? Drücke START!";
    private String statusSubtitle = "Präge dir das Muster ein und zeichne es nach!";
    private String statusDetail = "";

    public GameBoard(PatternLibrary patternLibrary, Random random) {
        this.patternLibrary = patternLibrary;
        this.random = random;
    }

    // ────────────────────────────────────────────────────────────
    //  Round flow
    // ────────────────────────────────────────────────────────────

    public void startRound() {
        for (boolean[] row : target) Arrays.fill(row, false);
        for (boolean[] row : player) Arrays.fill(row, false);

        boolean[][] chosen = patternLibrary.choosePattern(random);
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(chosen[r], 0, target[r], 0, COLS);
        }

        showSeconds = Math.max(GameConfig.MIN_SHOW_SECONDS, GameConfig.BASE_SHOW_SECONDS - level / 2);
        countdown = GameConfig.INITIAL_COUNTDOWN;
        lastTick = System.currentTimeMillis();
        phase = GamePhase.COUNTDOWN;
        statusTitle = "Augen auf!";
        statusSubtitle = "Das Muster kommt gleich...";
        statusDetail = "";
    }

    private void startShow() {
        showTick = showSeconds;
        lastTick = System.currentTimeMillis();
        phase = GamePhase.SHOW;
        statusTitle = "👁️  Muster einprägen!";
        statusSubtitle = showTick + " Sekunden — schau genau hin!";
        statusDetail = "";
    }

    private void enterHidePhase(long now) {
        phase = GamePhase.HIDE;
        lastTick = now;
        statusTitle = "💡 Jetzt bist du dran!";
        statusSubtitle = "Zeichne das Muster aus dem Gedächtnis!";
        statusDetail = "Klicken oder ziehen";
    }

    /** Advances timed phases (COUNTDOWN, SHOW, HIDE). Call this once per frame. */
    public void tick(long now) {
        switch (phase) {
            case COUNTDOWN -> tickCountdown(now);
            case SHOW -> tickShow(now);
            case HIDE -> tickHide(now);
            default -> {
                // IDLE, DRAW and RESULT wait for player input, not for time.
            }
        }
    }

    private void tickCountdown(long now) {
        if (now - lastTick < GameConfig.COUNTDOWN_TICK_MS) return;
        countdown--;
        lastTick = now;
        if (countdown <= 0) {
            startShow();
        } else {
            statusTitle = countdown == 2 ? "Fertig machen..." : "Gleich geht's los!";
        }
    }

    private void tickShow(long now) {
        if (now - lastTick < GameConfig.SHOW_TICK_MS) return;
        showTick--;
        lastTick = now;
        statusSubtitle = showTick > 0 ? "Noch " + showTick + " Sekunden — schau genau hin!" : "Zeit!";
        if (showTick <= 0) {
            enterHidePhase(now);
        }
    }

    private void tickHide(long now) {
        if (now - lastTick < GameConfig.HIDE_DURATION_MS) return;
        phase = GamePhase.DRAW;
    }

    /** Called when the action button is pressed; its effect depends on the current phase. */
    public void handleActionButtonPressed() {
        if (phase == GamePhase.IDLE || phase == GamePhase.RESULT) {
            startRound();
        } else if (phase == GamePhase.DRAW) {
            evaluate();
        }
    }

    /** Sets or toggles a cell the player draws on. Only has an effect during the DRAW phase. */
    public void touchCell(int row, int col, boolean toggle) {
        if (phase != GamePhase.DRAW) return;
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;
        player[row][col] = toggle ? !player[row][col] : true;
    }

    private void evaluate() {
        correctCells = 0;
        wrongCells = 0;
        missedCells = 0;
        int total = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (target[r][c]) total++;
                if (target[r][c] && player[r][c]) correctCells++;
                if (!target[r][c] && player[r][c]) wrongCells++;
                if (target[r][c] && !player[r][c]) missedCells++;
            }
        }
        accuracy = total == 0 ? 0 : Math.min(100, correctCells * 100.0 / (total + wrongCells));
        int points = (int) (accuracy * level * GameConfig.SCORE_MULTIPLIER);
        score += points;
        if (accuracy >= GameConfig.LEVEL_UP_ACCURACY_THRESHOLD) level++;

        phase = GamePhase.RESULT;
        if (accuracy == GameConfig.PERFECT_ACCURACY) {
            statusTitle = "🌟 PERFEKT! Du bist ein Gedächtnis-Champion!";
        } else if (accuracy >= GameConfig.GREAT_ACCURACY) {
            statusTitle = "🎉 Super gemacht! Weiter so!";
        } else if (accuracy >= GameConfig.GOOD_ACCURACY) {
            statusTitle = "👍 Gut! Noch ein bisschen üben!";
        } else {
            statusTitle = "💪 Kein Problem! Beim nächsten Mal klappt's!";
        }
        statusSubtitle = String.format("Genauigkeit: %.0f%%   +%d Punkte", accuracy, points);
        statusDetail = String.format("✅ %d richtig   ❌ %d falsch   ⬜ %d vergessen", correctCells, wrongCells, missedCells);
    }

    // ────────────────────────────────────────────────────────────
    //  Read-only access for the UI layer
    // ────────────────────────────────────────────────────────────

    public GamePhase phase() {
        return phase;
    }

    public boolean isTargetOn(int row, int col) {
        return target[row][col];
    }

    public boolean isPlayerOn(int row, int col) {
        return player[row][col];
    }

    public int score() {
        return score;
    }

    public int level() {
        return level;
    }

    public int countdown() {
        return countdown;
    }

    public int showTick() {
        return showTick;
    }

    public double accuracy() {
        return accuracy;
    }

    public int correctCells() {
        return correctCells;
    }

    public int wrongCells() {
        return wrongCells;
    }

    public int missedCells() {
        return missedCells;
    }

    public String statusTitle() {
        return statusTitle;
    }

    public String statusSubtitle() {
        return statusSubtitle;
    }

    public String statusDetail() {
        return statusDetail;
    }
}
