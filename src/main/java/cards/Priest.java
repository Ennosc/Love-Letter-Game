package cards;

import Game.Player;

/**
 * Represents the "Priest" card in the game.
 * When discarded, the Priest allows the player to secretly view another player's hand.
 * The viewed hand should not be revealed to any other players.
 */
public class Priest extends Cards {

    /**
     * Constructs a Priest card with a predefined name, value, and effect description.
     */
    public Priest() {
        super("Priest", 2, "When you discard the Priest, you can look at another player’s hand. Do not reveal the " +
                           "hand to any other players.");
    }

    /**
     * Applies the effect of the Priest card.
     * Allows the player discarding the Priest to look at the target player's hand.
     * If the target player is immune, the effect is discarded without action.
     *
     * @param target   the player whose hand will be viewed.
     * @param attacker the player discarding the Priest card.
     */
    public void applyEffect(Player target, Player attacker) {
        if (target.isImmune) {
            target.connectionHandler.broadcastToAll(target.nickname + " is immune. The card is discarded without effect.");
            return;
        }
        target.connectionHandler.broadcastToAll(target.nickname + " is being targeted");
        String handList = target.getHand();
        attacker.connectionHandler.broadcastToAll(attacker.nickname + " has looked at " + target.nickname + "'s hand.");
        attacker.connectionHandler.sendMessage("You viewed " + target.nickname + "'s hand, which contains: " + handList);
        System.out.println("Applying Priest effect to " + target.nickname);
    }
}
