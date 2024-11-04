package Game;
import ServerClient.*;
import cards.*;

import java.util.ArrayList;

/**
 * Represents a player in the game, managing their hand, tokens, immunity, and other states.
 * The Player class provides methods for interacting with the game's mechanics, such as drawing,
 * discarding, and playing cards, as well as managing knock-out and immunity statuses.
 */
public class Player {
    public String nickname;
    public boolean isPlaying = false;
    public boolean isActive = true;
    public boolean isMyTurn = false;
    public boolean isKnockedOutOfRound = false;
    public int lastDate;
    public long age;
    public int numberOfTokens = 0;
    public boolean isImmune = false;
    public Server.ConnectionHandler connectionHandler;
    public ArrayList<Cards> hand = new ArrayList<>();
    public ArrayList<String> discardedCards = new ArrayList<>();
    public int discardedCardsValue = 0;
    public int turnOrder;
    public String cardToPlay;
    public boolean highestCardValue = false;
    public boolean highestDiscardedCardValue = false;
    public boolean hasWonGame = false;

    /**
     * Constructs a new Player with a given nickname and connection handler.
     *
     * @param nickname the nickname of the player
     * @param connectionHandler the connection handler associated with this player
     */
    public Player(String nickname, Server.ConnectionHandler connectionHandler) {
        this.nickname = nickname;
        this.connectionHandler = connectionHandler;
    }

    /**
     * Retrieves the player's hand as a formatted string.
     *
     * @return the player's hand in a string format
     */
    public String getHand(){
        StringBuilder handList = new StringBuilder(5);
        for(Cards c : hand){
            handList.append(c.getName()).append(" | ");
        }
        return handList.toString();
    }

    /**
     * Draws the first card and adds it to the player's hand, notifying the player.
     *
     * @param card the card to add to the player's hand
     */
    public void drawFirstCard(Cards card){
        hand.add(card);
        //server @ u

        connectionHandler.sendMessage("Your drew the card: " + card.getName());
        connectionHandler.sendMessage("Your hand: | " + getHand());
        //connectionHandler.sendMessage("---------------------------------------------------------------------");
    }

    /**
     * Draws a card, handles special conditions for the Countess card, and sets the player’s turn.
     * Removes immunity status for the current player.
     *
     * @param card the card to add to the player's hand
     */
    public void drawCard(Cards card) {
        isImmune = false;
        connectionHandler.server.game.immunePlayersList.remove(this);
        drawFirstCard(card);

        connectionHandler.sendMessage("The following players are knocked out of the Round: " + getKnockedOutOfRoundPlayers());
        connectionHandler.sendMessage("The following players are immune: " + getImmunePlayers());
        //connectionHandler.sendMessage(" knockedoutlist list"+connectionHandler.server.game.knockedOutPlayersSet
        // .toString());
        //connectionHandler.sendMessage(" immune list"+connectionHandler.server.game.immunePlayersList.toString());

        if (checkForCountessAndRoyal()) {
            return;
        }

        connectionHandler.sendMessage("Which card do you want to play? \n"
                                      + "Type '/play <cardName>' to play a card. For example /play Guard ");
        isMyTurn = true;
    }

    /**
     * Checks if the player's hand contains both the Countess and either the King or Prince.
     * If this condition is met, the Countess is automatically played, and the turn ends.
     *
     * @return true if the Countess was played automatically; false otherwise.
     */
    public boolean checkForCountessAndRoyal(){
        boolean hasCountess = false;
        boolean hasKingOrPrince = false;

        for (Cards c : hand) {
            if (c.getName().equals("Countess")) {
                hasCountess = true;
            }
            if (c.getName().equals("King") || c.getName().equals("Prince")) {
                hasKingOrPrince = true;
            }
        }
        if (hasCountess && hasKingOrPrince ){
            connectionHandler.sendMessage("Your hand contains the Countess and either a King or Prince. The Countess " +
                                          "is played.");

            playCardCountess();
            connectionHandler.server.game.playerFinishedTurn();
            connectionHandler.server.game.isCountessPlayed = true;
            return true;
        }
        return false;
    }

    /**
     * Discards a specified card from the player's hand and adds it to the discarded cards list.
     * Informs all players of the discarded card.
     *
     * @param card the card to discard
     */
    public void discardCard(Cards card){
        hand.remove(card);
        discardedCards.add(card.getName());
        discardedCardsValue += card.getValue();
        //server @ all
        connectionHandler.broadcastToAll(nickname + " has played & discarded " + card.getName());
    }

    /**
     * Forces the player to discard a specified card and adds it to the discarded cards list.
     * Informs all players of the discarded card.
     *
     * @param card the card to discard
     */
    public void forceDiscardCard(Cards card){
        hand.remove(card);
        discardedCards.add(card.getName());
        discardedCardsValue += card.getValue();
        //server @ all
        connectionHandler.broadcastToAll(nickname + " has discarded " + card.getName());
    }

    /**
     * Reveals the player's card to all players by discarding it.
     */
    public void revealCard(){
        Cards c = hand.remove(0);
        discardedCards.add(c.getName());
        connectionHandler.broadcastToAll(nickname + " discardedCards are " + discardedCards.toString());

    }

    /**
     * Marks the player as knocked out of the round and removes their current card if they have one.
     *
     * @return a string representing the discarded cards of the player
     */
    public String gotKnockedOutOfRound() {
        isKnockedOutOfRound = true;
        if (!hand.isEmpty()) {
            discardedCards.add(hand.remove(0).getName());         }
        return discardedCards.toString();
    }

    /**
     * Plays the Guard card by applying its effect to the target player.
     *
     * @param number the number guessed by the Guard card
     * @param target the target player
     */
    public void playCardGuard(int number, Player target) {
        Cards cardToPlay = findCardInHand("Guard");
        discardCard(cardToPlay);
        ((Guard)cardToPlay).applyEffect(number, target);
    }

    /**
     * Plays the Priest card by applying its effect to the target player.
     *
     * @param target the target player
     */
    public void playCardPriest(Player target) {
        Cards cardToPlay = findCardInHand("Priest");
        discardCard(cardToPlay);
        ((Priest)cardToPlay).applyEffect(target, this);
    }

    /**
     * Plays the Baron card by applying its effect to the target player.
     *
     * @param target the target player
     */
    public void playCardBaron(Player target) {
        Cards cardToPlay = findCardInHand("Baron");
        discardCard(cardToPlay);
        ((Baron)cardToPlay).applyEffect(target, this);
    }

    /**
     * Plays the Handmaid card by applying its effect to make the player immune.
     */
    public void playCardHandmaid() {
        Cards cardToPlay = findCardInHand("Handmaid");
        discardCard(cardToPlay);
        ((Handmaid)cardToPlay).applyEffect(this);
    }

    /**
     * Plays the Prince card by applying its effect to the target player and drawing a new card.
     *
     * @param target the target player
     * @param deck the deck from which to draw a new card
     */
    public void playCardPrince(Player target, Deck deck) {
        Cards cardToPlay = findCardInHand("Prince");
        discardCard(cardToPlay);
        ((Prince)cardToPlay).applyEffect(target, deck);
    }

    /**
     * Plays the King card by swapping cards with the target player.
     *
     * @param target the target player
     */
    public void playCardKing(Player target) {
        Cards cardToPlay = findCardInHand("King");
        discardCard(cardToPlay);
        ((King)cardToPlay).applyEffect(target, this);
    }

    /**
     * Plays the Countess card by discarding it.
     */
    public void playCardCountess() {
        Cards cardToPlay = findCardInHand("Countess");
        discardCard(cardToPlay);
    }

    /**
     * Plays the Princess card, causing the player to be knocked out if discarded.
     */
    public void playCardPrincess() {
        Cards cardToPlay = findCardInHand("Princess");
        discardCard(cardToPlay);
        ((Princess)cardToPlay).applyEffect(this);
    }

    /**
     * Helper method to find a card in the player's hand by its name.
     *
     * @param cardName the name of the card to find
     * @return the card with the specified name, or null if not found
     */
    private Cards findCardInHand(String cardName) {
        for (Cards card : hand) {
            if (card.getName().equals(cardName)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Plays a specified card by discarding it without applying its effect.
     *
     * @param cardName the name of the card to play
     */
    public void playCardAllImmune(String c){
        Cards cardToPlay = null;
        for(Cards card : hand) {
            if(card.getName().equals(c)){
                cardToPlay = card;
                break;
            }
        }
        discardCard(cardToPlay);
    }

    /**
     * Retrieves a list of all players who are currently immune.
     *
     * @return a formatted string listing immune players
     */
    public String getImmunePlayers(){
        StringBuilder stringBuilder = new StringBuilder(100);
        connectionHandler.server.game.turnOrderMap.forEach( (key, value) -> {
            if(value.isImmune){
                stringBuilder.append(value.nickname).append(" | ");
                connectionHandler.server.game.immunePlayersList.add(value);
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Retrieves a list of all players who have been knocked out of the current round.
     *
     * @return a formatted string listing knocked-out players
     */
    public String getKnockedOutOfRoundPlayers(){
        StringBuilder stringBuilder = new StringBuilder(100);
        connectionHandler.server.game.turnOrderMap.forEach((key, value) -> {
            if (value.isKnockedOutOfRound) {
                value.isImmune = false;
                connectionHandler.server.game.knockedOutPlayersSet.add(value);
            }
        });
        connectionHandler.server.game.knockedOutPlayersSet.forEach( (player -> stringBuilder.append(player.nickname).append(" | ")));
        return stringBuilder.toString();
    }
}

