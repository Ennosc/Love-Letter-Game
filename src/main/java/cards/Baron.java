package cards;

import Game.Player;

/**
 * Represents the Baron card in the game. When discarded, the Baron card allows the player
 * to choose another player still in the round and compare their card values. The player
 * with the lower card value is knocked out of the round. If the values are equal, nothing happens.
 */
public class Baron extends Cards {

    /**
     * Creates a new Baron card with predefined name, rank, and description.
     */
    public Baron(){
        super("Baron", 3, "When you discard the Baron, choose another player still in the round. You and that player" +
                           " " +
                          "secretly\n" +
                          "compare your hands. The player with the lower number is knocked out of the round. In case of a\n" +
                          "tie, nothing happens");
    }

    /**
     * Applies the effect of the Baron card by comparing the card values of the attacker
     * and the target player. The player with the lower card value is knocked out of the round.
     *
     * @param target   the player targeted by the Baron card effect
     * @param attacker the player who played the Baron card
     */
    public void applyEffect(Player target, Player attacker) {
        if(target.isImmune){
            target.connectionHandler.broadcastToAll(target.nickname + " is immune. The card is discarded without " +
                                                    "effect");
            return;
        }
        target.connectionHandler.broadcastToAll(target.nickname + " is being targeted");
        int cardTarget = target.hand.get(0).getValue();
        int cardAttacker = attacker.hand.get(0).getValue();

        if (cardTarget == cardAttacker){
            target.connectionHandler.sendMessage("It's a tie. Nothing happens");
            attacker.connectionHandler.sendMessage("It's a tie. Nothing happens");
        }else if (cardTarget < cardAttacker){
            target.connectionHandler.broadcastToAll(target.nickname + " is knocked out of the round");
            target.isKnockedOutOfRound = true;
            target.revealCard();
        }else {//cardTarget > cardAttacker
            target.connectionHandler.sendMessage("Your card has a higher value than " + attacker.nickname);
            attacker.connectionHandler.sendMessage("Your card has a lower value than " + target.nickname);
            target.connectionHandler.broadcastToAll(attacker.nickname + " is knocked out of the round");
            attacker.isKnockedOutOfRound = true;
            attacker.revealCard();
        }
    }
}
