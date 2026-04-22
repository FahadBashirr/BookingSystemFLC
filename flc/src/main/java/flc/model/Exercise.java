package flc.model;

/**
 * Represents the types of group exercise lessons offered at Furzefield Leisure Centre.
 * Each exercise type has a fixed price regardless of time slot.
 */
public enum Exercise {
    YOGA("Yoga", 12.00),
    ZUMBA("Zumba", 10.00),
    AQUACISE("Aquacise", 9.50),
    BOX_FIT("Box Fit", 11.00),
    BODY_BLITZ("Body Blitz", 10.50);

    private final String displayName;
    private final double price;

    Exercise(String displayName, double price) {
        this.displayName = displayName;
        this.price = price;
    }

    public String getDisplayName() { return displayName; }
    public double getPrice() { return price; }

    @Override
    public String toString() { return displayName; }
}
