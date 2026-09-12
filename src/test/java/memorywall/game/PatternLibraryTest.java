package memorywall.game;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternLibraryTest {

    private final PatternLibrary library = new PatternLibrary();

    @Test
    void fixedPatternsHaveTheConfiguredGridSize() {
        for (boolean[][] pattern : library.fixedPatterns()) {
            assertEquals(GameConfig.ROWS, pattern.length);
            for (boolean[] row : pattern) {
                assertEquals(GameConfig.COLS, row.length);
            }
        }
    }

    @Test
    void fixedPatternsAreNotEmpty() {
        for (boolean[][] pattern : library.fixedPatterns()) {
            assertTrue(containsLitCell(pattern), "Expected at least one lit cell in every fixed pattern");
        }
    }

    @Test
    void thereAreSixteenFixedPatterns() {
        List<boolean[][]> patterns = library.fixedPatterns();
        assertEquals(16, patterns.size());
    }

    @Test
    void heartPatternIsSymmetricAroundTheVerticalCenter() {
        // The pattern methods build shapes around cx = COLS / 2 (an on-grid
        // column, not the true midpoint between columns 7 and 8), so column c
        // mirrors column (2*cx - c), not the usual (COLS - 1 - c).
        boolean[][] heart = library.fixedPatterns().get(0);
        int cx = GameConfig.COLS / 2;

        for (int r = 0; r < GameConfig.ROWS; r++) {
            for (int c = 0; c < GameConfig.COLS; c++) {
                int mirroredCol = 2 * cx - c;
                if (mirroredCol >= 0 && mirroredCol < GameConfig.COLS) {
                    assertEquals(heart[r][c], heart[r][mirroredCol],
                            "Heart should be mirrored left/right at row " + r);
                }
            }
        }
    }

    @Test
    void diamondPatternIsSymmetricAroundBothAxes() {
        // Diamond is the 5th fixed pattern added in buildFixedPatterns().
        boolean[][] diamond = library.fixedPatterns().get(4);
        int cx = GameConfig.COLS / 2;
        int cy = GameConfig.ROWS / 2;

        for (int r = 0; r < GameConfig.ROWS; r++) {
            for (int c = 0; c < GameConfig.COLS; c++) {
                int mirroredRow = 2 * cy - r;
                int mirroredCol = 2 * cx - c;
                if (mirroredRow >= 0 && mirroredRow < GameConfig.ROWS && mirroredCol >= 0 && mirroredCol < GameConfig.COLS) {
                    assertEquals(diamond[r][c], diamond[mirroredRow][mirroredCol],
                            "Diamond should be symmetric around its center");
                }
            }
        }
    }

    @Test
    void randomProceduralPatternAlwaysHasTheConfiguredGridSize() {
        Random rng = new Random(42);
        for (int i = 0; i < 50; i++) {
            boolean[][] pattern = library.randomProceduralPattern(rng);
            assertEquals(GameConfig.ROWS, pattern.length);
            assertEquals(GameConfig.COLS, pattern[0].length);
        }
    }

    @Test
    void choosePatternIsDeterministicForTheSameSeed() {
        boolean[][] first = library.choosePattern(new Random(7));
        boolean[][] second = library.choosePattern(new Random(7));
        assertGridsEqual(first, second);
    }

    private static boolean containsLitCell(boolean[][] pattern) {
        for (boolean[] row : pattern) {
            for (boolean cell : row) {
                if (cell) return true;
            }
        }
        return false;
    }

    private static void assertGridsEqual(boolean[][] a, boolean[][] b) {
        assertEquals(a.length, b.length);
        for (int r = 0; r < a.length; r++) {
            assertTrue(java.util.Arrays.equals(a[r], b[r]), "Row " + r + " differs");
        }
    }
}
