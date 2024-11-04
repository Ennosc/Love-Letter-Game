import Game.Player;
import Game.*;
import ServerClient.Server;
import cards.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import java.util.List;

class PlayerTest {

    private Player player;
    private Server.ConnectionHandler mockHandler;

    @BeforeEach
    void setUp() {
        mockHandler = mock(Server.ConnectionHandler.class);
        player = new Player("TestPlayer", mockHandler);
    }

    @Test
    void testDrawCard(){
        Deck deck = new Deck();
        deck.shuffleDeck();
        player.drawCard(deck.drawCard());
        assertEquals(1, player.hand.size());
        System.out.println(player.hand.toString());
        // Capturing the argument sent to sendMessage method
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockHandler, times(3)).sendMessage(messageCaptor.capture());

        List<String> allMessages = messageCaptor.getAllValues();
        System.out.println(allMessages); // Gibt alle Nachrichten aus

    }
    @Test
    void testDiscardCard() {
        Cards card = new Guard();
        player.drawCard(card);
        player.discardCard(card);
        assertEquals(0, player.hand.size());
        assertEquals(1, player.discardedCards.size());
        verify(mockHandler, times(1)).broadcastToAll("TestPlayer has discarded Guard1");
    }

    @Test
    void testGotKnockedOutOfRound() {
        Cards card = new Guard();
        player.drawCard(card);
        String discarded = player.gotKnockedOutOfRound();
        assertTrue(player.isKnockedOutOfRound);
        System.out.println("Player Knocked Out Status: " + player.isKnockedOutOfRound);
        System.out.println(discarded);
        assertTrue(discarded.contains("Guard1"));
        assertEquals(1, player.discardedCards.size());
    }
}