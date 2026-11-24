import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class App {
    
    private static final int MAX_RAISES = 4;
    private static final int MIN_RAISE = Board.ANTE;

    private int numPlayers;
    private int half;
    private int half2;

    private final CardLayout cardLayout;
    private final JFrame frame;
    private final JPanel main;
    private final JPanel panel1;
    private final JPanel panel2;
    private JPanel midCards;
    private JLabel turnLabel;
    private JTextField raiseField;
    private JLabel error;
    private JLabel message;
    private JLabel betNeeded;
    private JLabel pot;
    private JButton start;
    private JButton next;
    private JButton newGame;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel chips;
    private JPanel bottomPanel;
    private JPanel cards;
    private final JPanel labelPanel;
    private final JPanel buttonPanel;
    private final JLabel label;
    private JLabel cardd1;
    private JLabel cardd2;
    private JPanel actionButtons;
    private JButton fold;
    private JButton call;
    private JButton raise;
    private JLabel money;
    private JPanel chipss;

    private final ArrayList<Player> playerList = new ArrayList<>();
    private Deck deck = new Deck();
    private Board board = new Board();
    private int potAmount = 0;
    private int turn = 0;
    private int first = 0;
    private int street = 0;
    private int raisesThisStreet = 0;
    private int lastFullRaiseSize = MIN_RAISE;
    private boolean[] acted = new boolean[0];
    private boolean handStarted = false;
    private boolean handOver = false;
    private boolean gameOver = false;


    public App(){
        cardLayout = new CardLayout();
        frame = new JFrame("Poker Game");
        label = new JLabel("How many players?");
        main = new JPanel(cardLayout);
        panel1 = new JPanel();
        panel2 = new JPanel();

        panel1.setLayout(new BorderLayout());
        labelPanel = new JPanel();
        labelPanel.add(label);
        panel1.add(labelPanel, BorderLayout.NORTH);
        buttonPanel = new JPanel();
        panel1.add(buttonPanel, BorderLayout.CENTER);

        main.add(panel1, "one");
        main.add(panel2, "two");

        frame.add(main);
        frame.setSize(900, 700);
        frame.setMinimumSize(new Dimension(800, 680));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        for(int i=2;i<=7;i++){
            JButton btn = new JButton(String.valueOf(i));
            int chosenValue = i;
            btn.addActionListener(e -> {
                numPlayers = chosenValue;
                half = numPlayers/2;
                half2 = (numPlayers-1)/2;
                createPlayers();
                setupPanel2(panel2);
                cardLayout.show(main, "two");
            });
            buttonPanel.add(btn);
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new App());
    }

    private void createPlayers(){
        playerList.clear();
        for(int i=0;i<numPlayers;i++){
            playerList.add(new Player(i));
        }
        deck = new Deck();
        board = new Board();
        potAmount = 0;
        turn = 0;
        first = 0;
        street = 0;
        raisesThisStreet = 0;
        lastFullRaiseSize = MIN_RAISE;
        acted = new boolean[numPlayers];
        handStarted = false;
        handOver = false;
        gameOver = false;
    }

    //creates the display for the game
    private void setupPanel2(JPanel panel2){
        // Rebuild the table when the user starts another tournament. Without
        // clearing this panel, old controls and listeners accumulate.
        panel2.removeAll();
        panel2.setLayout(new BorderLayout());

        //top
        panel2.setLayout(new BorderLayout());
        topPanel = new JPanel(new GridLayout(2,1));
        JPanel controls = new JPanel();
        JPanel messages = new JPanel();
        pot = new JLabel("Pot: $0");
        start = new JButton("Start");
        next = new JButton("Next");
        betNeeded = new JLabel("Bet Needed: $" + board.getCurrentBet());
        error = new JLabel("");
        error.setForeground(Color.RED);
        message = new JLabel("Click Start to deal the first hand.");
        message.setFont(message.getFont().deriveFont(Font.BOLD));
        turnLabel = new JLabel("");
        newGame = new JButton("New Game");
        newGame.setEnabled(false);
        next.setEnabled(false);
        controls.add(newGame);
        controls.add(next);
        controls.add(start);
        controls.add(pot);
        controls.add(betNeeded);
        controls.add(turnLabel);
        messages.add(message);
        messages.add(error);
        topPanel.add(controls);
        topPanel.add(messages);
        panel2.add(topPanel, BorderLayout.NORTH);

        //center
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2,1));
        midCards = new JPanel();
        midCards.setLayout(new FlowLayout());
        centerPanel.add(midCards);
        chips = new JPanel();
        chips.setLayout(new FlowLayout());
        centerPanel.add(chips);
        panel2.add(centerPanel, BorderLayout.CENTER);

        //bottom
        bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(0, 150));
        panel2.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //bottom (cards)
        cards = new JPanel();
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        cardd1 = new JLabel(Images.cardBack());
        cardd2 = new JLabel(Images.cardBack());
        cards.add(cardd1);
        cards.add(cardd2);
        bottomPanel.add(cards, gbc);

        //bottom (chips/money)
        chipss = new JPanel();
        chipss.setLayout(new GridLayout(1,4,2,2));
        money = new JLabel("Total: $" + playerList.get(0).getChips());
        money.setHorizontalAlignment(SwingConstants.CENTER);
        setPlayerChips(chipss);
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        bottomPanel.add(chipss, gbc);

        //bottom (buttons)
        actionButtons = new JPanel();
        fold = new JButton("Fold");
        call = new JButton("Call");
        raise = new JButton("Raise");
        fold.setEnabled(false);
        call.setEnabled(false);
        raise.setEnabled(false);
        actionButtons.add(fold);
        actionButtons.add(call);
        actionButtons.add(raise);
        raiseField = new JTextField(5);
        actionButtons.add(raiseField);
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        bottomPanel.add(actionButtons, gbc);

        //left and right players
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(half, 1));
        int playerIndex = 1;
        for(int i = 0; i < half && playerIndex < playerList.size(); i++, playerIndex++){
            leftPanel.add(playerList.get(playerIndex).createSeatPanel());
        }
        panel2.add(leftPanel, BorderLayout.WEST);
        if(numPlayers > 2){
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new GridLayout(half2, 1));
            for(int i = 0; i < half2 && playerIndex < playerList.size(); i++, playerIndex++){
                rightPanel.add(playerList.get(playerIndex).createSeatPanel());
            }
            panel2.add(rightPanel, BorderLayout.EAST);
        }

        //button action listeners
        start.addActionListener(e->{
            start.setEnabled(false);
            beginHand();
        });

        next.addActionListener(e->{
            if(!handStarted || handOver || turn == 0){
                return;
            }
            aiTurn(turn);
            advance();
        });

        fold.addActionListener(e->{
            if(!humanCanAct()) return;
            doFold(0);
            advance();
        });

        call.addActionListener(e->{
            if(!humanCanAct()) return;
            error.setText("");
            doCall(0);
            advance();
        });

        raise.addActionListener(e->{
            if(!humanCanAct()) return;
            if(validateAndRaise()){
                advance();
            }
        });

        newGame.addActionListener(e->{
            if(gameOver){
                cardLayout.show(main, "one");
                return;
            }
            first = nextSeatWithChips(first);
            beginHand();
        });

        panel2.revalidate();
        panel2.repaint();
    }

    private void setPlayerChips(JPanel panel){
        panel.removeAll();
        Player human = playerList.get(0);
        int number = human.getChips();
        if (number>=25){
            panel.add(new JLabel(Images.chip("Black")));
            number %= 25;
        }
        if(number >= 5){
            panel.add(new JLabel(Images.chip("Red")));
            number %= 5;
        }
        if(number > 0){
            panel.add(new JLabel(Images.chip("Green")));
        }

        String status = "";
        if(handStarted && !handOver && human.isAllIn() && !human.hasFolded()){
            status = " (ALL IN)";
        } else if (human.hasFolded() && !human.isOut()){
            status = "(folded)";
        }
        money.setText("Total: $" + human.getChips() + status);
        panel.add(money);
    }

    private void beginHand(){
        deck = new Deck();
        board = new Board();
        potAmount = 0;
        street = 0;
        raisesThisStreet = 0;
        lastFullRaiseSize = MIN_RAISE;
        acted = new boolean[numPlayers];
        handStarted = true;
        handOver = false;
        error.setText("");
        raiseField.setText("");

        for(Player p : playerList){
            p.resetForNewHand();
            if(p.getChips()>0){
                p.addCards(deck.dealCard());
                p.addCards(deck.dealCard());
            } else {
                p.sitOut();
                p.setFoldedLabel();
            }
        }

        Player human = playerList.get(0);
        cardd1.setIcon(Images.card(human.getCard(0)));
        cardd2.setIcon(Images.card(human.getCard(1)));
        for(int i = 0;i<5;i++){
            board.dealRiver(deck.dealCard());
        }
        for(Player p : playerList){
            if (p.isInHand()){
                potAmount += p.pay(Board.ANTE);
            }
        }
        board.setCurrentBet(Board.ANTE);
        turn = prevSeat(first);
        say("New hand dealt. Everyone antes $" + Board.ANTE + ".");
        advance();
    }

    private void advance(){
        if(liveCount() == 1){
            awardByFold();
            return;
        }

        while(bettingComplete()){
            if(street == 3){
                showdown();
                return;
            }
            startStreet(street + 1);
        }
        turn = nextActor(turn);
        refreshDisplay();
    }

    private void startStreet(int newStreet){
        street = newStreet;
        for(Player p : playerList){
            p.resetBet();
        }
        acted = new boolean[numPlayers];
        board.setCurrentBet(0);
        raisesThisStreet = 0;
        lastFullRaiseSize = MIN_RAISE;
        turn = prevSeat(first);
    }

    private boolean bettingComplete(){
        int canAct = 0;
        for(int i = 0; i<numPlayers;i++){
            Player p = playerList.get(i);
            if(p.isInHand() && !p.isAllIn()){
                canAct++;
                if(p.getBet() < board.getCurrentBet()){
                    return false;
                }
            }
        }
        if(canAct >=2){
            for(int i=0;i<numPlayers;i++){
                Player p = playerList.get(i);
                if(p.isInHand() && !p.isAllIn() && !acted[i]){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean needsAction(int seat){
        Player p = playerList.get(seat);
        return p.isInHand() && !p.isAllIn() && (!acted[seat] || p.getBet() < board.getCurrentBet());
    }

    private int nextActor(int from){
        for (int step = 1; step <= numPlayers; step++){
            int seat = (from+step) % numPlayers;
            if (needsAction(seat)){
                return seat;
            }
        }
        return from;
    }

    private int liveCount(){
        int count = 0;
        for (Player p : playerList){
            if(p.isInHand()){
                count++;
            }
        }
        return count;
    }

    private int prevSeat(int seat){
        return (seat - 1 + numPlayers) % numPlayers;
    }

    private int nextSeatWithChips(int seat){
        for (int step = 1; step <= numPlayers; step++){
            int candidate = (seat + step) % numPlayers;
            if(playerList.get(candidate).getChips() > 0){
                return candidate;
            }
        }
        return seat;
    }

    private boolean humanCanAct(){
        return handStarted && !handOver && turn == 0 && needsAction(0);
    }

    private int amountToCall(Player p){
        return Math.max(0, board.getCurrentBet() - p.getBet());
    }

    private void doFold(int seat){
        playerList.get(seat).fold();
        acted[seat] = true;
        say(who(seat) + " " + verb(seat, "fold"));
    }

    private void doCall(int seat){
        Player p = playerList.get(seat);
        int toCall = amountToCall(p);
        int paid = p.pay(toCall);
        potAmount += paid;
        acted[seat] = true;

        if(toCall == 0){
            say(who(seat) + " " + verb(seat, "check"));
        }
        else if (p.isAllIn()) {
            say(who(seat) + " " + verb(seat, "call") + "$" + paid + " and " + (seat==0 ? "are" : "is") + " ALL IN");
        }
        else{
            say(who(seat) + " " + verb(seat, "call") + "$" + paid);
        }
    }

    private void doRaise(int seat, int raiseAmount){
        Player p = playerList.get(seat);
        int oldBet = board.getCurrentBet();
        int paid = p.pay(amountToCall(p) + raiseAmount);
        potAmount += paid;
        acted[seat] = true;

        if(p.getBet() > oldBet){
            int raiseSize = p.getBet() - oldBet;
            board.setCurrentBet(p.getBet());
            boolean fullRaise = raiseSize >= lastFullRaiseSize;
            if(fullRaise){
                lastFullRaiseSize = raiseSize;
                raisesThisStreet++;
                for(int i=0; i<numPlayers;i++){
                    if (i != seat){
                        acted[i] = false;
                    }
                }
            }
            say(who(seat) + " " + verb(seat, "raise") + " $" + raiseSize +
                (p.isAllIn() ? " (ALL IN)" : "") +
                (fullRaise ? "" : " (short raise; action not reopened)"));
        }
        else{
            say(who(seat) + " " + verb(seat, "call") + " $" + paid + " (ALL IN)");
        }
    }

    private boolean validateAndRaise(){
        Player p = playerList.get(0);
        String value = raiseField.getText().trim();
        if(value.isEmpty()){
            error.setText("Enter a raise amount.");
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            error.setText("Enter a whole number.");
            return false;
        }
        int room = p.getChips() - amountToCall(p);
        if(amount <=0){
            error.setText("Raise must be more than $0.");
            return false;
        }
        if(acted[0]){
            error.setText("A short all-in raise does not reopen raising for you.");
            return false;
        }
        if(raisesThisStreet >= MAX_RAISES){
            error.setText("The maximum of " + MAX_RAISES + " raises has been reached this round.");
            return false;
        }
        if(amount > room){
            error.setText("You can raise by at most $" + Math.max(room, 0) + ".");
            return false;
        }
        if (amount < MIN_RAISE && amount != room){
            error.setText("Minimum raise is $" + MIN_RAISE + " (or all in).");
            return false;
        }
        error.setText("");
        raiseField.setText("");
        doRaise(0, amount);
        return true;
    }

    private void aiTurn(int seat){
        Player p = playerList.get(seat);
        List<String> known = new ArrayList<>();
        known.add(p.getCard(0));
        known.add(p.getCard(1));
        known.addAll(board.getCards(visibleBoardCount()));
        int rank = HandEvaluator.evaluate(known).get(0);
        int toCall = amountToCall(p);
        int stack = p.getChips();
        if(toCall > stack){
            if(rank >=HandEvaluator.TWO_PAIR){
                doCall(seat);
            }
            else{
                doFold(seat);
            }
            return;
        }

        if(rank<=HandEvaluator.ONE_PAIR && toCall > stack / 3){
            doFold(seat);
            return;
        }
        boolean shouldRaise = false;
        if(raisesThisStreet < MAX_RAISES && !acted[seat]){
            double roll = Math.random();
            if(street==0){
                shouldRaise = roll < preflopRaiseChance(p);
            }
            else if (rank >=HandEvaluator.THREE_OF_A_KIND){
                shouldRaise = true;
            }
            else if (rank == HandEvaluator.TWO_PAIR) {
                shouldRaise = roll < 0.4;
            }
        }
        int room = stack - toCall;
        if (shouldRaise && room > 0){
            int raise = Math.min(Math.max(MIN_RAISE, stack/10), room);
            doRaise(seat, raise);
        }
        else{
            doCall(seat);
        }
    }

    private double preflopRaiseChance(Player p){
        int r1 = HandEvaluator.rankToIndex(p.getCard(0).substring(0,p.getCard(0).indexOf('-')));
        int r2 = HandEvaluator.rankToIndex(p.getCard(1).substring(0,p.getCard(1).indexOf('-')));
        if (r1==r2){
            return 0.5;
        }
        if(r1 >= 8 && r2 >= 8){
            return 0.3;
        }
        return 0.0;
    }

    private void awardByFold(){
        int winner = -1;
        for (int i = 0; i<numPlayers;i++){
            if(playerList.get(i).isInHand()) winner = i;
        }
        Player p = playerList.get(winner);
        p.addChips(potAmount);
        p.setStatusOverride("WINNER");
        String result = who(winner)+" " + verb(winner,"win")+" $"+potAmount+" (everyone else folded).";
        finishHand(result);
    }

    private void showdown(){
        List<String> boardCards = board.getCards(5);
        int[] contributions = new int[numPlayers];
        boolean[] live = new boolean[numPlayers];
        List<List<Integer>> scores = new ArrayList<>();
        for(int i = 0;i<numPlayers;i++){
            Player p = playerList.get(i);
            contributions[i] = p.getTotalContributed();
            live[i] = p.isInHand();
            if(live[i]){
                List<String> all = new ArrayList<>(boardCards);
                all.add(p.getCard(0));
                all.add(p.getCard(1));
                scores.add(HandEvaluator.evaluate(all));
            }
            else{
                scores.add(null);
            }
        }
        int[] winnings = Pots.distribute(contributions, live, scores, first);
        StringBuilder result = new StringBuilder();
        for(int i=0;i<numPlayers;i++){
            if(winnings[i]>0){
                playerList.get(i).addChips(winnings[i]);
                playerList.get(i).setStatusOverride("WINNER");
                if(result.length()>0){
                    result.append("; ");
                }
                result.append(who(i)).append(" ").append(verb(i, "win")).append(" $").append(winnings[i])
                    .append(" with ").append(HandEvaluator.handName(scores.get(i)));
            }
        }
        result.append(".");
        for(int i=1;i<numPlayers;i++){
            if(playerList.get(i).isInHand()){
                playerList.get(i).showCards();
            }
        }
        street = 3;
        finishHand(result.toString());
    }

    private void finishHand(String result){
        handOver = true;
        potAmount = 0;
        int withChips = 0;
        for(Player p : playerList){
            if(p.getChips()>0){
                withChips++;
            }
        }
        if(playerList.get(0).getChips()==0){
            gameOver = true;
            result += " You are out of chips - game over";
        }
        else if (withChips==1) {
            gameOver = true;
            result += " Everyone else is out of chips - you win the game";
        }
        say(result);
        refreshDisplay();
    }

    private int visibleBoardCount(){
        if(handOver){
            return 5;
        }
        return street == 0 ? 0 : street + 2;
    }

    private void refreshDisplay(){
        Player human = playerList.get(0);
        pot.setText("Pot: $" + potAmount);
        betNeeded.setText("Your call: $" + amountToCall(human));
        setChips(chips);
        setPlayerChips(chipss);
        for(int i =1;i<playerList.size();i++){
            playerList.get(i).setTotalLabel();
            playerList.get(i).setFoldedLabel();
        }
        midCards.removeAll();
        if(handStarted){
            int visible = visibleBoardCount();
            for(int i =0;i<5;i++){
                midCards.add(new JLabel(i<visible ? Images.card(board.getCard(i)) : Images.cardBack()));
            }
        }
        if(handStarted && !handOver){
            turnLabel.setText("Turn: Player " + (turn + 1));
        }
        else{
            turnLabel.setText("");
        }
        boolean humanTurn = humanCanAct();
        int toCall = amountToCall(human);
        fold.setEnabled(humanTurn);
        call.setEnabled(humanTurn);
        raise.setEnabled(humanTurn && !acted[0] && raisesThisStreet < MAX_RAISES);
        if(toCall == 0){
            call.setText("Check");
        }
        else if(toCall >= human.getChips()){
            call.setText("Call $" + human.getChips() + " (all in)");
        }
        else{
            call.setText("Call $" + toCall);
        }
        next.setEnabled(handStarted && !handOver && !humanTurn);
        newGame.setEnabled(handOver);
        newGame.setText(gameOver ? "Play Again" : "New Game");
        panel2.revalidate();
        panel2.repaint();
    }

    private void say(String text){
        message.setText(text);
    }

    private String who(int seat){
        return seat == 0 ? "You" : "Player " + (seat + 1);
    }

    private String verb(int seat, String base){
        return seat == 0 ? base : base + "s";
    }
    
    private void setChips(JPanel panel){
        panel.removeAll();
        int temp = potAmount;
        while(temp>=25){
            panel.add(new JLabel(Images.chip("Black")));
            temp -=25;
        }
        while(temp >= 5){
            panel.add(new JLabel(Images.chip("Red")));
            temp -=5;
        }
        while(temp > 0){
            panel.add(new JLabel(Images.chip("Green")));
            temp--;
        }
    }
}
