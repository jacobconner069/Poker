import java.awt.*;
import javax.swing.*;

public class Player {

    //class variables
    public static final int STARTING_CHIPS = 99;
    private static final Color GREEN = new Color(0, 130, 0);

    private final int seat;
    private int chips = STARTING_CHIPS;
    private boolean folded = false;
    private boolean allIn = false;
    private boolean out = false;
    private int betThisTurn = 0;
    private int totalContributed = 0;
    private final String[] hand = new String[2];
    private String statusOverride = null;
    private final JLabel total;
    private final JLabel foldedLabel;
    private JPanel down;
    private JPanel up;
    private final JLabel cardd1 = new JLabel(Images.cardBackSmall());
    private final JLabel cardd2 = new JLabel(Images.cardBackSmall());

    //constructor
    public Player(int seat){
        this.seat = seat;
        total = new JLabel("Total $" + chips);
        foldedLabel = new JLabel("playing");
    }

    //get functions
    public int getChips(){
        return chips;
    }

    public int getSeat(){
        return seat;
    }

    public boolean hasFolded(){
        return folded;
    }

    public boolean isAllIn(){
        return allIn;
    }

    public boolean isOut(){
        return out;
    }

    public boolean isInHand(){
        return !folded && !out;
    }

    public String getCard(int index){
        return hand[index];
    }

    public int getBet(){
        return betThisTurn;
    }

    public int getTotalContributed(){
        return totalContributed;
    }

    public JLabel getFoldedLabel(){
        return foldedLabel;
    }
    
    public JLabel getTotalLabel(){
        return total;
    }

    //set functions
    public void resetBet(){
        betThisTurn = 0;
    }

    public void setStatusOverride(String status){
        statusOverride = status;
        setFoldedLabel();
    }

    public void setTotalLabel(){
        total.setText("Total: $" + chips);
    }

    public void setFoldedLabel(){
        if(statusOverride != null){
            foldedLabel.setText(statusOverride);
            foldedLabel.setForeground(GREEN);
        } else if (out){
            foldedLabel.setText("out");
            foldedLabel.setForeground(Color.GRAY);
        } else if (allIn && !folded){
            foldedLabel.setText("ALL IN");
            foldedLabel.setForeground(Color.RED);
        } else if (folded){
            foldedLabel.setText("folded");
            foldedLabel.setForeground(Color.BLACK);
        } else{
            foldedLabel.setText("Playing");
            foldedLabel.setForeground(Color.BLACK);
        }
    }

    public void addChips(int amount){
        chips += amount;
    }

    public void fold(){
        folded = true;
    }

    public void sitOut(){
        out = true;
        folded = true;
    }

    //other functions
    public void resetForNewHand(){
        folded = false;
        allIn = false;
        out = false;
        betThisTurn = 0;
        totalContributed = 0;
        hand[0] = null;
        hand[1] = null;
        statusOverride = null;

        cardd1.setIcon(Images.cardBackSmall());
        cardd2.setIcon(Images.cardBackSmall());
        setFoldedLabel();
        setTotalLabel();

        if(up!=null){
            up.revalidate();
            up.repaint();
        }
        if(down!=null){
            down.revalidate();
            down.repaint();
        }
    }

    public void addCards(String card){
        if(hand[0] == null){
            hand[0] = card;
        } else if (hand[1] == null){
            hand[1] = card;
        }
    }

    public JPanel createSeatPanel(){
        JPanel playerPanel = new JPanel(new GridLayout(3,1));
        JPanel name = new JPanel();
        name.add(new JLabel("Player " + (seat+1)));

        up = new JPanel();
        cardd1.setIcon(Images.cardBackSmall());
        cardd2.setIcon(Images.cardBackSmall());
        up.add(cardd1);
        up.add(cardd2);
        
        down = new JPanel();
        setFoldedLabel();
        setTotalLabel();
        down.add(getFoldedLabel());
        down.add(getTotalLabel());
        playerPanel.add(name);
        playerPanel.add(up);
        playerPanel.add(down);
        return playerPanel;
    }

    public void showCards(){
        if(up==null || hand[0] == null || hand[1] == null){
            return;
        }
        cardd1.setIcon(Images.cardSmall(hand[0]));
        cardd2.setIcon(Images.cardSmall(hand[1]));
        up.revalidate();
        up.repaint();
    }

    public int pay(int amount){
        int paid = Math.max(0, Math.min(amount, chips));
        chips -= paid;
        betThisTurn += paid;
        totalContributed += paid;
        if(chips==0){
            allIn = true;
        }
        return paid;
    }
}
