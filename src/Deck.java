import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    
    private final List<String> cards = new ArrayList<>();

    public Deck(){
        initialize();
        shuffle();
    }

    private void initialize(){
        cards.clear();
        String[] suits = {"-H", "-D", "-C", "-S"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        for(String suit : suits){
            for(String rank : ranks){
                cards.add(rank + suit);
            }
        }
    }

    public final void shuffle(){
        Collections.shuffle(cards);
    }

    public int size(){
        return cards.size();
    }

    public String dealCard(){
        if (cards.isEmpty()){
            return null;
        }
        return cards.remove(cards.size() -1);
    }
}
