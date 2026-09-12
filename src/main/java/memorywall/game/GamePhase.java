package memorywall.game;

/**
 * The phases a single round moves through, in order:
 * IDLE -> COUNTDOWN -> SHOW -> HIDE -> DRAW -> RESULT -> (IDLE again via a new round).
 */
public enum GamePhase {
    IDLE,
    COUNTDOWN,
    SHOW,
    HIDE,
    DRAW,
    RESULT
}
