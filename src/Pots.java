import java.util.ArrayList;
import java.util.List;

public final class Pots {
    
    private Pots(){}

    /**
     * Works out how many chips each seat wins at showdown.
     *
     * @param contributions total chips each seat put in this hand (folded seats included)
     * @param live          true for seats that did not fold and can win
     * @param scores        each seat's HandEvaluator score (only read for live seats)
     * @param firstSeat     seat that receives odd leftover chips first, then clockwise
     * @return chips won by each seat; the amounts always add up to the total contributed
     */
    public static int[] distribute(int[] contributions, boolean[] live, List<List<Integer>> scores, int firstSeat){
        int n = contributions.length;
        int[] winnings = new int[n];
        List<Integer> levels = new ArrayList<>();
        for(int c : contributions){
            if (c > 0 && !levels.contains(c)){
                levels.add(c);
            }
        }
        java.util.Collections.sort(levels);
        int previous = 0;
        for(int level : levels){
            int layer = 0;
            for(int c : contributions){
                layer += Math.min(c,level) - Math.min(c,previous);
            }
            List<Integer> eligible = new ArrayList<>();
            for(int i=0;i<n;i++){
                if(live[i] && contributions[i] >=level){
                    eligible.add(i);
                }
            }
            if(eligible.isEmpty()){
                int most = -1;
                for(int i = 0;i<n;i++){
                    if(live[i] && contributions[i] > most){
                        most = contributions[i];
                    }
                }
                for (int i = 0;i<n;i++){
                    if(live[i] && contributions[i] == most){
                        eligible.add(i);
                    }
                }
            }
            List<Integer> winners = new ArrayList<>();
            for(int seat : eligible){
                if(winners.isEmpty()){
                    winners.add(seat);
                    continue;
                }
                int cmp = HandEvaluator.compare(scores.get(seat), scores.get(winners.get(0)));
                if(cmp>0){
                    winners.clear();
                    winners.add(seat);
                }
                else if(cmp==0){
                    winners.add(seat);
                }
            }
            int share = layer/winners.size();
            int leftover = layer % winners.size();
            for(int seat : winners){
                winnings[seat] += share;
            }
            for(int step = 0;step<n && leftover > 0;step++){
                int seat = (firstSeat + step) % n;
                if(winners.contains(seat)){
                    winnings[seat]++;
                    leftover--;
                }
            }
            previous=level;
        }
        return winnings;
    }
}
