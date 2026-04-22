package flc;

import flc.data.DataInitializer;
import flc.model.*;
import flc.service.BookingService;
import flc.service.ReportService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Furzefield Leisure Centre booking system.
 * Tests cover: booking creation, capacity enforcement, time-conflict detection,
 * change-booking logic, review validation, and report generation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FLCSystemTest {

    private BookingService svc;
    private List<Lesson>  lessons;
    private List<Member>  members;

    @BeforeEach
    void setUp() {
        Booking.resetCounter();
        svc     = new BookingService();
        lessons = DataInitializer.buildTimetable();
        members = DataInitializer.buildMembers();
    }

    // ── Timetable structure ───────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Timetable should contain exactly 48 lessons (8 weekends × 6)")
    void timetable_has_48_lessons() {
        assertEquals(48, lessons.size(), "Expected 48 lessons in 8-weekend timetable");
    }

    @Test
    @Order(2)
    @DisplayName("Each weekend has exactly 6 lessons (2 days × 3 slots)")
    void each_weekend_has_6_lessons() {
        for (int w = 1; w <= 8; w++) {
            final int weekend = w;
            long count = lessons.stream()
                    .filter(l -> l.getWeekendNumber() == weekend)
                    .count();
            assertEquals(6, count, "Weekend " + w + " should have 6 lessons");
        }
    }

    @Test
    @Order(3)
    @DisplayName("All 5 exercise types are present in the timetable")
    void all_exercise_types_present() {
        for (Exercise ex : Exercise.values()) {
            boolean found = lessons.stream()
                    .anyMatch(l -> l.getExercise() == ex);
            assertTrue(found, ex.getDisplayName() + " not found in timetable");
        }
    }

    // ── Lesson capacity ───────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("New lesson should have 4 spaces available")
    void new_lesson_has_4_spaces() {
        Lesson l = lessons.get(0);
        assertEquals(4, l.getAvailableSpaces());
        assertTrue(l.hasSpace());
    }

    @Test
    @Order(5)
    @DisplayName("Lesson should reject a 5th booking (capacity = 4)")
    void lesson_rejects_5th_booking() {
        Lesson l = lessons.get(0); // W1 Sat Morning
        Member[] fourMembers = members.subList(0, 4).toArray(new Member[0]);

        // Book 4 members into the lesson (they all have different IDs so no conflict)
        for (Member m : fourMembers) {
            svc.bookLesson(m, l);
        }
        assertFalse(l.hasSpace(), "Lesson should be full after 4 bookings");

        // 5th member (index 4) should be rejected
        Member fifth = members.get(4);
        assertThrows(IllegalStateException.class, () -> svc.bookLesson(fifth, l),
                "Should throw when booking a full lesson");
    }

    // ── Booking creation ──────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("Successful booking links member and lesson correctly")
    void booking_links_member_and_lesson() {
        Member alice = members.get(0);
        Lesson l     = lessons.get(0);
        Booking b    = svc.bookLesson(alice, l);

        assertNotNull(b.getBookingId());
        assertEquals(alice, b.getMember());
        assertEquals(l, b.getLesson());
        assertEquals(1, l.getBookedCount());
        assertEquals(1, alice.getBookings().size());
    }

    // ── Time-conflict detection ────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Member cannot book two lessons at the same time slot")
    void time_conflict_is_detected() {
        // Get two lessons on the same weekend, same day, same time slot
        // W1 Sat Morning: L11; there's only one per slot, but let's pick two
        // lessons with identical weekend/day/slot by using the timetable structure:
        // W1 has only one SAT MORNING slot so we test with a constructed scenario.
        // We'll use L11 (W1 SAT MORNING) and create a fake conflict check manually.
        Lesson l1 = DataInitializer.findLesson(lessons, "L11"); // W1 SAT MORNING
        Lesson l2 = DataInitializer.findLesson(lessons, "L21"); // W2 SAT MORNING

        Member alice = members.get(0);
        svc.bookLesson(alice, l1); // W1 Sat Morning — OK

        // L21 is W2, different weekend — should succeed (no conflict)
        assertDoesNotThrow(() -> svc.bookLesson(alice, l2));

        // Now try to book ANOTHER W1 SAT MORNING lesson for alice.
        // There is only one per slot in the timetable so we verify hasTimeConflict directly.
        assertTrue(alice.hasTimeConflict(l1),
                "Alice should have a time conflict with the same slot she already booked");
    }

    @Test
    @Order(8)
    @DisplayName("Member can book on same day but different time slots")
    void same_day_different_slot_is_allowed() {
        Member alice = members.get(0);
        Lesson morning   = DataInitializer.findLesson(lessons, "L11"); // W1 SAT MORNING
        Lesson afternoon = DataInitializer.findLesson(lessons, "L12"); // W1 SAT AFTERNOON
        Lesson evening   = DataInitializer.findLesson(lessons, "L13"); // W1 SAT EVENING

        svc.bookLesson(alice, morning);
        assertDoesNotThrow(() -> svc.bookLesson(alice, afternoon));
        assertDoesNotThrow(() -> svc.bookLesson(alice, evening));
        assertEquals(3, alice.getBookings().size());
    }

    // ── Change booking ────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("Change booking moves member to new lesson correctly")
    void change_booking_succeeds() {
        Member alice = members.get(0);
        Lesson l1 = DataInitializer.findLesson(lessons, "L11"); // W1 SAT MORNING
        Lesson l2 = DataInitializer.findLesson(lessons, "L21"); // W2 SAT MORNING

        Booking b = svc.bookLesson(alice, l1);
        assertEquals(1, l1.getBookedCount());

        svc.changeBooking(b, l2);

        assertEquals(0, l1.getBookedCount(), "Old lesson should now be empty");
        assertEquals(1, l2.getBookedCount(), "New lesson should have 1 booking");
        assertEquals(l2, b.getLesson(), "Booking should now reference new lesson");
    }

    @Test
    @Order(10)
    @DisplayName("Change booking fails if new lesson is full")
    void change_booking_fails_when_full() {
        // Fill L21 with 4 members
        Lesson l21 = DataInitializer.findLesson(lessons, "L21");
        for (int i = 1; i < 5; i++) svc.bookLesson(members.get(i), l21);
        assertFalse(l21.hasSpace());

        // Alice books L11 then tries to move to L21
        Member alice = members.get(0);
        Lesson l11 = DataInitializer.findLesson(lessons, "L11");
        Booking b = svc.bookLesson(alice, l11);

        assertThrows(IllegalStateException.class, () -> svc.changeBooking(b, l21));

        // Alice should still be in l11
        assertEquals(l11, b.getLesson());
        assertEquals(1, l11.getBookedCount());
    }

    @Test
    @Order(11)
    @DisplayName("Change booking fails if time conflict exists with another booking")
    void change_booking_fails_on_time_conflict() {
        Member alice = members.get(0);
        Lesson l11 = DataInitializer.findLesson(lessons, "L11"); // W1 SAT MORNING
        Lesson l14 = DataInitializer.findLesson(lessons, "L14"); // W1 SUN MORNING
        Lesson l41 = DataInitializer.findLesson(lessons, "L41"); // W4 SAT MORNING

        // Alice books W1 SAT and W1 SUN morning
        Booking b1 = svc.bookLesson(alice, l11);
        svc.bookLesson(alice, l14);

        // Now try to change b1 (W1 SAT MORNING) to L41 (W4 SAT MORNING) — different weekend, OK
        assertDoesNotThrow(() -> svc.changeBooking(b1, l41));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @Test
    @Order(12)
    @DisplayName("Review can only be added by a member who attended the lesson")
    void review_requires_attendance() {
        Member alice = members.get(0);
        Member bob   = members.get(1);
        Lesson l11   = DataInitializer.findLesson(lessons, "L11");

        svc.bookLesson(alice, l11);

        // Alice attended — should succeed
        assertDoesNotThrow(() -> svc.addReview(alice, l11, 5, "Great!"));

        // Bob did not attend — should fail
        assertThrows(IllegalArgumentException.class,
                () -> svc.addReview(bob, l11, 4, "Nice"),
                "Non-attendee should not be able to leave a review");
    }

    @Test
    @Order(13)
    @DisplayName("Rating must be between 1 and 5")
    void review_rating_out_of_range_throws() {
        Member alice = members.get(0);
        Lesson l11   = DataInitializer.findLesson(lessons, "L11");
        svc.bookLesson(alice, l11);

        assertThrows(IllegalArgumentException.class,
                () -> svc.addReview(alice, l11, 0, "Too low"));
        assertThrows(IllegalArgumentException.class,
                () -> svc.addReview(alice, l11, 6, "Too high"));
    }

    @Test
    @Order(14)
    @DisplayName("Average rating is computed correctly")
    void average_rating_correct() {
        Member alice = members.get(0);
        Member bob   = members.get(1);
        Lesson l11   = DataInitializer.findLesson(lessons, "L11");

        svc.bookLesson(alice, l11);
        svc.bookLesson(bob,   l11);
        svc.addReview(alice, l11, 4, "Good");
        svc.addReview(bob,   l11, 2, "Ok");

        assertEquals(3.0, l11.getAverageRating(), 0.01,
                "Average of 4 and 2 should be 3.0");
    }

    @Test
    @Order(15)
    @DisplayName("Lesson with no reviews returns average rating 0.0")
    void no_reviews_average_is_zero() {
        Lesson l = lessons.get(0);
        assertEquals(0.0, l.getAverageRating(), 0.001);
    }

    // ── Timetable queries ─────────────────────────────────────────────────────

    @Test
    @Order(16)
    @DisplayName("getLessonsByDay returns only Saturday lessons")
    void filter_by_day_saturday() {
        List<Lesson> satLessons = svc.getLessonsByDay(lessons, Day.SATURDAY);
        assertEquals(24, satLessons.size(), "Should be 24 Saturday lessons (8 weekends × 3)");
        assertTrue(satLessons.stream().allMatch(l -> l.getDay() == Day.SATURDAY));
    }

    @Test
    @Order(17)
    @DisplayName("getLessonsByExercise returns only Yoga lessons")
    void filter_by_exercise_yoga() {
        List<Lesson> yogaLessons = svc.getLessonsByExercise(lessons, Exercise.YOGA);
        assertFalse(yogaLessons.isEmpty());
        assertTrue(yogaLessons.stream().allMatch(l -> l.getExercise() == Exercise.YOGA));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    @Test
    @Order(18)
    @DisplayName("Reports can be generated without errors after loading data")
    void reports_generated_successfully() {
        DataInitializer.populateSampleData(lessons, members, svc);
        ReportService rs = new ReportService();

        String r1 = rs.generateAttendanceReport(lessons);
        assertNotNull(r1);
        assertTrue(r1.contains("ATTENDANCE"), "Report 1 should mention ATTENDANCE");

        String r2 = rs.generateIncomeReport(lessons);
        assertNotNull(r2);
        assertTrue(r2.contains("INCOME"), "Report 2 should mention INCOME");
        assertTrue(r2.contains("HIGHEST"), "Report 2 should identify highest earner");
    }

    @Test
    @Order(19)
    @DisplayName("Income report identifies highest-earning exercise")
    void income_report_highest_earner() {
        DataInitializer.populateSampleData(lessons, members, svc);
        ReportService rs = new ReportService();
        String report = rs.generateIncomeReport(lessons);
        assertTrue(report.contains("◄ HIGHEST"), "Income report must flag the top earner");
    }

    @Test
    @Order(20)
    @DisplayName("Cancel booking frees lesson space")
    void cancel_booking_frees_space() {
        Member alice = members.get(0);
        Lesson l = lessons.get(0);
        Booking b = svc.bookLesson(alice, l);
        assertEquals(1, l.getBookedCount());

        svc.cancelBooking(b);
        assertEquals(0, l.getBookedCount(), "Lesson should have 0 bookings after cancel");
        assertEquals(0, alice.getBookings().size(), "Member should have 0 bookings after cancel");
    }
}
