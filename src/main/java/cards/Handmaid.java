package cards;

import Game.Player;

/**
 * Represents the "Handmaid" card in the game.
 * When discarded, the Handmaid grants the player immunity from other players' card effects until the start of their next turn.
 * If all other players are protected by the Handmaid, the current player must choose themselves if possible.
 */
public class Handmaid extends Cards {

    /**
     * Constructs a Handmaid card with a predefined name, value, and effect description.
     */
    public Handmaid() {
        super("Handmaid", 4, "When you discard the Handmaid, you are immune to the effects of other players' cards " +
                             "until the start of your next turn. If all players other than the player whose turn it is " +
                             "are protected by the Handmaid, the player must choose themselves for a card’s effects, if possible.");
    }

    /**
     * Applies the effect of the Handmaid card.
     * Grants the current player immunity from all other players' card effects until the start of their next turn.
     *
     * @param currentPlayer the player discarding the Handmaid card, who will become immune.
     */
    public void applyEffect(Player currentPlayer) {
        currentPlayer.isImmune = true;
        currentPlayer.connectionHandler.broadcastToAll(currentPlayer.nickname + " is now immune for the round.");
    }
}
