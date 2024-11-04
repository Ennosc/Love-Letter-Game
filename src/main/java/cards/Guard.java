package cards;

import Game.Player;

/**
 * Represents the "Guard" card in the game.
 * When discarded, the Guard allows the player to choose another player and guess a card's value.
 * If the guessed value matches the value of the chosen player's card, the chosen player is knocked out of the round.
 * This effect does not apply if the chosen player is immune or if the guessed value does not match.
 */
public class Guard extends Cards {

    /**
     * Constructs a Guard card with a predefined name, value, and effect description.
     */
    public Guard() {
        super("Guard", 1, "Choose a player and name a number (other than 1). If that player has that number in their hand, " +
                          "that player is knocked out of the round. If all other players still in the round cannot be chosen " +
                          "(e.g., due to Handmaid), this card is discarded without effect.");
    }

    /**
     * Applies the effect of the Guard card.
     * Allows the player to choose a target and guess a card value. If the target player has a card with the guessed value,
     * the target player is knocked out of the round. If the target player is immune or the guess is incorrect, the effect is nullified.
     *
     * @param number the guessed card value, which should be a number other than 1.
     * @param target the target player whose hand will be checked for the guessed value.
     */
    public void applyEffect(int number, Player target) {
        if (target.isImmune) {
            target.connectionHandler.broadcastToAll(target.nickname + " is immune. The card is discarded without effect.");
            return;
        }
        target.connectionHandler.broadcastToAll(target.nickname + " is being targeted");
        for (Cards card : target.hand) {
            if (card.getValue() == number) {
                target.connectionHandler.broadcastToAll(target.nickname + " has the number " + number + " on his hand" +
                                                        ". " +
                                                        target.nickname + " is knocked out of the round.");
                target.isKnockedOutOfRound = true;
                target.revealCard();
                return;
            }
        }
        target.connectionHandler.broadcastToAll(target.nickname + " does not have the number " + number + " on his " +
                                                "hand. The Guard card is discarded without effect.");

    }
}
