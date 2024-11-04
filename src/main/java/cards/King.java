package cards;

import Game.Player;

/**
 * Represents the "King" card in the game.
 * When discarded, the player trades their card with another player of their choice,
 * unless that player is immune or has been knocked out of the round.
 */
public class King extends Cards {

    /**
     * Constructs a King card with a predefined name, value, and effect description.
     */
    public King() {
        super("King", 6, "When you discard King, trade the card in your hand with the card held by another player " +
                         "of your choice.\nYou cannot trade with a player who is out of the round.");
    }

    /**
     * Applies the effect of the King card.
     * Trades the card in the hand of the player discarding the King with the card of the specified target player.
     * If the target player is immune, the effect is discarded without any action.
     *
     * @param target   the player whose card will be traded.
     * @param attacker the player discarding the King card.
     */
    public void applyEffect(Player target, Player attacker) {
        if (target.isImmune) {
            target.connectionHandler.broadcastToAll(target.nickname + " is immune. The card is discarded without effect");
            return;
        }

        target.connectionHandler.broadcastToAll(target.nickname + " is being targeted");
        Cards cardTarget = target.hand.remove(0);
        Cards cardAttacker = attacker.hand.remove(0);

        attacker.hand.add(cardTarget);
        target.hand.add(cardAttacker);

        target.connectionHandler.sendMessage("Your new hand after the trade is: " + target.hand.get(0).getName());
        attacker.connectionHandler.sendMessage("Your new hand after the trade is: " + attacker.hand.get(0).getName());
    }
}
