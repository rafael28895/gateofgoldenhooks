import java.util.*;

public class UniqueSlotMachine {

    private static final int ROWS = 6;
    private static final int COLS = 5;
    private static final int MAX_WIN_MULTIPLIER = 28888; // 28,888x
    private static final double MIN_BET = 0.28;
    private static final double BONUS_BUY_COST = 28.88;
    private static final String[] SYMBOLS = {
        "ZEUS", "POSEIDON", "TRIDENT", "OLYMPUS_RING",
        "FISH", "GOLDEN_BASS", "FLOAT", "HOOK",
        "WILD", "SCATTER"
    };

    private Random rng = new Random();
    private double hotness = 0.25; // 0.0 (cold) to 1.0 (hot); can be adjusted dynamically

    // Calculate bet for a given level (0 = min bet, 1 = next, etc.)
    public double getBetAmount(int betLevel) {
        return MIN_BET * Math.pow(2, betLevel);
    }

    // Calculate bonus buy cost for a given level
    public double getBonusBuyCost(int betLevel) {
        return BONUS_BUY_COST * Math.pow(2, betLevel);
    }

    // Spin reels and return the grid
    public String[][] spinReels() {
        String[][] grid = new String[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = SYMBOLS[rng.nextInt(SYMBOLS.length)];
            }
        }
        return grid;
    }

    // Count scatter symbols in the grid
    public int countScatters(String[][] grid) {
        int scatterCount = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].equals("SCATTER")) scatterCount++;
            }
        }
        return scatterCount;
    }

    // Calculate base win for a grid (simple matching logic)
    public int calculateBaseWin(String[][] grid) {
        int multiplier = 0;
        int wildCount = 0;
        for (int r = 0; r < ROWS; r++) {
            Map<String, Integer> counts = new HashMap<>();
            for (int c = 0; c < COLS; c++) {
                String sym = grid[r][c];
                counts.put(sym, counts.getOrDefault(sym, 0) + 1);
                if (sym.equals("WILD")) wildCount++;
            }
            for (String sym : counts.keySet()) {
                if (counts.get(sym) >= 3 && !sym.equals("SCATTER") && !sym.equals("WILD")) {
                    multiplier += counts.get(sym) * 10;
                }
            }
        }
        if (wildCount > 0) {
            multiplier *= (1 + wildCount);
        }
        return multiplier;
    }

    // Determine if Zeus/Poseidon throws a multiplier this spin
    public int getRandomMultiplier() {
        double baseChance = 0.10; // 10% base
        double multiplierChance = baseChance + hotness * 0.4; // up to 50%
        if (rng.nextDouble() < multiplierChance) {
            return 2 + rng.nextInt(499); // 2x to 500x
        }
        return 1;
    }

    // Run a single normal spin
    public double normalSpin(int betLevel) {
        double bet = getBetAmount(betLevel);
        String[][] grid = spinReels();
        int baseWin = calculateBaseWin(grid);
        int multiplier = getRandomMultiplier();
        double win = baseWin * bet * multiplier;
        if (win > bet * MAX_WIN_MULTIPLIER) {
            win = bet * MAX_WIN_MULTIPLIER;
        }
        System.out.println("Normal Spin Grid:");
        printGrid(grid);
        if (multiplier > 1) {
            System.out.println("Zeus/Poseidon throws a multiplier: " + multiplier + "x!");
        }
        System.out.println("Win: " + String.format("%.2f", win));
        return win;
    }

    // Trigger bonus round (used for both scatter trigger and bonus buy)
    public double bonusRound(int betLevel, int scatterCount) {
        double bet = getBetAmount(betLevel);
        int bonusSpins = 0;
        if (scatterCount == 4) bonusSpins = 10;
        else if (scatterCount == 5) bonusSpins = 15;
        else if (scatterCount >= 6) bonusSpins = 20;
        else bonusSpins = 10; // For bonus buy or fallback

        double totalWin = 0;
        for (int spin = 1; spin <= bonusSpins; spin++) {
            String[][] grid = spinReels();
            int baseWin = calculateBaseWin(grid);
            int multiplier = getRandomMultiplier();
            double win = baseWin * bet * multiplier;
            if (win > bet * MAX_WIN_MULTIPLIER) {
                win = bet * MAX_WIN_MULTIPLIER;
            }
            totalWin += win;
            System.out.println("Bonus Spin " + spin + ":");
            printGrid(grid);
            if (multiplier > 1) {
                System.out.println("Zeus/Poseidon throws a multiplier: " + multiplier + "x!");
            }
            System.out.println("Win: " + String.format("%.2f", win));
        }
        System.out.println("Total Bonus Win: " + String.format("%.2f", totalWin));
        return totalWin;
    }

    // Bonus buy feature
    public void bonusBuy(int betLevel) {
        double bet = getBetAmount(betLevel);
        double cost = getBonusBuyCost(betLevel);
        System.out.println("Bonus Buy activated! Cost: " + String.format("%.2f", cost) + " | Bet: " + String.format("%.2f", bet));
        bonusRound(betLevel, 4);
    }

    // Print the slot grid
    public void printGrid(String[][] grid) {
        for (String[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
    }

    public static void main(String[] args) {
        UniqueSlotMachine slot = new UniqueSlotMachine();
        Scanner scanner = new Scanner(System.in);
        int betLevel = 0;

        System.out.println("Welcome to the Unique Slot Machine!");
        System.out.println("Minimum bet: 0.28. Each level doubles the bet.");
        System.out.println("Enter your bet level (0 for 0.28, 1 for 0.56, 2 for 1.12, etc.):");
        betLevel = scanner.nextInt();

        double bet = slot.getBetAmount(betLevel);
        double bonusBuyCost = slot.getBonusBuyCost(betLevel);

        System.out.println("Your bet: " + String.format("%.2f", bet));
        System.out.println("Bonus Buy cost: " + String.format("%.2f", bonusBuyCost));
        System.out.println("Press 1 for normal spin, 2 for bonus buy, 3 to spin until you trigger the bonus by scatters:");

        int choice = scanner.nextInt();

        if (choice == 2) {
            slot.bonusBuy(betLevel);
        } else if (choice == 1) {
            slot.normalSpin(betLevel);
        } else if (choice == 3) {
            boolean triggered = false;
            int tries = 0;
            while (!triggered) {
                String[][] grid = slot.spinReels();
                int scatterCount = slot.countScatters(grid);
                tries++;
                if (scatterCount >= 4) {
                    System.out.println("Bonus triggered after " + tries + " spins with " + scatterCount + " scatters!");
                    slot.bonusRound(betLevel, scatterCount);
                    triggered = true;
                }
            }
        }
    }
}
