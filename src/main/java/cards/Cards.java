package cards;

/**
 * Represents a card in the game with a name, value, and effect.
 * This class is intended to be extended by specific card types
 * that implement the actual effects in their `applyEffect` method.
 */
public abstract class Cards {
    private String name;
    private int value;
    private String effect;

    /**
     * Constructs a card with the specified name, value, and effect description.
     *
     * @param name   the name of the card
     * @param value  the numerical value of the card, used for comparison in game rules
     * @param effect a description of the card's effect
     */
    public Cards(String name, int value, String effect) {
        this.name = name;
        this.value = value;
        this.effect = effect;
    }

    /**
     * Gets the name of the card.
     *
     * @return the name of the card
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the card.
     *
     * @return the value of the card
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the effect description of the card.
     *
     * @return a description of the card's effect
     */
    public String getEffect() {
        return effect;
    }

    /**
     * Applies the card's effect. This method is intended to be overridden
     * by subclasses to provide specific behaviors.
     */
    public void applyEffect() {
        // Intentionally left empty. To be implemented by subclasses.
    }
}
