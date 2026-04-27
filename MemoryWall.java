import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
// ═══════════════════════════════════════════════════════
//  MEMORY WALL — Gedächtnisspiel für WS2812 LED-Wand
//  Zielgruppe: 12–14 Jahre
//  Hardware:   16×16 = 256 WS2812 LED-Ringe (1×1 Meter)
//  Software:   Java (javax.swing) — läuft direkt in IntelliJ
//  Keine externen Bibliotheken nötig!
// ═══════════════════════════════════════════════════════
public class MemoryWall extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    // ── Raster: 16×16 = 256 LED-Ringe ──────────────────
    static final int COLS = 16;
    static final int ROWS = 16;
    static final int CELL = 42;       // Pixelgröße jedes LED-Rings
    static final int MARGIN = 20;
    static final int INFO_H = 110;

    // ── Spielphasen ─────────────────────────────────────
    enum Phase {IDLE, COUNTDOWN, SHOW, HIDE, DRAW, RESULT}

    Phase phase = Phase.IDLE;

    // ── LED-Arrays ──────────────────────────────────────
    boolean[][] target = new boolean[ROWS][COLS];   // Zielmuster
    boolean[][] player = new boolean[ROWS][COLS];   // Spielereingabe

    // ── Farben (bunt & kindgerecht) ──────────────────────
    static final Color TARGET_ON = new Color(100, 80, 255);   // Lila-Blau
    static final Color PLAYER_ON = new Color(255, 60, 160);   // Pink
    static final Color CORRECT = new Color(50, 230, 100);  // Grün
    static final Color WRONG = new Color(255, 50, 50);   // Rot
    static final Color MISSED = new Color(255, 165, 0);    // Orange
    static final Color OFF = new Color(15, 10, 35);   // Dunkel
    static final Color RING_BODY = new Color(30, 20, 60);   // Ring-Gehäuse

    // ── Regenbogen für IDLE-Animation ───────────────────
    static final Color[] RAINBOW = {
            new Color(255, 50, 50),
            new Color(255, 150, 0),
            new Color(255, 230, 0),
            new Color(50, 220, 80),
            new Color(0, 180, 255),
            new Color(100, 80, 255),
            new Color(200, 50, 255),
            new Color(255, 60, 160),
    };

    // ── Spielvariablen ───────────────────────────────────
    int score = 0;
    int level = 1;
    int countdown = 3;
    int showSeconds = 4;
    int showTick = 0;
    long lastTick = 0;

    // ── Auswertung ───────────────────────────────────────
    int correctCells = 0, wrongCells = 0, missedCells = 0;
    double accuracy = 0;

    // ── Nachrichten ──────────────────────────────────────
    String msg1 = "Bereit? Drücke START!";
    String msg2 = "Präge dir das Muster ein und zeichne es nach!";
    String msg3 = "";

    // ── Timer & Zeit ─────────────────────────────────────
    Timer timer;
    long startTime = System.currentTimeMillis();

    // ── Musterliste ──────────────────────────────────────
    java.util.List<boolean[][]> patternList = new ArrayList<>();
    Random rng = new Random();
    // ════════════════════════════════════════════════════
    //  KONSTRUKTOR
    // ════════════════════════════════════════════════════
    public MemoryWall() {
        setPreferredSize(new Dimension(
                COLS * CELL + 2 * MARGIN,
                ROWS * CELL + 2 * MARGIN + INFO_H
        ));
        setBackground(new Color(10, 5, 25));
        addMouseListener(this);
        addMouseMotionListener(this);
        buildPatterns();
        timer = new Timer(16, this);  // ~60 FPS
        timer.start();
    }

    // ════════════════════════════════════════════════════
    //  MUSTERBIBLIOTHEK
    // ════════════════════════════════════════════════════
    void buildPatterns() {
        int cx = COLS / 2, cy = ROWS / 2;

        // Herz ❤
        boolean[][] heart = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                double x = (c - cx) / 4.0, y = (r - cy + 2) / 3.5;
                double v = Math.pow(x * x + y * y - 1, 3) - x * x * y * y * y;
                if (v <= 0 && r < cy + 7) heart[r][c] = true;
            }
        patternList.add(heart);

        // Stern ⭐
        boolean[][] star = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                double dx = c - cx, dy = r - cy;
                double angle = Math.atan2(dy, dx);
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 4.5 + 3 * Math.cos(5 * angle)) star[r][c] = true;
            }
        patternList.add(star);

        // Smiley 😊
        boolean[][] smiley = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                double dx = c - cx, dy = r - cy;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d >= 6 && d <= 7.5) smiley[r][c] = true;
                double ex = c - (cx - 2.5), ey = r - (cy - 2);
                if (ex * ex + ey * ey <= 1.5) smiley[r][c] = true;
                double ex2 = c - (cx + 2.5), ey2 = r - (cy - 2);
                if (ex2 * ex2 + ey2 * ey2 <= 1.5) smiley[r][c] = true;
                if (d >= 3.5 && d <= 5 && r > cy) smiley[r][c] = true;
            }
        patternList.add(smiley);

        // Blitz ⚡
        boolean[][] blitz = new boolean[ROWS][COLS];
        for (int r = 1; r < 9; r++) {
            int c = cx + (8 - r);
            if (c >= 0 && c < COLS) blitz[r][c] = true;
            if (c - 1 >= 0) blitz[r][c - 1] = true;
        }
        for (int c = cx - 4; c <= cx + 4; c++)
            if (c >= 0 && c < COLS) blitz[8][c] = true;
        for (int r = 8; r < 15; r++) {
            int c = cx - (r - 8);
            if (c >= 0 && c < COLS) blitz[r][c] = true;
            if (c + 1 < COLS) blitz[r][c + 1] = true;
        }
        patternList.add(blitz);

        // Diamant 💎
        boolean[][] diamond = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (Math.abs(r - cy) + Math.abs(c - cx) == 6) diamond[r][c] = true;
        patternList.add(diamond);

        // Haus 🏠
        boolean[][] house = new boolean[ROWS][COLS];
        for (int i = 0; i <= 5; i++) {
            if (cx - i >= 0 && cx + i < COLS) {
                house[3 + i][cx - i] = true;
                house[3 + i][cx + i] = true;
            }
        }
        for (int r = 8; r < 14; r++) {
            house[r][cx - 5] = true;
            house[r][cx + 5] = true;
        }
        for (int c = cx - 5; c <= cx + 5; c++) house[13][c] = true;
        for (int r = 10; r < 14; r++) {
            house[r][cx - 1] = true;
            house[r][cx + 1] = true;
        }
        house[10][cx] = true;
        patternList.add(house);

        // Schmetterling 🦋
        boolean[][] butterfly = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                double lx = c - (cx - 4), ly = r - cy;
                double rx = c - (cx + 4), ry = r - cy;
                double bx = c - cx, by = r - cy;
                if (lx * lx / 16.0 + ly * ly / 9.0 <= 1) butterfly[r][c] = true;
                if (rx * rx / 16.0 + ry * ry / 9.0 <= 1) butterfly[r][c] = true;
                if (bx * bx + by * by / 4.0 <= 1) butterfly[r][c] = true;
            }
        patternList.add(butterfly);

        // Rakete 🚀
        boolean[][] rocket = new boolean[ROWS][COLS];
        for (int r = 2; r < 12; r++) {
            int w = (int) (3 * (1 - (r - 2) / 10.0));
            for (int c = cx - w; c <= cx + w; c++)
                if (c >= 0 && c < COLS) rocket[r][c] = true;
        }
        for (int r = 12; r < 15; r++) {
            rocket[r][cx - 3] = true;
            rocket[r][cx + 3] = true;
        }
        rocket[2][cx] = true;
        rocket[3][cx - 1] = true;
        rocket[3][cx + 1] = true;
        patternList.add(rocket);

        // Kreuz +
        boolean[][] plus = new boolean[ROWS][COLS];
        for (int r = cy - 5; r <= cy + 5; r++) plus[r][cx] = true;
        for (int c = cx - 5; c <= cx + 5; c++) plus[cy][c] = true;
        patternList.add(plus);

        // X
        boolean[][] xShape = new boolean[ROWS][COLS];
        for (int i = 2; i < ROWS - 2; i++) {
            if (i < COLS) xShape[i][i] = true;
            int j = COLS - 1 - i;
            if (j >= 0 && j < COLS) xShape[i][j] = true;
        }
        patternList.add(xShape);

        // Rahmen / square border
        boolean[][] frame = new boolean[ROWS][COLS];
        int top = 3, left = 3, bottom = ROWS - 4, right = COLS - 4;
        for (int c = left; c <= right; c++) {
            frame[top][c] = true;
            frame[bottom][c] = true;
        }
        for (int r = top; r <= bottom; r++) {
            frame[r][left] = true;
            frame[r][right] = true;
        }
        patternList.add(frame);

        // Pfeil nach oben ⬆
        boolean[][] arrowUp = new boolean[ROWS][COLS];
        for (int i = 0; i < 5; i++) {
            arrowUp[3 + i][cx - i] = true;
            arrowUp[3 + i][cx + i] = true;
        }
        for (int r = 7; r < 13; r++) arrowUp[r][cx] = true;
        patternList.add(arrowUp);

        // Treppe
        boolean[][] stairs = new boolean[ROWS][COLS];
        for (int i = 0; i < 6; i++) {
            for (int c = cx - 5 + i; c <= cx - 1 + i; c++) stairs[cy + 4 - i][c] = true;
        }
        patternList.add(stairs);

        // Welle
        boolean[][] wave = new boolean[ROWS][COLS];
        for (int c = 1; c < COLS - 1; c++) {
            int r = cy + (int) Math.round(3 * Math.sin(c * 0.7));
            if (r >= 0 && r < ROWS) wave[r][c] = true;
        }
        patternList.add(wave);
        // Checkerboard klein
        boolean[][] checker = new boolean[ROWS][COLS];
        for (int r = 4; r < 12; r++)
            for (int c = 4; c < 12; c++)
                if ((r + c) % 2 == 0) checker[r][c] = true;
        patternList.add(checker);

        // Augen
        boolean[][] eyes = new boolean[ROWS][COLS];
        for (int r = 4; r <= 7; r++) {
            for (int c = 3; c <= 6; c++) eyes[r][c] = true;
            for (int c = 9; c <= 12; c++) eyes[r][c] = true;
        }
        eyes[5][4] = false;
        eyes[5][5] = false;
        eyes[5][10] = false;
        eyes[5][11] = false;
        patternList.add(eyes);
    }

    boolean[][] randomPattern() {
        int type = rng.nextInt(6);
        return switch (type) {
            case 0 -> generateSymmetricDots();
            case 1 -> generateRandomCross();
            case 2 -> generateRandomDiamond();
            case 3 -> generateRandomLines();
            case 4 -> generateRandomBlocks();
            default -> generateRandomRing();
        };
    }

    boolean[][] generateSymmetricDots() {
        boolean[][] p = new boolean[ROWS][COLS];
        for (int r = 2; r < ROWS - 2; r++) {
            for (int c = 2; c < COLS / 2; c++) {
                if (rng.nextDouble() < 0.10) {
                    p[r][c] = true;
                    p[r][COLS - 1 - c] = true; // تقارن افقی
                }
            }
        }
        thicken(p);
        return p;
    }

        boolean[][] generateRandomCross() {
            boolean[][] p = new boolean[ROWS][COLS];
            int cx = 4 + rng.nextInt(COLS - 8);
            int cy = 4 + rng.nextInt(ROWS - 8);
            int len = 3 + rng.nextInt(4);

            for (int r = cy - len; r <= cy + len; r++)
                if (r >= 0 && r < ROWS) p[r][cx] = true;

            for (int c = cx - len; c <= cx + len; c++)
                if (c >= 0 && c < COLS) p[cy][c] = true;

            if (rng.nextBoolean()) thicken(p);
            return p;
        }

        boolean[][] generateRandomDiamond() {
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

        boolean[][] generateRandomLines() {
            Random rng = new Random();
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

        boolean[][] generateRandomBlocks() {
            boolean[][] p = new boolean[ROWS][COLS];
            int blocks = 3 + rng.nextInt(4);

            for (int i = 0; i < blocks; i++) {
                int h = 2 + rng.nextInt(3);
                int w = 2 + rng.nextInt(3);
                int sr = 1 + rng.nextInt(ROWS - h - 2);
                int sc = 1 + rng.nextInt(COLS - w - 2);

                for (int r = sr; r < sr + h; r++)
                    for (int c = sc; c < sc + w; c++)
                        p[r][c] = true;
            }
            return p;
        }

        boolean[][] generateRandomRing() {
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

        void thicken(boolean[][] p) {
            boolean[][] copy = new boolean[ROWS][COLS];
            for (int r = 0; r < ROWS; r++)
                System.arraycopy(p[r], 0, copy[r], 0, COLS);

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

    // ════════════════════════════════════════════════════
    //  SPIELLOGIK
    // ════════════════════════════════════════════════════
  //  void startRound() {
   //     for (boolean[] row : target) Arrays.fill(row, false);
     //   for (boolean[] row : player) Arrays.fill(row, false);
       // boolean[][] chosen = patternList.get(new Random().nextInt(patternList.size()));
     //   for (int r = 0; r < ROWS; r++) System.arraycopy(chosen[r], 0, target[r], 0, COLS);
    //    showSeconds = Math.max(2, 5 - level / 2);
      //  countdown = 3;
     //   lastTick  = System.currentTimeMillis();
     //   phase = Phase.COUNTDOWN;
       // msg1 = "Augen auf!"; msg2 = "Das Muster kommt gleich..."; msg3 = "";
  //  }//
        void startRound() {
            for (boolean[] row : target) Arrays.fill(row, false);
            for (boolean[] row : player) Arrays.fill(row, false);

            boolean[][] chosen;

            // 70٪ پترن ثابت، 30٪ پترن رندم
            if (rng.nextDouble() < 0.7) {
                chosen = patternList.get(rng.nextInt(patternList.size()));
            } else {
                chosen = randomPattern();
            }

            for (int r = 0; r < ROWS; r++) {
                System.arraycopy(chosen[r], 0, target[r], 0, COLS);
            }

            showSeconds = Math.max(2, 5 - level / 2);
            countdown = 3;
            lastTick  = System.currentTimeMillis();
            phase = Phase.COUNTDOWN;
            msg1 = "Augen auf!";
            msg2 = "Das Muster kommt gleich...";
            msg3 = "";
        }




    void startShow() {
        showTick = showSeconds;
        lastTick = System.currentTimeMillis();
        phase = Phase.SHOW;
        msg1 = "👁️  Muster einprägen!";
        msg2 = showTick + " Sekunden — schau genau hin!";
        msg3 = "";
    }

    void startDraw() {
        phase = Phase.HIDE;
        msg1  = "💡 Jetzt bist du dran!";
        msg2  = "Zeichne das Muster aus dem Gedächtnis!";
        msg3  = "Klicken oder ziehen";
        new Timer(800, e -> { phase = Phase.DRAW; ((Timer)e.getSource()).stop(); }).start();
    }

    void evaluate() {
        correctCells = 0; wrongCells = 0; missedCells = 0;
        int total = 0;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                if (target[r][c]) total++;
                if ( target[r][c] &&  player[r][c]) correctCells++;
                if (!target[r][c] &&  player[r][c]) wrongCells++;
                if ( target[r][c] && !player[r][c]) missedCells++;
            }
        accuracy = total == 0 ? 0 : Math.min(100, correctCells * 100.0 / (total + wrongCells));
        int pts = (int)(accuracy * level * 8);
        score  += pts;
        if (accuracy >= 75) level++;
        phase = Phase.RESULT;
        if      (accuracy == 100) msg1 = "🌟 PERFEKT! Du bist ein Gedächtnis-Champion!";
        else if (accuracy >= 80)  msg1 = "🎉 Super gemacht! Weiter so!";
        else if (accuracy >= 60)  msg1 = "👍 Gut! Noch ein bisschen üben!";
        else                      msg1 = "💪 Kein Problem! Beim nächsten Mal klappt's!";
        msg2 = String.format("Genauigkeit: %.0f%%   +%d Punkte", accuracy, pts);
        msg3 = String.format("✅ %d richtig   ❌ %d falsch   ⬜ %d vergessen", correctCells, wrongCells, missedCells);
    }

    // ════════════════════════════════════════════════════
    //  RENDERING
    // ════════════════════════════════════════════════════
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long t = System.currentTimeMillis() - startTime;

        // Hintergrund
        g2.setPaint(new GradientPaint(0, 0, new Color(15, 5, 40), 0, getHeight(), new Color(5, 0, 15)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                drawLED(g2, MARGIN + c * CELL, MARGIN + r * CELL, r, c, t);

        drawInfoBar(g2, t);
    }

    void drawLED(Graphics2D g2, int x, int y, int r, int c, long t) {
        int size = CELL - 4;
        boolean isTgt = target[r][c], isPly = player[r][c];
        Color color = OFF;
        float brightness = 0f;

        switch (phase) {
            case IDLE -> {
                double wave = Math.sin((r + c) * 0.5 - t * 0.004);
                if (wave > 0.3) {
                    color = RAINBOW[(int)Math.abs((r + c + t / 120)) % RAINBOW.length];
                    brightness = (float)((wave - 0.3) / 0.7) * 0.65f;
                }
            }
            case COUNTDOWN -> { color = new Color(80, 60, 200); brightness = (float)(Math.sin(t*0.006)*0.5+0.5)*0.45f; }
            case SHOW      -> { if (isTgt) { color = TARGET_ON; brightness = 1f; } }
            case HIDE      -> {}
            case DRAW      -> { if (isPly) { color = PLAYER_ON; brightness = 0.95f; } }
            case RESULT    -> {
                if (isTgt && isPly)   { color = CORRECT; brightness = 1f; }
                else if (!isTgt && isPly) {
                    color = WRONG;
                    brightness = (t / 300) % 2 == 0 ? 0.9f : 0.2f;  // blinken
                }
                else if (isTgt)       { color = MISSED; brightness = 0.7f; }
            }
        }

        // Ring-Körper
        g2.setColor(RING_BODY);
        g2.fillOval(x+1, y+1, size, size);

        // Glow
        if (brightness > 0) {
            for (int gl = 5; gl >= 1; gl--) {
                float alpha = brightness * (0.22f - gl * 0.035f);
                if (alpha <= 0) continue;
                g2.setColor(new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, Math.min(1f,alpha)));
                g2.fillOval(x-gl, y-gl, size+gl*2, size+gl*2);
            }
            g2.setColor(color);
            g2.fillOval(x+4, y+4, size-8, size-8);
            // Glanzpunkte
            g2.setColor(new Color(255,255,255,(int)(brightness*190)));
            g2.fillOval(x+size/2-3, y+size/2-3, 6, 6);
            g2.setColor(new Color(255,255,255,(int)(brightness*80)));
            g2.fillOval(x+size/2-7, y+size/2-7, 14, 14);
        }

        // Kontur
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(brightness > 0 ? color.darker() : new Color(45, 30, 75));
        g2.drawOval(x+1, y+1, size, size);
    }

    void drawInfoBar(Graphics2D g2, long t) {
        int barY = MARGIN + ROWS*CELL + 6, barW = COLS*CELL, barH = INFO_H-8;

        g2.setPaint(new GradientPaint(MARGIN, barY, new Color(25,15,60), MARGIN, barY+barH, new Color(15,8,40)));
        g2.fillRoundRect(MARGIN, barY, barW, barH, 15, 15);

        Color border = switch (phase) {
            case SHOW   -> TARGET_ON;
            case DRAW   -> PLAYER_ON;
            case RESULT -> accuracy >= 75 ? CORRECT : WRONG;
            default     -> new Color(100,80,200);
        };
        g2.setColor(border); g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(MARGIN, barY, barW, barH, 15, 15);

        String phaseText = switch (phase) {
            case IDLE      -> "✨ BEREIT";
            case COUNTDOWN -> "🔢  " + countdown + "...";
            case SHOW      -> "👁️  EINPRÄGEN — " + showTick + "s";
            case HIDE      -> "🙈  VERSTECKT";
            case DRAW      -> "✏️  ZEICHNEN";
            case RESULT    -> accuracy >= 75 ? "🏆  SUPER!" : "📊  ERGEBNIS";
        };
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(border);
        g2.drawString(phaseText, MARGIN+14, barY+24);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(220,200,255));
        String sc = "⭐ " + score + "   🎯 Level " + level;
        g2.drawString(sc, MARGIN + barW - g2.getFontMetrics().stringWidth(sc) - 14, barY+24);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        g2.drawString(msg1, MARGIN+14, barY+48);

        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(200,185,240));
        g2.drawString(msg2, MARGIN+14, barY+67);

        if (!msg3.isEmpty()) {
            g2.setColor(new Color(160,150,200));
            g2.drawString(msg3, MARGIN+14, barY+85);
        }

        // Schaltfläche
        boolean active = (phase==Phase.IDLE || phase==Phase.RESULT || phase==Phase.DRAW);
        String btnTxt  = phase==Phase.DRAW ? "✅  FERTIG!" : "▶  START";
        Color  btnCol  = phase==Phase.DRAW ? new Color(40,170,70) : active ? new Color(110,55,210) : new Color(45,35,75);
        int bw=115, bh=32, bx=MARGIN+barW-bw-12, by2=barY+barH-bh-8;

        if (active) {
            float p = (float)(Math.sin(t*0.005)*0.5+0.5);
            g2.setColor(new Color(btnCol.getRed()/255f,btnCol.getGreen()/255f,btnCol.getBlue()/255f,0.25f+p*0.25f));
            g2.fillRoundRect(bx-5, by2-5, bw+10, bh+10, 14, 14);
        }
        g2.setColor(btnCol); g2.fillRoundRect(bx, by2, bw, bh, 10, 10);
        g2.setColor(btnCol.brighter()); g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by2, bw, bh, 10, 10);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(btnTxt, bx+(bw-fm.stringWidth(btnTxt))/2, by2+bh/2+5);
    }

    // ════════════════════════════════════════════════════
    //  TIMER-SCHLEIFE
    // ════════════════════════════════════════════════════
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();
        if (phase == Phase.COUNTDOWN && now - lastTick >= 900) {
            countdown--; lastTick = now;
            if (countdown <= 0) startShow();
            else msg1 = countdown == 2 ? "Fertig machen..." : "Gleich geht's los!";
        }
        if (phase == Phase.SHOW && now - lastTick >= 1000) {
            showTick--; lastTick = now;
            msg2 = showTick > 0 ? "Noch " + showTick + " Sekunden — schau genau hin!" : "Zeit!";
            if (showTick <= 0) startDraw();
        }
        repaint();
    }

    // ════════════════════════════════════════════════════
    //  MAUSEINGABE
    // ════════════════════════════════════════════════════
    void touch(int mx, int my, boolean toggle) {
        if (phase != Phase.DRAW) return;
        int c = (mx-MARGIN)/CELL, r = (my-MARGIN)/CELL;
        if (r>=0 && r<ROWS && c>=0 && c<COLS) player[r][c] = toggle ? !player[r][c] : true;
    }

    void checkBtn(int mx, int my) {
        int barY=MARGIN+ROWS*CELL+6, barW=COLS*CELL;
        int bw=115, bh=32, bx=MARGIN+barW-bw-12, by=barY+(INFO_H-8)-bh-8;
        if (mx>=bx && mx<=bx+bw && my>=by && my<=by+bh) {
            if (phase==Phase.IDLE || phase==Phase.RESULT) startRound();
            else if (phase==Phase.DRAW) evaluate();
        }
    }

    @Override public void mouseClicked(MouseEvent e)  { checkBtn(e.getX(),e.getY()); touch(e.getX(),e.getY(),true); }
    @Override public void mouseDragged(MouseEvent e)  { touch(e.getX(),e.getY(),false); }
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}

    // ════════════════════════════════════════════════════
    //  MAIN — Hier startet alles!
    // ════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🎮 Memory Wall — LED Gedächtnisspiel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MemoryWall game = new MemoryWall();
            JScrollPane scrollPane = new JScrollPane(game);
            frame.setContentPane(scrollPane);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}