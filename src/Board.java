import java.util.ArrayList;
import java.util.List;

public class Board {
    
    public static final int ANTE = 5;
    private final String[] river = new String[5];
    private int currentBet = ANTE;

    //creates a board object
    public Board(){}

    //deals out the next river card
    public void dealRiver(String card){
        for(int i=0;i<river.length;i++){
            if(river[i] == null){
                river[i] = card;
                return;
            }
        }
    }

    //returns the amount a player has bet for the round
    public int getCurrentBet(){
        return currentBet;
    }

    public String getCard(int index){
        return river[index];
    }

    public List<String> getCards(int count){
        List<String> cards = new ArrayList<>();
        for(int i = 0; i<count && i < river.length;i++){
            if(river[i] != null){
                cards.add(river[i]);
            }
        }
        return cards;
    }

    public void setCurrentBet(int amount){
        currentBet = amount;
    }
}
