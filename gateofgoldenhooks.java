import java.util.*;

public class UniqueSlotMachine {
    private static final int ROWS = 6;
    private static final int COLS = 5;
    private static final int MAX_WIN_MULTIPLIER = 28888;
    private static final String[] SYMBOLS = {
        "ZEUS", "POSEIDON", "TRIDENT", "OLYMPUS_RING",
        "FISH", "GOLDEN_BASS", "FLOAT", "HOOK",
        "WILD", "SCATTER"
    };
    private static final double RTP = 0.965;

    private Random rng = new Random();

    public String[][] spinReels() {
        String[][] grid = new String[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = SYMBOLS[rng.nextInt(SYMBOLS.length)];
            }
        }
        return grid;
    }

    public int evaluateWin(String[][] grid, int bet) {
        int multiplier = 0;
        int scatterCount = 0;
        int wildCount = 0;

        for (int r = 0; r < ROWS; r++) {
            Map<String, Integer> counts = new HashMap<>();
            for (int c = 0; c < COLS; c++) {
                String sym = grid[r][c];
                counts.put(sym, counts.getOrDefault(sym, 0) + 1);
                if (sym.equals("SCATTER")) scatterCount++;
                if (sym.equals("WILD")) wildCount++;
            }
            for (String sym : counts.keySet()) {
                if (counts.get(sym) >= 3 && !sym.equals("SCATTER") && !sym.equals("WILD")) {
                    multiplier += counts.get(sym) * 100;
                }
            }
        }

        if (scatterCount >= 4) {
            multiplier += scatterCount * 500;
        }

        if (wildCount > 0) {
            multiplier *= (1 + wildCount);
        }

        if (rng.nextDouble() < 0.0001) {
            multiplier = MAX_WIN_MULTIPLIER;
        }

        return Math.min(multiplier, MAX_WIN_MULTIPLIER) * bet;
    }

    public static void main(String[] args) {
        UniqueSlotMachine slot = new UniqueSlotMachine();
        int bet = 1;
        String[][] grid = slot.spinReels();
        for (String[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
        int win = slot.evaluateWin(grid, bet);
        System.out.println("You won: " + win + "x your bet!");
    }
}
