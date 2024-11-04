package cards;

/**
 * Represents the "Countess" card in the game.
 * The effect of the Countess card is unique in that it applies while in the player's hand.
 * If the player has both the Countess and either the King or Prince, they must discard the Countess.
 * The effect does not apply upon discarding.
 */
public class Countess extends Cards {

    /**
     * Constructs a Countess card with a predefined name, value, and effect description.
     */
    public Countess() {
        super("Countess", 7, "Unlike other cards, which take effect when discarded, the text on the Countess applies " +
                             "while she is in your hand. In fact, the only time it doesn’t apply is when you discard her. " +
                             "If you ever have the Countess and either the King or Prince in your hand, you must discard the Countess. " +
                             "You do not have to reveal the other card in your hand. Of course, you can also discard the Countess even " +
                             "if you do not have a royal family member in your hand. The Countess likes to play mind games.");
    }

}
