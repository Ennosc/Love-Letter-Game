package Game;

import ServerClient.Server;
import cards.Cards;

import java.util.*;


/**
 * Represents the main game logic for the card game. The Game class handles the initialization,
 * turn management, round play, scoring, and winner determination. It manages the communication
 * between players and ensures that each round and the game overall follows the rules.
 */
public class Game implements Runnable {
    public Server server;
    public Deck deck;
    public int tokensToWin;
    public int maxTokens;
    public final int roundNumber = 1;
    private int whosTurn = 0;
    private String firstPlayerName;
    public Map<Integer, Player> turnOrderMap = new HashMap<>();
    public HashSet<Player> knockedOutPlayersSet = new HashSet<>();
    public HashSet<Player> immunePlayersList = new HashSet<>();
    public boolean isCountessPlayed = false;

    /**
     * Initializes a new Game instance with the provided server.
     *
     * @param server the server that hosts the game
     */
    public Game(Server server){
        this.server = server;
    }

    /**
     * Runs the main game loop, checking for the winning condition based on tokens and
     * playing rounds until the game is complete.
     */
    public void run(){
        game();

    }

    /**
     * Main game logic to initialize token requirements, determine player order,
     * and manage rounds until the win condition is met.
     */
    public void game(){
        if(server.playerMap.size() == 2){
            tokensToWin = 7; //7
        }else if(server.playerMap.size() == 3){
            tokensToWin = 5; //5
        }else{
            tokensToWin = 4; // 4
        }
        orderOfLastDate();
        turnOrderMap.forEach( (key, value) -> {
            System.out.println("key" + key + " value" + value);
            System.out.println("key" + key + " value" + value.nickname);
        });
        System.out.println(turnOrderMap.size() + " Turn Order Map Size");
        server.connectionsMap.get(firstPlayerName).broadcastToAll(firstPlayerName + " starts the round");
        while(tokensToWin != maxTokens){
            playRound();
        }
        gameWinner();
        resetGameStats();
    }

    /**
     * Initializes the round by resetting player stats, shuffling the deck,
     * and dealing cards to players.
     */
    public void initializeRound(){
        tokenInfo();

        whosTurn = 0;
        System.out.println("initializeRound: whosTurn ist " + whosTurn);
        deck = new Deck();
        System.out.println("new deck");
        resetPlayerStats();
        deck.showDeck();
        deck.shuffleDeck();
        deck.showDeck();
        deck.removeOneCard();
        deck.showDeck();
        if(server.playerMap.size() == 2){
            deck.removeThreeCards();
            server.connectionsMap.values().iterator().next().broadcastToAll("The 3 removed cards are: " +
                                                                            deck.showThreeRemovedCards() + " | Type " +
                                                                            "/3cards to see them again.");
        }
        turnOrderMap.forEach((key,val)->{
            Cards c = deck.drawCard();
            val.drawFirstCard(c);
        });
        deck.showDeck();
        System.out.println("turnOrderMap " + turnOrderMap);
    }

    /**
     * Manages the sequence of turns in a round until one player remains or the deck is empty.
     * Determines the winner if the round ends.
     */
    public synchronized void playRound(){
        server.connectionsMap.get(firstPlayerName).broadcastToAll("--------------------------------------------");
        System.out.println("playRound: whosTurn startet bei " + whosTurn);
        initializeRound();
        while(lastManStanding() > 1){
            while (turnOrderMap.get(whosTurn).isKnockedOutOfRound){
                whosTurn = (whosTurn + 1) % turnOrderMap.size();
            }
            server.connectionsMap.get(firstPlayerName).broadcastToAll(
                    "------------------------------------------------");
            server.connectionsMap.get(firstPlayerName).broadcastToAll("Now it is " + turnOrderMap.get(whosTurn).nickname +
                                                                      "'s turn." );
            server.connectionsMap.get(firstPlayerName).broadcastToAll(
                    "------------------------------------------------");
            turnOrderMap.get(whosTurn).drawCard(deck.drawCard());
            try{
                System.out.println("watit");
                if(!isCountessPlayed){
                    wait();
                }
                isCountessPlayed = false;
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
            System.out.println("after wait");
            if(deck.isEmpty()){
                break;
            }
            whosTurn = (whosTurn + 1) % turnOrderMap.size();
        }
        if(deck.isEmpty()){
            System.out.println("deck is empty round winner chcked ealsf");
            roundWinnerDeck();
            return;
        }
        roundWinner();
    }

    /**
     * Notifies the game that the current player has finished their turn.
     */
    public synchronized void playerFinishedTurn(){
        System.out.println("playerFinishedTurn() aufgerufen: Notify wird jetzt ausgeführt");
        notify();
    }
    /**
     * Counts players still in the round who are not knocked out.
     *
     * @return the count of players still in the round.
     */
    public int lastManStanding(){
        int lastManStand = 0;
       for(Player p : turnOrderMap.values()){
           if(p.isKnockedOutOfRound == false){
               lastManStand += 1;
           }
       }
       return lastManStand;
    }

    /**
     * Determines the winner of a round and updates their token count.
     */
    public void roundWinner(){
        //only on left with isknoked out == false
        Player pl = null;
        for(Player p : turnOrderMap.values()){
            if(p.isKnockedOutOfRound == false){
                pl = p;
            }
        }
        pl.numberOfTokens += 1;
        server.connectionsMap.get(pl.nickname).broadcastToAll(pl.nickname + " won the round and has now " + pl.numberOfTokens + " tokens.");
        for(Player p : turnOrderMap.values()){
            maxTokens = Math.max(maxTokens, p.numberOfTokens);
        }
        System.out.println(maxTokens + " maxTokesn");

        updateOrderOfPlay(pl);
    }

    /**
     * Determines the winner of a round if the deck is empty, comparing card values.
     */
    public void roundWinnerDeck(){
        compareValueOfCards();
    }

    /**
     * Determines the winner of the game based on tokens.
     */
    public void gameWinner(){
        server.connectionsMap.get(firstPlayerName).broadcastToAll("GAME OVER");
        int numberOfWinners = 0;
        for(Player p: turnOrderMap.values()){
            if(p.numberOfTokens == tokensToWin){
                numberOfWinners += 1;
                p.hasWonGame = true;
            }
        }
        System.out.println("game Winner method");
        System.out.println("number of Winners: " + numberOfWinners);
        if(numberOfWinners == 1){
            for(Player p: turnOrderMap.values()){
                if(p.hasWonGame){
                    server.connectionsMap.get(p.nickname).broadcastToAll(p.nickname + " has won the game.");
                    break;
                }
            }

        }else{
            StringBuilder stringBuilder = new StringBuilder(100);
            stringBuilder.append("|");
            for(Player p : turnOrderMap.values()){
                if(p.hasWonGame){
                    stringBuilder.append(p.nickname).append( " | ");
                }
            }
            server.connectionsMap.get(firstPlayerName).broadcastToAll(stringBuilder.toString() + "have won the game.");
        }

    }

    /**
     * Compares the discarded card values of players to determine the winner when there is a tie.
     * The player with the highest discarded card value wins the round.
     */
    public void compareDiscardedCardsValue(){
        int max = 0;
        for(Player p : turnOrderMap.values()){
            if(p.highestCardValue){
                max = Math.max(max, p.discardedCardsValue);
            }
        }
        int howManyHaveHighestDiscardedCardsValue = 0;
        for(Player p : turnOrderMap.values()){
            if(p.discardedCardsValue == max){
                howManyHaveHighestDiscardedCardsValue += 1;
                p.highestDiscardedCardValue = true;
                server.connectionsMap.get(p.nickname).broadcastToAll(p.nickname + " has the highest discarded Cards " +
                                                                     "value " + max);
            }

        }
        if(howManyHaveHighestDiscardedCardsValue > 1){
            StringBuilder stringBuilder = new StringBuilder(20);
            for(Player p : turnOrderMap.values()){
                if(p.highestDiscardedCardValue){
                    p.numberOfTokens += 1;
                    stringBuilder.append(p.nickname).append(" # ofTokens = ").append(p.numberOfTokens).append(" | ");

                }

            }
            server.connectionsMap.get(firstPlayerName).broadcastToAll("It's a tie again. The following players win " +
                                                                      "the round: " + stringBuilder.toString());
            Player pla = youngestPlayer();
            for(Player p : turnOrderMap.values()){
                maxTokens = Math.max(maxTokens, p.numberOfTokens);
            }
            updateOrderOfPlay(pla);
        }else{
            Player player = null;
            for(Player p : turnOrderMap.values()){
                if(p.highestDiscardedCardValue){
                    player = p;
                    break;
                }
            }
            player.numberOfTokens += 1;
            server.connectionsMap.get(player.nickname).broadcastToAll(player.nickname + " won the round and has now " + player.numberOfTokens + " tokens.");

            for(Player p : turnOrderMap.values()){
                maxTokens = Math.max(maxTokens, p.numberOfTokens);
            }
            updateOrderOfPlay(player);
        }
    }
    /**
     * Compares the values of players' current cards to determine the round winner when the deck is empty.
     * If there is a tie in card values, compares discarded card values to break the tie.
     */
    public void compareValueOfCards(){
        server.connectionsMap.get(firstPlayerName).broadcastToAll("-----------------------------------------");

        server.connectionsMap.get(firstPlayerName).broadcastToAll("The Deck is empty. Checking for a winner");
        int max = 0;
        for(Player p : turnOrderMap.values()){
            if(p.isKnockedOutOfRound == false && !p.hand.isEmpty()){
                max = Math.max(max, p.hand.get(0).getValue());
            }
        }
        int howManyHaveHighestValueCard = 0;
        for(Player p : turnOrderMap.values()){
            if(p.isKnockedOutOfRound == false && !p.hand.isEmpty()){
                if(p.hand.get(0).getValue() == max){
                    p.highestCardValue = true;
                    howManyHaveHighestValueCard += 1;
                    server.connectionsMap.get(p.nickname).broadcastToAll(p.nickname + " has the highest card value of " + max);
                }
            }

        }
        if(howManyHaveHighestValueCard > 1){
            server.connectionsMap.get(firstPlayerName).broadcastToAll("It's a tie. Highest Value of discarded Cards " +
                                                                      "will be checked");
            compareDiscardedCardsValue();
        }else{
            Player player = null;
            for(Player p : turnOrderMap.values()){
                if(p.highestCardValue){
                    player = p;
                    break;
                }
            }
            player.numberOfTokens += 1;
            for(Player p : turnOrderMap.values()){
                maxTokens = Math.max(maxTokens, p.numberOfTokens);
            }
            updateOrderOfPlay(player);
        }

    }

    /**
     * Sorts players by the time of their last date, and if tied, by their age to determine the order of play.
     * Sets the first player name and updates the turn order map.
     */
    private void orderOfLastDate(){
        List<Player> players = new ArrayList<>(server.playerMap.values());
        // Sort players based on lastDate (ascending), then age (ascending)
        players.sort((p1, p2) -> {
            if (p1.lastDate != p2.lastDate) {
                return Integer.compare(p1.lastDate, p2.lastDate); // Smaller lastDate first (more recent date)
            } else {
                return Long.compare(p1.age, p2.age); // Younger player first if dates are equal
            }
        });
        for(int i = 0; i < players.size(); i++){
            Player p = players.get(i);
            p.turnOrder = i;
            if(i == 0){
                firstPlayerName = p.nickname;
            }
            turnOrderMap.put(i, p);
            server.connectionsMap.get(p.nickname).broadcastToAll(p.nickname + " turn order is " + p.turnOrder);
        }
    }

    /**
     * Determines the youngest player among those with the highest discarded card value in case of a tie.
     *
     * @return the youngest player with the highest discarded card value.
     */
    private Player youngestPlayer(){
        Player player = null;
        long min = Long.MAX_VALUE;
        for(Player p : turnOrderMap.values()){
            if(p.highestDiscardedCardValue){
                min = Math.min(min, p.age);
            }

        }
        for(Player p : turnOrderMap.values()){
            if(p.age == min){
                player = p;
            }
        }
        return player;
    }

    /**
     * Updates the turn order for the next round based on the player who won the last round.
     * Resets the turn order map and assigns new turn orders.
     *
     * @param player the player who won the last round and will start the next round.
     */
    private void updateOrderOfPlay(Player player){
        System.out.println("old turnordermap");
        turnOrderMap.forEach( (key, value) -> {
            System.out.println("key" + key + " value" + value);
            System.out.println("key" + key + " value" + value.nickname);
        });
        int numberToAdd = player.turnOrder;
        System.out.println(numberToAdd + " numberTo Add");

        List<Player> newOrder = new ArrayList<>();
        for(int i = 0; i<turnOrderMap.size(); i++){
            int newIdx = (i + numberToAdd) % turnOrderMap.size();
            newOrder.add(turnOrderMap.get(newIdx));
        }
        turnOrderMap.clear();
        for(int i = 0; i<newOrder.size(); i++){
            Player p = newOrder.get(i);
            p.turnOrder = i;
            turnOrderMap.put(i, p);
        }
        whosTurn = 0;
        System.out.println("whos turn = " + whosTurn);
        System.out.println("new turnordermap");
        turnOrderMap.forEach( (key, value) -> {
            System.out.println("key" + key + " value" + value);
            System.out.println("key" + key + " value" + value.nickname);
        });
        server.connectionsMap.get(player.nickname).broadcastToAll("---------------------------------------------");
        if(tokensToWin == maxTokens){
            return;
        }
        server.connectionsMap.get(player.nickname).broadcastToAll("The new TurnOrder is: ");
        for(int i = 0; i<turnOrderMap.size(); i++){
            Player p = turnOrderMap.get(i);
            server.connectionsMap.get(p.nickname).broadcastToAll(p.nickname + " turn order is " + p.turnOrder);
        }
        System.out.println(player.nickname + " starts the next round");
        server.connectionsMap.get(player.nickname).broadcastToAll(player.nickname + " starts the next round");
    }

    /**
     * Resets player statistics at the end of a round.
     */
    public void resetPlayerStats(){
        for(Player p : turnOrderMap.values()){
            p.isMyTurn = false;
            p.isKnockedOutOfRound = false;
            p.isImmune = false;
            p.hand = new ArrayList<>();
            p.discardedCards = new ArrayList<>();
            p.discardedCardsValue = 0;
            p.highestCardValue = false;
            p.highestDiscardedCardValue = false;
            p.hasWonGame = false;
        }
        knockedOutPlayersSet = new HashSet<>();
        immunePlayersList = new HashSet<>();
        isCountessPlayed = false;
    }

    /**
     * Resets the game statistics, allowing a new game to be started.
     */
    public void resetGameStats(){
        server.connectionsMap.get(firstPlayerName).broadcastToAll("-----------------------------------");
        server.connectionsMap.get(firstPlayerName).broadcastToAll("Type /create to play another game.");
        server.isGameCreated = false;
        server.isGameRunning = false;
        server.playerCounter = 0;
        server.dateCounter = 0;
        server.birthdayCounter = 0;
        server.playerMap = Collections.synchronizedMap(new HashMap<>());
        server.connectionsMap.forEach( (key, value) -> {
            value.hasJoinedGame = false;
        });
        isCountessPlayed = false;
    }

    /**
     * Broadcasts the token information for each player to all players.
     */
    public void tokenInfo(){
        server.connectionsMap.get(firstPlayerName).broadcastToAll(tokensToWin + " tokens are required to win!");
        for(Player p : turnOrderMap.values()){
            server.connectionsMap.get(p.nickname).broadcastToAll(p.nickname + " has currently " + p.numberOfTokens +
                                                                 " tokens");
        }
        server.connectionsMap.get(firstPlayerName).broadcastToAll(
                "------------------------------------------------");
    }
}
