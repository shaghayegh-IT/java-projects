package memorywall.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static memorywall.game.GameConfig.COLS;
import static memorywall.game.GameConfig.ROWS;

/**
 * Builds the patterns a round can show: a fixed set of hand-designed shapes
 * (heart, star, smiley, ...) plus procedurally generated ones. All methods
 * work on plain {@code boolean[ROWS][COLS]} grids and have no dependency on
 * Swing/AWT, so the geometry can be unit-tested on its own.
 */
public final class PatternLibrary {

    private final List<boolean[][]> fixedPatterns = buildFixedPatterns();

    public List<boolean[][]> fixedPatterns() {
        return fixedPatterns;
    }

    /** Picks a round's pattern: mostly a fixed shape, sometimes a generated one. */
    public boolean[][] choosePattern(Random rng) {
        if (rng.nextDouble() < GameConfig.FIXED_PATTERN_PROBABILITY) {
            return fixedPatterns.get(rng.nextInt(fixedPatterns.size()));
        }
        return randomProceduralPattern(rng);
    }

    public boolean[][] randomProceduralPattern(Random rng) {
        int type = rng.nextInt(6);
        return switch (type) {
            case 0 -> generateSymmetricDots(rng);
            case 1 -> generateRandomCross(rng);
            case 2 -> generateRandomDiamond(rng);
            case 3 -> generateRandomLines(rng);
            case 4 -> generateRandomBlocks(rng);
            default -> generateRandomRing(rng);
        };
    }

    // ────────────────────────────────────────────────────────────
    //  Fixed, hand-designed patterns
    // ────────────────────────────────────────────────────────────

    private static List<boolean[][]> buildFixedPatterns() {
        List<boolean[][]> patterns = new ArrayList<>();
        patterns.add(heartPattern());
        patterns.add(starPattern());
        patterns.add(smileyPattern());
        patterns.add(boltPattern());
        patterns.add(diamondPattern());
        patterns.add(housePattern());
        patterns.add(butterflyPattern());
        patterns.add(rocketPattern());
        patterns.add(plusPattern());
        patterns.add(xPattern());
        patterns.add(framePattern());
        patterns.add(arrowUpPattern());
        patterns.add(stairsPattern());
        patterns.add(wavePattern());
        patterns.add(checkerPattern());
        patterns.add(eyesPattern());
        return patterns;
    }

    /** Heart shape, based on the classic implicit heart-curve formula. */
    private static boolean[][] heartPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double x = (c - cx) / 4.0, y = (r - cy + 2) / 3.5;
                double v = Math.pow(x * x + y * y - 1, 3) - x * x * y * y * y;
                if (v <= 0 && r < cy + 7) p[r][c] = true;
            }
        }
        return p;
    }

    /** Five-pointed star, drawn with a polar-coordinate radius function. */
    private static boolean[][] starPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double dx = c - cx, dy = r - cy;
                double angle = Math.atan2(dy, dx);
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 4.5 + 3 * Math.cos(5 * angle)) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] smileyPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double dx = c - cx, dy = r - cy;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d >= 6 && d <= 7.5) p[r][c] = true;
                double ex = c - (cx - 2.5), ey = r - (cy - 2);
                if (ex * ex + ey * ey <= 1.5) p[r][c] = true;
                double ex2 = c - (cx + 2.5), ey2 = r - (cy - 2);
                if (ex2 * ex2 + ey2 * ey2 <= 1.5) p[r][c] = true;
                if (d >= 3.5 && d <= 5 && r > cy) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] boltPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2;
        for (int r = 1; r < 9; r++) {
            int c = cx + (8 - r);
            if (c >= 0 && c < COLS) p[r][c] = true;
            if (c - 1 >= 0) p[r][c - 1] = true;
        }
        for (int c = cx - 4; c <= cx + 4; c++) {
            if (c >= 0 && c < COLS) p[8][c] = true;
        }
        for (int r = 8; r < 15; r++) {
            int c = cx - (r - 8);
            if (c >= 0 && c < COLS) p[r][c] = true;
            if (c + 1 < COLS) p[r][c + 1] = true;
        }
        return p;
    }

    private static boolean[][] diamondPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (Math.abs(r - cy) + Math.abs(c - cx) == 6) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] housePattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2;
        for (int i = 0; i <= 5; i++) {
            if (cx - i >= 0 && cx + i < COLS) {
                p[3 + i][cx - i] = true;
                p[3 + i][cx + i] = true;
            }
        }
        for (int r = 8; r < 14; r++) {
            p[r][cx - 5] = true;
            p[r][cx + 5] = true;
        }
        for (int c = cx - 5; c <= cx + 5; c++) {
            p[13][c] = true;
        }
        for (int r = 10; r < 14; r++) {
            p[r][cx - 1] = true;
            p[r][cx + 1] = true;
        }
        p[10][cx] = true;
        return p;
    }

    private static boolean[][] butterflyPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double lx = c - (cx - 4), ly = r - cy;
                double rx = c - (cx + 4), ry = r - cy;
                double bx = c - cx, by = r - cy;
                if (lx * lx / 16.0 + ly * ly / 9.0 <= 1) p[r][c] = true;
                if (rx * rx / 16.0 + ry * ry / 9.0 <= 1) p[r][c] = true;
                if (bx * bx + by * by / 4.0 <= 1) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] rocketPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2;
        for (int r = 2; r < 12; r++) {
            int w = (int) (3 * (1 - (r - 2) / 10.0));
            for (int c = cx - w; c <= cx + w; c++) {
                if (c >= 0 && c < COLS) p[r][c] = true;
            }
        }
        for (int r = 12; r < 15; r++) {
            p[r][cx - 3] = true;
            p[r][cx + 3] = true;
        }
        p[2][cx] = true;
        p[3][cx - 1] = true;
        p[3][cx + 1] = true;
        return p;
    }

    private static boolean[][] plusPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int r = cy - 5; r <= cy + 5; r++) p[r][cx] = true;
        for (int c = cx - 5; c <= cx + 5; c++) p[cy][c] = true;
        return p;
    }

    private static boolean[][] xPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        for (int i = 2; i < ROWS - 2; i++) {
            if (i < COLS) p[i][i] = true;
            int j = COLS - 1 - i;
            if (j >= 0 && j < COLS) p[i][j] = true;
        }
        return p;
    }

    private static boolean[][] framePattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int top = 3, left = 3, bottom = ROWS - 4, right = COLS - 4;
        for (int c = left; c <= right; c++) {
            p[top][c] = true;
            p[bottom][c] = true;
        }
        for (int r = top; r <= bottom; r++) {
            p[r][left] = true;
            p[r][right] = true;
        }
        return p;
    }

    private static boolean[][] arrowUpPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2;
        for (int i = 0; i < 5; i++) {
            p[3 + i][cx - i] = true;
            p[3 + i][cx + i] = true;
        }
        for (int r = 7; r < 13; r++) p[r][cx] = true;
        return p;
    }

    private static boolean[][] stairsPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2, cy = ROWS / 2;
        for (int i = 0; i < 6; i++) {
            for (int c = cx - 5 + i; c <= cx - 1 + i; c++) p[cy + 4 - i][c] = true;
        }
        return p;
    }

    private static boolean[][] wavePattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        int cy = ROWS / 2;
        for (int c = 1; c < COLS - 1; c++) {
            int r = cy + (int) Math.round(3 * Math.sin(c * 0.7));
            if (r >= 0 && r < ROWS) p[r][c] = true;
        }
        return p;
    }

    private static boolean[][] checkerPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        for (int r = 4; r < 12; r++) {
            for (int c = 4; c < 12; c++) {
                if ((r + c) % 2 == 0) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] eyesPattern() {
        boolean[][] p = new boolean[ROWS][COLS];
        for (int r = 4; r <= 7; r++) {
            for (int c = 3; c <= 6; c++) p[r][c] = true;
            for (int c = 9; c <= 12; c++) p[r][c] = true;
        }
        p[5][4] = false;
        p[5][5] = false;
        p[5][10] = false;
        p[5][11] = false;
        return p;
    }

    // ────────────────────────────────────────────────────────────
    //  Procedurally generated patterns
    // ────────────────────────────────────────────────────────────

    private static boolean[][] generateSymmetricDots(Random rng) {
        boolean[][] p = new boolean[ROWS][COLS];
        for (int r = 2; r < ROWS - 2; r++) {
            for (int c = 2; c < COLS / 2; c++) {
                if (rng.nextDouble() < GameConfig.RANDOM_DOT_PROBABILITY) {
                    p[r][c] = true;
                    p[r][COLS - 1 - c] = true; // mirror horizontally
                }
            }
        }
        thicken(p, rng);
        return p;
    }

    private static boolean[][] generateRandomCross(Random rng) {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = 4 + rng.nextInt(COLS - 8);
        int cy = 4 + rng.nextInt(ROWS - 8);
        int len = 3 + rng.nextInt(4);

        for (int r = cy - len; r <= cy + len; r++) {
            if (r >= 0 && r < ROWS) p[r][cx] = true;
        }
        for (int c = cx - len; c <= cx + len; c++) {
            if (c >= 0 && c < COLS) p[cy][c] = true;
        }
        if (rng.nextBoolean()) thicken(p, rng);
        return p;
    }

    private static boolean[][] generateRandomDiamond(Random rng) {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2;
        int cy = ROWS / 2;
        int radius = 3 + rng.nextInt(4);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (Math.abs(r - cy) + Math.abs(c - cx) == radius) {
                    p[r][c] = true;
                }
            }
        }
        return p;
    }

    private static boolean[][] generateRandomLines(Random rng) {
        boolean[][] p = new boolean[ROWS][COLS];
        int lines = 3 + rng.nextInt(3);

        for (int i = 0; i < lines; i++) {
            if (rng.nextBoolean()) {
                int r = 2 + rng.nextInt(ROWS - 4);
                for (int c = 2; c < COLS - 2; c++) p[r][c] = true;
            } else {
                int c = 2 + rng.nextInt(COLS - 4);
                for (int r = 2; r < ROWS - 2; r++) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] generateRandomBlocks(Random rng) {
        boolean[][] p = new boolean[ROWS][COLS];
        int blocks = 3 + rng.nextInt(4);

        for (int i = 0; i < blocks; i++) {
            int h = 2 + rng.nextInt(3);
            int w = 2 + rng.nextInt(3);
            int sr = 1 + rng.nextInt(ROWS - h - 2);
            int sc = 1 + rng.nextInt(COLS - w - 2);

            for (int r = sr; r < sr + h; r++) {
                for (int c = sc; c < sc + w; c++) p[r][c] = true;
            }
        }
        return p;
    }

    private static boolean[][] generateRandomRing(Random rng) {
        boolean[][] p = new boolean[ROWS][COLS];
        int cx = COLS / 2 + rng.nextInt(3) - 1;
        int cy = ROWS / 2 + rng.nextInt(3) - 1;
        double radius = 3.5 + rng.nextInt(3);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double dx = c - cx;
                double dy = r - cy;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d >= radius - 0.7 && d <= radius + 0.7) {
                    p[r][c] = true;
                }
            }
        }
        return p;
    }

    /** Randomly grows each lit cell into its direct neighbours, to make thin shapes bolder. */
    private static void thicken(boolean[][] p, Random rng) {
        boolean[][] copy = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(p[r], 0, copy[r], 0, COLS);
        }

        for (int r = 1; r < ROWS - 1; r++) {
            for (int c = 1; c < COLS - 1; c++) {
                if (copy[r][c]) {
                    if (rng.nextBoolean()) p[r][c - 1] = true;
                    if (rng.nextBoolean()) p[r][c + 1] = true;
                    if (rng.nextBoolean()) p[r - 1][c] = true;
                    if (rng.nextBoolean()) p[r + 1][c] = true;
                }
            }
        }
    }
}
