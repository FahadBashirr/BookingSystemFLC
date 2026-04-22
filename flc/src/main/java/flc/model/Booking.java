package flc.model;

/**
 * Represents a booking made by a member for a specific lesson.
 */
public class Booking {
    private static int counter = 1;

    private final String bookingId;
    private final Member member;
    private Lesson lesson;

    public Booking(Member member, Lesson lesson) {
        this.bookingId = "BK" + String.format("%03d", counter++);
        this.member = member;
        this.lesson = lesson;
    }

    public String getBookingId() { return bookingId; }
    public Member getMember()    { return member; }
    public Lesson getLesson()    { return lesson; }

    /** Used when changing a booking to a new lesson. */
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    // For test resets
    public static void resetCounter() { counter = 1; }

    @Override
    public String toString() {
        return bookingId + ": " + member.getName() + " → " + lesson;
    }
}
