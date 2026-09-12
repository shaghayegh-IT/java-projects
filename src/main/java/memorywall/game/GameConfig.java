package memorywall.game;

/**
 * Central place for all non-visual game constants (grid size, timing, scoring).
 * Pixel/color constants live in the ui package instead, since they are
 * presentation details, not game rules.
 */
public final class GameConfig {

    private GameConfig() {
    }

    public static final int ROWS = 16;
    public static final int COLS = 16;

    public static final int INITIAL_COUNTDOWN = 3;
    public static final long COUNTDOWN_TICK_MS = 900;
    public static final long SHOW_TICK_MS = 1000;
    public static final long HIDE_DURATION_MS = 800;

    public static final int MIN_SHOW_SECONDS = 2;
    public static final int BASE_SHOW_SECONDS = 5;

    public static final int SCORE_MULTIPLIER = 8;
    public static final double LEVEL_UP_ACCURACY_THRESHOLD = 75.0;

    public static final double PERFECT_ACCURACY = 100.0;
    public static final double GREAT_ACCURACY = 80.0;
    public static final double GOOD_ACCURACY = 60.0;

    /** Probability that a round uses one of the hand-drawn patterns instead of a generated one. */
    public static final double FIXED_PATTERN_PROBABILITY = 0.7;

    /** Probability that a single cell is lit in the symmetric-dots generator, before thickening. */
    public static final double RANDOM_DOT_PROBABILITY = 0.10;
}
