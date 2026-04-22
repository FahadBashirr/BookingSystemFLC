package flc.model;

/**
 * Represents the three time slots available for lessons each day.
 */
public enum TimeSlot {
    MORNING("Morning", "09:00 - 10:00"),
    AFTERNOON("Afternoon", "13:00 - 14:00"),
    EVENING("Evening", "18:00 - 19:00");

    private final String displayName;
    private final String timeRange;

    TimeSlot(String displayName, String timeRange) {
        this.displayName = displayName;
        this.timeRange = timeRange;
    }

    public String getDisplayName() { return displayName; }
    public String getTimeRange() { return timeRange; }

    @Override
    public String toString() { return displayName + " (" + timeRange + ")"; }
}
