package cards;

import Game.Player;

/**
 * Represents the "Princess" card in the game.
 * If the Princess card is discarded for any reason, the player who discarded it is immediately knocked out
 * of the round. If the Princess was discarded by another card's effect, any remaining effects of that card do not apply.
 * However, effects related to being knocked out of the round still apply (e.g., Constable, Jester).
 */
public class Princess extends Cards {

    /**
     * Constructs a Princess card with a predefined name, value, and effect description.
     */
    public Princess() {
        super("Princess", 8, "If you discard the Princess—no matter how or why—she has tossed your letter into the fire. " +
                             "You are immediately knocked out of the round. If the Princess was discarded by a card effect, " +
                             "any remaining effects of that card do not apply (you do not draw a card from the Prince, for example). " +
                             "Effects tied to being knocked out of the round still apply (e.g., Constable, Jester), however.");
    }

    /**
     * Applies the effect of the Princess card.
     * The player who discards the Princess is immediately knocked out of the round.
     * This applies regardless of the reason for discarding the Princess card.
     *
     * @param target the player discarding the Princess card, who will be knocked out of the round.
     */
    public void applyEffect(Player target) {
        target.isKnockedOutOfRound = true;
        target.connectionHandler.broadcastToAll(target.nickname + " discarded the Princess and is knocked out of the round.");
        target.revealCard();
    }
}
