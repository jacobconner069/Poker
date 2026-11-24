//imports libraries
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evaluates poker hands. Pure logic with no GUI code, so it can be unit tested.
 *
 * A hand is scored as a list of 7 integers: [category, tiebreaker1, tiebreaker2, ...].
 * Two scores are compared left to right (see compare). Card ranks are indexed
 * 0-12 for 2..A.
 */
public final class HandEvaluator {

    //category numbers used as the first entry of a score
    public static final int HIGH_CARD = 1;
    public static final int ONE_PAIR = 2;
    public static final int TWO_PAIR = 3;
    public static final int THREE_OF_A_KIND = 4;
    public static final int STRAIGHT = 5;
    public static final int FLUSH = 6;
    public static final int FULL_HOUSE = 7;
    public static final int FOUR_OF_A_KIND = 8;
    public static final int STRAIGHT_FLUSH = 9;
    public static final int ROYAL_FLUSH = 10;

    private static final String[] NAMES = {
        "", "High Card", "One Pair", "Two Pair", "Three of a Kind", "Straight",
        "Flush", "Full House", "Four of a Kind", "Straight Flush", "Royal Flush"
    };

    private static final String[] SUITS = {"-H", "-D", "-C", "-S"};
    private static final int SCORE_LENGTH = 7;

    private HandEvaluator() {}

    //returns the readable name of a score's category, like "Full House"
    public static String handName(List<Integer> score) {
        return NAMES[score.get(0)];
    }

    //scores the best 5-card hand that can be made from the given cards (2-7 cards like "10-H")
    public static ArrayList<Integer> evaluate(List<String> cards) {
        int[] rankCount = new int[13];
        List<List<Integer>> bySuit = new ArrayList<>();
        for (int i = 0; i < SUITS.length; i++) {
            bySuit.add(new ArrayList<>());
        }

        //tally ranks and group ranks by suit
        for (String card : cards) {
            if (card == null) continue;
            int dash = card.indexOf('-');
            int r = rankToIndex(card.substring(0, dash));
            if (r < 0) continue;
            rankCount[r]++;
            for (int s = 0; s < SUITS.length; s++) {
                if (card.endsWith(SUITS[s])) {
                    bySuit.get(s).add(r);
                }
            }
        }

        //distinct ranks present
        ArrayList<Integer> allRanks = new ArrayList<>();
        for (int r = 0; r < 13; r++) {
            if (rankCount[r] > 0) allRanks.add(r);
        }

        //groups of equal ranks (highest first)
        int quadsRank = -1;
        ArrayList<Integer> trips = new ArrayList<>();
        ArrayList<Integer> pairs = new ArrayList<>();
        for (int r = 12; r >= 0; r--) {
            if (rankCount[r] == 4) quadsRank = r;
            else if (rankCount[r] == 3) trips.add(r);
            else if (rankCount[r] == 2) pairs.add(r);
        }

        //a suit with 5+ cards is a flush
        ArrayList<Integer> flushRanks = null;
        for (List<Integer> suited : bySuit) {
            if (suited.size() >= 5) {
                flushRanks = new ArrayList<>(suited);
            }
        }

        ArrayList<Integer> result = new ArrayList<>();

        //straight flush / royal flush
        if (flushRanks != null) {
            int high = findStraightHigh(flushRanks);
            if (high != -1) {
                return build(high == 12 ? ROYAL_FLUSH : STRAIGHT_FLUSH, high);
            }
        }

        //four of a kind + best kicker
        if (quadsRank != -1) {
            return build(FOUR_OF_A_KIND, quadsRank, kickers(rankCount, 1, quadsRank));
        }

        //full house (trips + pair, or two sets of trips)
        if (!trips.isEmpty()) {
            int pairPart = -1;
            if (trips.size() >= 2) pairPart = trips.get(1);
            if (!pairs.isEmpty()) pairPart = Math.max(pairPart, pairs.get(0));
            if (pairPart != -1) {
                return build(FULL_HOUSE, trips.get(0), pairPart);
            }
        }

        //flush: top five cards of the suit
        if (flushRanks != null) {
            flushRanks.sort(Collections.reverseOrder());
            result.add(FLUSH);
            for (int i = 0; i < 5; i++) result.add(flushRanks.get(i));
            return pad(result);
        }

        //straight
        int straightHigh = findStraightHigh(allRanks);
        if (straightHigh != -1) {
            return build(STRAIGHT, straightHigh);
        }

        //three of a kind + two kickers
        if (!trips.isEmpty()) {
            return build(THREE_OF_A_KIND, trips.get(0), kickers(rankCount, 2, trips.get(0)));
        }

        //two pair + one kicker
        if (pairs.size() >= 2) {
            return build(TWO_PAIR, pairs.get(0), pairs.get(1), kickers(rankCount, 1, pairs.get(0), pairs.get(1)));
        }

        //one pair + three kickers
        if (pairs.size() == 1) {
            return build(ONE_PAIR, pairs.get(0), kickers(rankCount, 3, pairs.get(0)));
        }

        //high card: top five cards
        return build(HIGH_CARD, kickers(rankCount, 5));
    }

    //compares two scores: positive if a is better, negative if b is better, 0 for a tie
    public static int compare(List<Integer> a, List<Integer> b) {
        int len = Math.min(a.size(), b.size());
        for (int i = 0; i < len; i++) {
            int diff = a.get(i) - b.get(i);
            if (diff != 0) return diff;
        }
        return 0;
    }

    //returns the high-card index of a straight, or -1 if there is none
    //the wheel (A-2-3-4-5) counts as a straight with high card index 3 (the 5)
    public static int findStraightHigh(List<Integer> ranks) {
        List<Integer> unique = new ArrayList<>();
        for (int r : ranks) {
            if (!unique.contains(r)) unique.add(r);
        }
        if (unique.size() < 5) return -1;
        Collections.sort(unique);

        //check from the highest possible straight down
        for (int high = 12; high >= 4; high--) {
            boolean straight = true;
            for (int r = high - 4; r <= high; r++) {
                if (!unique.contains(r)) {
                    straight = false;
                    break;
                }
            }
            if (straight) return high;
        }

        //the wheel
        if (unique.contains(12) && unique.contains(0) && unique.contains(1)
                && unique.contains(2) && unique.contains(3)) {
            return 3;
        }
        return -1;
    }

    //converts a rank string ("2".."10","J","Q","K","A") to an index 0-12, or -1 if invalid
    public static int rankToIndex(String rank) {
        switch (rank) {
            case "2": return 0;
            case "3": return 1;
            case "4": return 2;
            case "5": return 3;
            case "6": return 4;
            case "7": return 5;
            case "8": return 6;
            case "9": return 7;
            case "10": return 8;
            case "J": return 9;
            case "Q": return 10;
            case "K": return 11;
            case "A": return 12;
            default: return -1;
        }
    }

    //highest 'count' ranks present that are not in the excluded list
    private static List<Integer> kickers(int[] rankCount, int count, Integer... excluded) {
        List<Integer> skip = java.util.Arrays.asList(excluded);
        List<Integer> out = new ArrayList<>();
        for (int r = 12; r >= 0 && out.size() < count; r--) {
            if (rankCount[r] > 0 && !skip.contains(r)) out.add(r);
        }
        return out;
    }

    //builds a padded score from a category followed by ints and/or lists of ints
    private static ArrayList<Integer> build(int category, Object... parts) {
        ArrayList<Integer> result = new ArrayList<>();
        result.add(category);
        for (Object part : parts) {
            if (part instanceof Integer) {
                result.add((Integer) part);
            } else {
                @SuppressWarnings("unchecked")
                List<Integer> list = (List<Integer>) part;
                result.addAll(list);
            }
        }
        return pad(result);
    }

    //pads a score with zeros so every score has the same length
    private static ArrayList<Integer> pad(ArrayList<Integer> score) {
        while (score.size() < SCORE_LENGTH) score.add(0);
        return score;
    }
}
