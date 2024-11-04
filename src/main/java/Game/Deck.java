package Game;

import cards.*;

import java.util.Collections;
import java.util.ArrayList;

/**
 * Represents the deck of cards used in the game.
 * The deck is initialized with a predefined set of cards, which includes specific numbers and types.
 * It provides functionality to shuffle the deck, remove cards, draw cards, and check if the deck is empty.
 */
public class Deck {
    public ArrayList<Cards> deckOfCards = new ArrayList<>();
    public int numOfCards;
    public String[] removedThreeCards = new String[3];
    public ArrayList<Cards> removedCards = new ArrayList<>();
    public Cards removedFirstCard;

    /**
     * Constructs a Deck object, initializing it with the full set of cards needed for the game.
     * It also sets the initial number of cards in the deck.
     */
    public Deck() {
        deckOfCards.add(new Princess());
        deckOfCards.add(new Countess());
        deckOfCards.add(new King());
        deckOfCards.add(new Prince());
        deckOfCards.add(new Prince());
        deckOfCards.add(new Handmaid());
        deckOfCards.add(new Handmaid());
        deckOfCards.add(new Baron());
        deckOfCards.add(new Baron());
        deckOfCards.add(new Priest());
        deckOfCards.add(new Priest());
        deckOfCards.add(new Guard());
        deckOfCards.add(new Guard());
        deckOfCards.add(new Guard());
        deckOfCards.add(new Guard());
        deckOfCards.add(new Guard());
        numOfCards = deckOfCards.size();
    }

    /**
     * Shuffles the deck of cards to randomize the order.
     */
    public void shuffleDeck() {
        Collections.shuffle(deckOfCards);
    }

    /**
     * Removes the first card from the deck and sets it aside without revealing it to players.
     */
    public void removeOneCard(){
        removedFirstCard = deckOfCards.remove(0);
        numOfCards -= 1;
    }

    /**
     * Removes three cards from the deck, sets them aside, and records their names.
     * This method is used only when there are two players in the game.
     */
    public void removeThreeCards(){
        for(int i = 0; i < 3; i++){
            Cards card = deckOfCards.remove(0);
            removedThreeCards[i] = card.getName();
            numOfCards -= 1;
        }
    }

    /**
     * Returns a string representation of the three cards that were removed from the deck.
     *
     * @return the names of the three removed cards, formatted as a string.
     */
    public String showThreeRemovedCards(){
        return  "| " + removedThreeCards[0] + " | " + removedThreeCards[1] + " | " + removedThreeCards[2] + " |";
    }

    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck is empty, false otherwise.
     */
    public boolean isEmpty(){
        return deckOfCards.isEmpty();
    }

    /**
     * Draws the first card from the deck and removes it.
     *
     * @return the drawn card.
     */
    public Cards drawCard(){
        Cards card = deckOfCards.remove(0);
        numOfCards -= 1;
        return card;
    }

    /**
     * Prints the current contents of the deck to the console, mainly for debugging purposes.
     */
    public void showDeck(){
        System.out.println("deck is ::");
        for(Cards c : deckOfCards){
            System.out.println(c.getName());
        }
        System.out.println("end of deck");
    }
}
