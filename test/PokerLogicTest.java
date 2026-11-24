//imports libraries
import java.util.*;

/**
 * Plain-Java tests for HandEvaluator and Pots (no JUnit needed).
 *
 * Run from the project root (see README):
 *   javac -d out src/*.java test/*.java
 *   java -cp out PokerLogicTest
 */
public class PokerLogicTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        deckIntegrity();
        handCategories();
        handComparisons();
        bruteForceComparison();
        potSplits();
        potConservation();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }


    // ---------------------------------------------------------------
    // deck integrity
    // ---------------------------------------------------------------

    private static void deckIntegrity() {
        Deck deck = new Deck();
        Set<String> dealt = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            String card = deck.dealCard();
            if (card != null) dealt.add(card);
        }
        check("a fresh deck contains 52 unique cards", dealt.size() == 52);
        check("a full deck is exhausted after 52 deals", deck.size() == 0 && deck.dealCard() == null);
    }

    // ---------------------------------------------------------------
    // hand categories
    // ---------------------------------------------------------------

    private static void handCategories() {
        category("royal flush", HandEvaluator.ROYAL_FLUSH, "A-S", "K-S", "Q-S", "J-S", "10-S", "2-D", "3-C");
        category("straight flush", HandEvaluator.STRAIGHT_FLUSH, "9-H", "8-H", "7-H", "6-H", "5-H", "A-D", "K-C");
        category("steel wheel (A-5 straight flush)", HandEvaluator.STRAIGHT_FLUSH, "A-C", "2-C", "3-C", "4-C", "5-C", "K-D", "9-H");
        category("four of a kind", HandEvaluator.FOUR_OF_A_KIND, "7-H", "7-D", "7-C", "7-S", "2-H", "9-D", "K-C");
        category("full house", HandEvaluator.FULL_HOUSE, "K-H", "K-D", "K-C", "4-S", "4-H", "9-D", "2-C");
        category("full house from two trips", HandEvaluator.FULL_HOUSE, "K-H", "K-D", "K-C", "4-S", "4-H", "4-D", "2-C");
        category("flush", HandEvaluator.FLUSH, "A-H", "J-H", "8-H", "6-H", "3-H", "K-D", "K-C");
        category("straight", HandEvaluator.STRAIGHT, "5-H", "6-D", "7-C", "8-S", "9-H", "2-D", "2-C");
        category("wheel straight", HandEvaluator.STRAIGHT, "A-H", "2-D", "3-C", "4-S", "5-H", "9-D", "K-C");
        category("broadway straight", HandEvaluator.STRAIGHT, "A-H", "K-D", "Q-C", "J-S", "10-H", "2-D", "2-C");
        category("three of a kind", HandEvaluator.THREE_OF_A_KIND, "9-H", "9-D", "9-C", "4-S", "2-H", "K-D", "J-C");
        category("two pair", HandEvaluator.TWO_PAIR, "9-H", "9-D", "4-C", "4-S", "2-H", "K-D", "J-C");
        category("three pair is still two pair", HandEvaluator.TWO_PAIR, "9-H", "9-D", "4-C", "4-S", "2-H", "2-D", "J-C");
        category("one pair", HandEvaluator.ONE_PAIR, "9-H", "9-D", "4-C", "7-S", "2-H", "K-D", "J-C");
        category("high card", HandEvaluator.HIGH_CARD, "A-H", "9-D", "4-C", "7-S", "2-H", "K-D", "J-C");
        category("two cards only (pocket pair before the flop)", HandEvaluator.ONE_PAIR, "Q-H", "Q-D");
        category("straight flush beats a flush hidden in same suit", HandEvaluator.STRAIGHT_FLUSH, "5-S", "6-S", "7-S", "8-S", "9-S", "A-S", "2-S");
        category("not a straight flush when straight and flush are different cards", HandEvaluator.FLUSH, "5-S", "6-S", "7-S", "8-S", "9-H", "A-S", "2-S");

        check("hand names", "Full House".equals(HandEvaluator.handName(
            HandEvaluator.evaluate(Arrays.asList("K-H", "K-D", "K-C", "4-S", "4-H")))));
    }

    private static void category(String name, int expected, String... cards) {
        int actual = HandEvaluator.evaluate(Arrays.asList(cards)).get(0);
        check(name + " (expected " + expected + ", got " + actual + ")", actual == expected);
    }

    // ---------------------------------------------------------------
    // comparing two hands
    // ---------------------------------------------------------------

    private static void handComparisons() {
        check("higher pair beats lower pair",
            beats(hand("A-H", "A-D", "2-C", "5-S", "9-H", "J-D", "3-C"), hand("K-H", "K-D", "2-C", "5-S", "9-H", "J-D", "3-C")));
        check("pair kicker decides",
            beats(hand("A-H", "9-D", "9-C", "5-S", "3-H", "J-D", "2-C"), hand("K-H", "9-D", "9-C", "5-S", "3-H", "J-D", "2-C")));
        check("flush: higher cards win",
            beats(hand("A-H", "9-H", "7-H", "5-H", "3-H", "K-D", "2-C"), hand("K-H", "9-H", "7-H", "5-H", "3-H", "Q-D", "2-C")));
        check("six-high straight beats wheel",
            beats(hand("2-H", "3-D", "4-C", "5-S", "6-H", "9-D", "K-C"), hand("A-H", "2-D", "3-C", "4-S", "5-H", "9-D", "K-C")));
        check("full house: bigger trips win",
            beats(hand("3-H", "3-D", "3-C", "2-S", "2-H", "9-D", "K-C"), hand("2-H", "2-D", "2-C", "A-S", "A-H", "9-D", "K-C")));
        check("same best five cards tie (board plays)",
            HandEvaluator.compare(
                hand("2-H", "3-D", "A-S", "A-D", "A-H", "K-C", "K-D"),
                hand("2-C", "4-D", "A-S", "A-D", "A-H", "K-C", "K-D")) == 0);
        check("two pair: low pair kicker from third pair",
            beats(hand("A-H", "A-D", "K-C", "K-S", "Q-H", "Q-D", "2-C"), hand("A-C", "A-S", "K-H", "K-D", "J-H", "J-D", "2-S")));
    }

    private static List<Integer> hand(String... cards) {
        return HandEvaluator.evaluate(Arrays.asList(cards));
    }

    private static boolean beats(List<Integer> a, List<Integer> b) {
        return HandEvaluator.compare(a, b) > 0 && HandEvaluator.compare(b, a) < 0;
    }

    // ---------------------------------------------------------------
    // compare against an independent brute-force evaluator
    // ---------------------------------------------------------------

    private static void bruteForceComparison() {
        Random rng = new Random(12345);
        int mismatches = 0;
        int trials = 100000;
        for (int t = 0; t < trials; t++) {
            List<String> deck = fullDeck();
            Collections.shuffle(deck, rng);
            List<String> board = deck.subList(0, 5);
            List<String> a = new ArrayList<>(board);
            a.addAll(deck.subList(5, 7));
            List<String> b = new ArrayList<>(board);
            b.addAll(deck.subList(7, 9));

            int mine = Integer.signum(HandEvaluator.compare(HandEvaluator.evaluate(a), HandEvaluator.evaluate(b)));
            int reference = Long.compare(bestFiveCardKey(a), bestFiveCardKey(b));
            if (mine != reference) mismatches++;
        }
        check("evaluator agrees with brute force on " + trials + " random showdowns (" + mismatches + " mismatches)", mismatches == 0);
    }

    //best score over all 5-card subsets of the given cards
    private static long bestFiveCardKey(List<String> cards) {
        long best = -1;
        int n = cards.size();
        for (int skip1 = 0; skip1 < n; skip1++) {
            for (int skip2 = skip1 + 1; skip2 < n; skip2++) {
                List<String> five = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    if (i != skip1 && i != skip2) five.add(cards.get(i));
                }
                best = Math.max(best, fiveCardKey(five));
            }
        }
        return best;
    }

    //scores exactly five cards as one long (category, then tiebreakers in base 15)
    private static long fiveCardKey(List<String> five) {
        int[] ranks = new int[5];
        Set<Character> suits = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            String c = five.get(i);
            ranks[i] = HandEvaluator.rankToIndex(c.substring(0, c.indexOf('-')));
            suits.add(c.charAt(c.length() - 1));
        }
        Arrays.sort(ranks);
        boolean flush = suits.size() == 1;

        boolean straight = true;
        for (int i = 1; i < 5; i++) if (ranks[i] != ranks[i - 1] + 1) straight = false;
        int straightHigh = ranks[4];
        if (!straight && ranks[4] == 12 && ranks[0] == 0 && ranks[1] == 1 && ranks[2] == 2 && ranks[3] == 3) {
            straight = true;
            straightHigh = 3;
        }

        //group by count, biggest group first, then higher rank first
        int[] counts = new int[13];
        for (int r : ranks) counts[r]++;
        List<int[]> groups = new ArrayList<>();
        for (int r = 12; r >= 0; r--) if (counts[r] > 0) groups.add(new int[]{counts[r], r});
        groups.sort((x, y) -> x[0] != y[0] ? y[0] - x[0] : y[1] - x[1]);

        int category;
        List<Integer> tiebreak = new ArrayList<>();
        if (straight && flush) {
            category = straightHigh == 12 ? 10 : 9;
            tiebreak.add(straightHigh);
        } else if (groups.get(0)[0] == 4) {
            category = 8;
        } else if (groups.get(0)[0] == 3 && groups.get(1)[0] == 2) {
            category = 7;
        } else if (flush) {
            category = 6;
        } else if (straight) {
            category = 5;
            tiebreak.add(straightHigh);
        } else if (groups.get(0)[0] == 3) {
            category = 4;
        } else if (groups.get(0)[0] == 2 && groups.get(1)[0] == 2) {
            category = 3;
        } else if (groups.get(0)[0] == 2) {
            category = 2;
        } else {
            category = 1;
        }
        if (tiebreak.isEmpty()) for (int[] g : groups) tiebreak.add(g[1]);

        long key = category;
        for (int i = 0; i < 5; i++) {
            key = key * 15 + (i < tiebreak.size() ? tiebreak.get(i) : 0);
        }
        return key;
    }

    private static List<String> fullDeck() {
        List<String> deck = new ArrayList<>();
        for (String suit : new String[]{"-H", "-D", "-C", "-S"}) {
            for (String rank : new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"}) {
                deck.add(rank + suit);
            }
        }
        return deck;
    }

    // ---------------------------------------------------------------
    // pots and side pots
    // ---------------------------------------------------------------

    //scores for seats: each entry is a made-up one-number hand strength
    private static List<List<Integer>> strengths(int... s) {
        List<List<Integer>> scores = new ArrayList<>();
        for (int v : s) {
            scores.add(v < 0 ? null : new ArrayList<>(Arrays.asList(v, 0, 0, 0, 0, 0, 0)));
        }
        return scores;
    }

    private static void potSplits() {
        //simple winner takes everything
        int[] w = Pots.distribute(new int[]{10, 10, 10}, new boolean[]{true, true, true}, strengths(3, 5, 4), 0);
        check("single winner takes the pot", Arrays.equals(w, new int[]{0, 30, 0}));

        //folded player's chips stay in the pot
        w = Pots.distribute(new int[]{10, 10, 10}, new boolean[]{true, false, true}, strengths(5, -1, 4), 0);
        check("folded chips go to the winner", Arrays.equals(w, new int[]{30, 0, 0}));

        //tie splits evenly
        w = Pots.distribute(new int[]{10, 10, 10, 10}, new boolean[]{true, true, true, true}, strengths(5, 5, 2, 1), 0);
        check("tie splits evenly", Arrays.equals(w, new int[]{20, 20, 0, 0}));

        //odd chip goes to the first winner at/after firstSeat
        w = Pots.distribute(new int[]{5, 5, 5}, new boolean[]{true, true, true}, strengths(5, 5, 5), 1);
        check("odd chips: 15 split 3 ways is 5 each", Arrays.equals(w, new int[]{5, 5, 5}));
        w = Pots.distribute(new int[]{5, 5, 5}, new boolean[]{true, true, false}, strengths(5, 5, -1), 1);
        check("15 split two ways: seat 1 gets the odd chip", Arrays.equals(w, new int[]{7, 8, 0}));

        //short all-in: best hand only wins what it covered
        //seat0 all-in for 20 (best hand), seats 1 and 2 put in 50 each (seat 1 better of the two)
        w = Pots.distribute(new int[]{20, 50, 50}, new boolean[]{true, true, true}, strengths(9, 6, 3), 0);
        check("short all-in wins main pot only", Arrays.equals(w, new int[]{60, 60, 0}));

        //short all-in has the worst hand: main pot goes to the next best
        w = Pots.distribute(new int[]{20, 50, 50}, new boolean[]{true, true, true}, strengths(1, 6, 3), 0);
        check("short all-in loses everything", Arrays.equals(w, new int[]{0, 120, 0}));

        //three different all-in amounts
        w = Pots.distribute(new int[]{10, 30, 60}, new boolean[]{true, true, true}, strengths(9, 7, 5), 0);
        check("layered side pots: 30 / 40 / 30", Arrays.equals(w, new int[]{30, 40, 30}));

        //uncalled bet returns to the bettor
        w = Pots.distribute(new int[]{50, 20, 10}, new boolean[]{true, true, false}, strengths(1, 9, -1), 0);
        check("uncalled part of a bet comes back", Arrays.equals(w, new int[]{30, 50, 0}));
    }

    private static void potConservation() {
        Random rng = new Random(99);
        boolean ok = true;
        for (int t = 0; t < 50000; t++) {
            int n = 2 + rng.nextInt(6);
            int[] contrib = new int[n];
            boolean[] live = new boolean[n];
            List<List<Integer>> scores = new ArrayList<>();
            int liveCount = 0;
            int total = 0;
            for (int i = 0; i < n; i++) {
                contrib[i] = 1 + rng.nextInt(99);
                live[i] = rng.nextInt(3) != 0;
                if (live[i]) liveCount++;
                total += contrib[i];
                scores.add(new ArrayList<>(Arrays.asList(1 + rng.nextInt(10), rng.nextInt(13), 0, 0, 0, 0, 0)));
            }
            if (liveCount == 0) continue;
            int[] w = Pots.distribute(contrib, live, scores, rng.nextInt(n));
            int sum = 0;
            for (int i = 0; i < n; i++) {
                sum += w[i];
                if (w[i] < 0 || (!live[i] && w[i] > 0)) ok = false;
            }
            if (sum != total) ok = false;
        }
        check("pots always pay out exactly what was put in, only to live players (50000 random hands)", ok);
    }

    // ---------------------------------------------------------------

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  ok    " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }
}