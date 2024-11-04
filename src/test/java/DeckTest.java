import Game.Deck;
import org.junit.jupiter.api.Test;
import cards.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void testDeckInitialization(){
        Deck deck = new Deck();
        assertEquals(16, deck.numOfCards);
        assertFalse(deck.isEmpty());
    }
    @Test
    void testShuffleDeck(){
        Deck deck = new Deck();
        ArrayList<Cards> beforeShuffle = new ArrayList<>(deck.deckOfCards);
        deck.shuffleDeck();
        assertNotEquals(beforeShuffle, deck.deckOfCards);
    }
    @Test
    void testRemoveOneCard(){
        Deck deck = new Deck();
        deck.removeOneCard();
        System.out.println(deck.deckOfCards.size());
        assertEquals(15, deck.numOfCards);
        assertEquals(15, deck.deckOfCards.size());
    }
    @Test
    void testRemoveThreeCards(){
        Deck deck = new Deck();
        deck.shuffleDeck();
        for(int i = 0; i < deck.numOfCards; i++){
            System.out.println(deck.deckOfCards.get(i));
        }

        deck.removeThreeCards();
        System.out.println(deck.deckOfCards.size());
        assertEquals(13, deck.numOfCards);
        assertEquals(13, deck.deckOfCards.size());
        for(int i = 0; i < deck.numOfCards; i++){
            System.out.println(deck.deckOfCards.get(i));
        }
        System.out.println(deck.showThreeRemovedCards());
    }

    @Test
    void testIsEmpty(){
        Deck deck = new Deck();
        while(!deck.isEmpty()){
            deck.removeOneCard();
        }
        assertTrue(deck.isEmpty());
    }
}