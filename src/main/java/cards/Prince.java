package cards;

import Game.Deck;
import Game.Player;

/**
 * Represents the "Prince" card in the game.
 * When discarded, the Prince card allows the player to choose another player to discard their hand
 * and draw a new card. If the chosen player discards the Princess card, they are immediately knocked out
 * of the round. If the deck is empty, the player draws the card that was removed at the start of the round.
 * If all other players are protected by the Handmaid, the player must choose themselves as the target.
 */
public class Prince extends Cards {

    /**
     * Constructs a Prince card with a predefined name, value, and effect description.
     */
    public Prince() {
        super("Prince", 5, "When you discard Prince Arnaud, choose one player still in the round (including " +
                           "yourself). That player discards his or her hand (but doesn’t apply its effect, unless it is the Princess) and draws a new one. " +
                           "If the deck is empty and the player cannot draw a card, that player draws the card that was removed at the start of the round. " +
                           "If all other players are protected by the Handmaid, you must choose yourself.");
    }

    /**
     * Applies the effect of the Prince card.
     * The target player discards their current hand and draws a new card. If the discarded card is the Princess,
     * the target player is knocked out of the round. If the deck is empty, the player draws the previously removed
     * card. If the target player is immune, the card is discarded without effect.
     *
     * @param target the player who will discard and draw a new card.
     * @param deck   the deck from which the new card will be drawn.
     */
    public void applyEffect(Player target, Deck deck) {
        if (target.isImmune) {
            target.connectionHandler.broadcastToAll(target.nickname + " is immune. The card is discarded without effect");
            return;
        }
        target.connectionHandler.broadcastToAll(target.nickname + " is being targeted");
        Cards card = target.hand.get(0);
        if (card.getName().equals("Princess")) {
            target.isKnockedOutOfRound = true;
            target.connectionHandler.broadcastToAll(target.nickname + " discarded the Princess and is knocked out of the round.");
            target.revealCard();
            return;
        }

        target.forceDiscardCard(card);

        Cards newCard;
        if (!deck.isEmpty()) {
            newCard = deck.drawCard();
            target.hand.add(newCard);
        } else {
            newCard = deck.removedFirstCard;
            target.hand.add(newCard);
        }

        target.connectionHandler.sendMessage("You have drawn the card: " + newCard.getName());
        target.connectionHandler.sendMessage("Your hand: " + target.hand.get(0).getName());
    }
}
