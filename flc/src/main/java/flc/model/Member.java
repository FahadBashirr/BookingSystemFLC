package flc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a member of Furzefield Leisure Centre.
 * A member can book multiple lessons as long as there are no time conflicts.
 */
public class Member {
    private final String memberId;
    private final String name;
    private final List<Booking> bookings;

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
        this.bookings = new ArrayList<>();
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public List<Booking> getBookings() { return new ArrayList<>(bookings); }

    /** Adds a booking to this member's list. Called by BookingService only. */
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    /** Removes a booking from this member's list. Called by BookingService only. */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
    }

    /**
     * Checks whether this member already has a booking that conflicts with the given lesson
     * (same weekend number, same day, same time slot).
     */
    public boolean hasTimeConflict(Lesson lesson) {
        for (Booking b : bookings) {
            Lesson booked = b.getLesson();
            if (booked.getWeekendNumber() == lesson.getWeekendNumber()
                    && booked.getDay() == lesson.getDay()
                    && booked.getTimeSlot() == lesson.getTimeSlot()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the booking for the given lesson, or null if not booked.
     */
    public Booking getBookingForLesson(Lesson lesson) {
        for (Booking b : bookings) {
            if (b.getLesson().equals(lesson)) return b;
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " (" + memberId + ")";
    }
}
