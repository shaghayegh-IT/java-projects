package memorywall.ui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Memory Wall — a memory game on a simulated 16x16 LED wall, inspired by
 * WS2812 LED rings. Players briefly see a lit pattern and then have to
 * redraw it from memory. Built as a small Java/Swing portfolio project;
 * no external libraries needed to run it.
 */
public final class MemoryWallApp {

    private MemoryWallApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🎮 Memory Wall — LED Gedächtnisspiel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MemoryWallPanel panel = new MemoryWallPanel();
            JScrollPane scrollPane = new JScrollPane(panel);
            frame.setContentPane(scrollPane);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
