package flc.service;

import flc.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Core service layer handling all booking operations for the FLC system.
 * No external database is used; all state is held in memory.
 */
public class BookingService {

    // ── Book a lesson ─────────────────────────────────────────────────────────

    /**
     * Books a lesson for a member.
     *
     * @return the created Booking
     * @throws IllegalStateException    if the lesson is full
     * @throws IllegalArgumentException if the member has a time conflict
     */
    public Booking bookLesson(Member member, Lesson lesson) {
        if (!lesson.hasSpace()) {
            throw new IllegalStateException(
                    "Lesson is fully booked: " + lesson.getExercise().getDisplayName());
        }
        if (member.hasTimeConflict(lesson)) {
            throw new IllegalArgumentException(
                    "Time conflict: " + member.getName() + " already has a booking at this time.");
        }

        Booking booking = new Booking(member, lesson);
        lesson.addBooking(booking);
        member.addBooking(booking);
        return booking;
    }

    // ── Change a booking ──────────────────────────────────────────────────────

    /**
     * Changes an existing booking to a new lesson.
     * The old lesson slot is freed; the new lesson must have space and no time conflict.
     *
     * @return the updated Booking
     * @throws IllegalStateException    if the new lesson is full
     * @throws IllegalArgumentException if the new lesson causes a time conflict
     */
    public Booking changeBooking(Booking booking, Lesson newLesson) {
        Member member   = booking.getMember();
        Lesson oldLesson = booking.getLesson();

        if (!newLesson.hasSpace()) {
            throw new IllegalStateException(
                    "Cannot change booking: new lesson is fully booked.");
        }

        // Temporarily remove from old lesson so conflict check doesn't self-conflict
        oldLesson.removeBooking(booking);
        member.removeBooking(booking);

        if (member.hasTimeConflict(newLesson)) {
            // Roll back
            oldLesson.addBooking(booking);
            member.addBooking(booking);
            throw new IllegalArgumentException(
                    "Cannot change booking: time conflict with another booking.");
        }

        booking.setLesson(newLesson);
        newLesson.addBooking(booking);
        member.addBooking(booking);
        return booking;
    }

    // ── Cancel a booking ──────────────────────────────────────────────────────

    /**
     * Cancels an existing booking entirely.
     */
    public void cancelBooking(Booking booking) {
        booking.getLesson().removeBooking(booking);
        booking.getMember().removeBooking(booking);
    }

    // ── Add a review ──────────────────────────────────────────────────────────

    /**
     * Adds a review for a lesson by a member.
     * The member must have attended (booked) the lesson.
     *
     * @throws IllegalArgumentException if the member did not book this lesson
     */
    public Review addReview(Member member, Lesson lesson, int rating, String comment) {
        boolean attended = lesson.getBookings().stream()
                .anyMatch(b -> b.getMember().equals(member));
        if (!attended) {
            throw new IllegalArgumentException(
                    member.getName() + " did not attend this lesson and cannot review it.");
        }
        Review review = new Review(member, lesson, rating, comment);
        lesson.addReview(review);
        return review;
    }

    // ── Timetable queries ─────────────────────────────────────────────────────

    /**
     * Returns all lessons for a specific day across all weekends.
     */
    public List<Lesson> getLessonsByDay(List<Lesson> allLessons, Day day) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : allLessons) {
            if (l.getDay() == day) result.add(l);
        }
        return result;
    }

    /**
     * Returns all lessons for a specific exercise type across all weekends.
     */
    public List<Lesson> getLessonsByExercise(List<Lesson> allLessons, Exercise exercise) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : allLessons) {
            if (l.getExercise() == exercise) result.add(l);
        }
        return result;
    }

    /**
     * Returns all lessons for a specific weekend number.
     */
    public List<Lesson> getLessonsByWeekend(List<Lesson> allLessons, int weekendNumber) {
        List<Lesson> result = new ArrayList<>();
        for (Lesson l : allLessons) {
            if (l.getWeekendNumber() == weekendNumber) result.add(l);
        }
        return result;
    }
}
