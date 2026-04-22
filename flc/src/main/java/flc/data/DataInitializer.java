package flc.data;

import flc.model.*;
import flc.service.BookingService;

import java.util.ArrayList;
import java.util.List;

/**
 * Initialises all timetable data (8 weekends × 2 days × 3 slots = 48 lessons),
 * 10 members, sample bookings, and 20+ reviews.
 * This class is the single source of truth for demo/test data.
 */
public class DataInitializer {

    // ── Weekend layouts: each weekend has a specific exercise per slot ────────
    // Layout: [SAT_MORNING, SAT_AFTERNOON, SAT_EVENING, SUN_MORNING, SUN_AFTERNOON, SUN_EVENING]
    private static final Exercise[][] WEEKEND_LAYOUTS = {
        // W1
        { Exercise.YOGA,      Exercise.ZUMBA,      Exercise.BOX_FIT,
          Exercise.AQUACISE,  Exercise.BODY_BLITZ,  Exercise.YOGA      },
        // W2
        { Exercise.ZUMBA,     Exercise.AQUACISE,   Exercise.YOGA,
          Exercise.BOX_FIT,   Exercise.YOGA,        Exercise.ZUMBA     },
        // W3
        { Exercise.BOX_FIT,   Exercise.BODY_BLITZ, Exercise.ZUMBA,
          Exercise.YOGA,      Exercise.AQUACISE,    Exercise.BOX_FIT   },
        // W4
        { Exercise.AQUACISE,  Exercise.YOGA,       Exercise.BODY_BLITZ,
          Exercise.ZUMBA,     Exercise.BOX_FIT,     Exercise.AQUACISE  },
        // W5
        { Exercise.BODY_BLITZ, Exercise.BOX_FIT,   Exercise.AQUACISE,
          Exercise.BODY_BLITZ, Exercise.ZUMBA,      Exercise.YOGA      },
        // W6
        { Exercise.YOGA,      Exercise.AQUACISE,   Exercise.ZUMBA,
          Exercise.BOX_FIT,   Exercise.BODY_BLITZ,  Exercise.AQUACISE  },
        // W7
        { Exercise.ZUMBA,     Exercise.BODY_BLITZ, Exercise.YOGA,
          Exercise.AQUACISE,  Exercise.ZUMBA,       Exercise.BOX_FIT   },
        // W8
        { Exercise.BOX_FIT,   Exercise.YOGA,       Exercise.BODY_BLITZ,
          Exercise.YOGA,      Exercise.AQUACISE,    Exercise.ZUMBA     },
    };

    /**
     * Builds and returns all 48 lessons across 8 weekends.
     */
    public static List<Lesson> buildTimetable() {
        List<Lesson> lessons = new ArrayList<>();
        Day[] days     = { Day.SATURDAY, Day.SATURDAY, Day.SATURDAY,
                           Day.SUNDAY,   Day.SUNDAY,   Day.SUNDAY };
        TimeSlot[] slots = { TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING,
                             TimeSlot.MORNING, TimeSlot.AFTERNOON, TimeSlot.EVENING };

        for (int w = 0; w < 8; w++) {
            for (int i = 0; i < 6; i++) {
                String id = String.format("L%d%d", w + 1, i + 1);
                lessons.add(new Lesson(id, WEEKEND_LAYOUTS[w][i], days[i], slots[i], w + 1));
            }
        }
        return lessons;
    }

    /**
     * Creates 10 members.
     */
    public static List<Member> buildMembers() {
        List<Member> members = new ArrayList<>();
        members.add(new Member("M001", "Alice Johnson"));
        members.add(new Member("M002", "Bob Smith"));
        members.add(new Member("M003", "Carol Williams"));
        members.add(new Member("M004", "David Brown"));
        members.add(new Member("M005", "Emma Davis"));
        members.add(new Member("M006", "Frank Miller"));
        members.add(new Member("M007", "Grace Wilson"));
        members.add(new Member("M008", "Henry Moore"));
        members.add(new Member("M009", "Isla Taylor"));
        members.add(new Member("M010", "Jack Anderson"));
        return members;
    }

    /**
     * Populates bookings and reviews into the supplied timetable.
     * Requires at least 48 lessons and 10 members.
     */
    public static void populateSampleData(List<Lesson> lessons,
                                          List<Member> members,
                                          BookingService svc) {
        // Helper – find lesson by ID
        // Lesson IDs: L{weekend}{slot_index}  e.g. L11=W1 SAT MORNING, L12=W1 SAT AFTERNOON …

        // ── BOOKINGS ──────────────────────────────────────────────────────────
        // Weekend 1
        book(svc, members, lessons, "M001", "L11");  // Alice  – W1 SAT MORNING  (Yoga)
        book(svc, members, lessons, "M002", "L11");  // Bob    – W1 SAT MORNING  (Yoga)
        book(svc, members, lessons, "M003", "L11");  // Carol  – W1 SAT MORNING  (Yoga)
        book(svc, members, lessons, "M004", "L11");  // David  – W1 SAT MORNING  (Yoga)
        book(svc, members, lessons, "M001", "L12");  // Alice  – W1 SAT AFTERNOON (Zumba)
        book(svc, members, lessons, "M005", "L12");  // Emma   – W1 SAT AFTERNOON (Zumba)
        book(svc, members, lessons, "M002", "L13");  // Bob    – W1 SAT EVENING  (Box Fit)
        book(svc, members, lessons, "M006", "L13");  // Frank  – W1 SAT EVENING  (Box Fit)
        book(svc, members, lessons, "M007", "L14");  // Grace  – W1 SUN MORNING  (Aquacise)
        book(svc, members, lessons, "M008", "L14");  // Henry  – W1 SUN MORNING  (Aquacise)
        book(svc, members, lessons, "M009", "L15");  // Isla   – W1 SUN AFTERNOON (Body Blitz)
        book(svc, members, lessons, "M010", "L15");  // Jack   – W1 SUN AFTERNOON (Body Blitz)

        // Weekend 2
        book(svc, members, lessons, "M001", "L21");  // Alice  – W2 SAT MORNING  (Zumba)
        book(svc, members, lessons, "M003", "L21");  // Carol  – W2 SAT MORNING  (Zumba)
        book(svc, members, lessons, "M005", "L21");  // Emma   – W2 SAT MORNING  (Zumba)
        book(svc, members, lessons, "M002", "L22");  // Bob    – W2 SAT AFTERNOON (Aquacise)
        book(svc, members, lessons, "M004", "L22");  // David  – W2 SAT AFTERNOON (Aquacise)
        book(svc, members, lessons, "M006", "L23");  // Frank  – W2 SAT EVENING  (Yoga)
        book(svc, members, lessons, "M007", "L24");  // Grace  – W2 SUN MORNING  (Box Fit)
        book(svc, members, lessons, "M008", "L24");  // Henry  – W2 SUN MORNING  (Box Fit)
        book(svc, members, lessons, "M009", "L24");  // Isla   – W2 SUN MORNING  (Box Fit)

        // Weekend 3
        book(svc, members, lessons, "M001", "L31");  // Alice  – W3 SAT MORNING  (Box Fit)
        book(svc, members, lessons, "M010", "L31");  // Jack   – W3 SAT MORNING  (Box Fit)
        book(svc, members, lessons, "M003", "L32");  // Carol  – W3 SAT AFTERNOON (Body Blitz)
        book(svc, members, lessons, "M005", "L32");  // Emma   – W3 SAT AFTERNOON (Body Blitz)
        book(svc, members, lessons, "M002", "L33");  // Bob    – W3 SAT EVENING  (Zumba)
        book(svc, members, lessons, "M004", "L33");  // David  – W3 SAT EVENING  (Zumba)
        book(svc, members, lessons, "M006", "L34");  // Frank  – W3 SUN MORNING  (Yoga)
        book(svc, members, lessons, "M007", "L35");  // Grace  – W3 SUN AFTERNOON (Aquacise)
        book(svc, members, lessons, "M008", "L35");  // Henry  – W3 SUN AFTERNOON (Aquacise)

        // Weekend 4
        book(svc, members, lessons, "M009", "L41");  // Isla   – W4 SAT MORNING  (Aquacise)
        book(svc, members, lessons, "M010", "L41");  // Jack   – W4 SAT MORNING  (Aquacise)
        book(svc, members, lessons, "M001", "L42");  // Alice  – W4 SAT AFTERNOON (Yoga)
        book(svc, members, lessons, "M002", "L42");  // Bob    – W4 SAT AFTERNOON (Yoga)
        book(svc, members, lessons, "M003", "L43");  // Carol  – W4 SAT EVENING  (Body Blitz)
        book(svc, members, lessons, "M004", "L44");  // David  – W4 SUN MORNING  (Zumba)
        book(svc, members, lessons, "M005", "L45");  // Emma   – W4 SUN AFTERNOON (Box Fit)
        book(svc, members, lessons, "M006", "L45");  // Frank  – W4 SUN AFTERNOON (Box Fit)

        // ── REVIEWS (20+) ─────────────────────────────────────────────────────
        addReview(svc, members, lessons, "M001", "L11", 5, "Loved the morning Yoga session, very relaxing!");
        addReview(svc, members, lessons, "M002", "L11", 4, "Good Yoga class, instructor was clear.");
        addReview(svc, members, lessons, "M003", "L11", 5, "Amazing start to the weekend!");
        addReview(svc, members, lessons, "M004", "L11", 3, "Ok session, a bit crowded.");
        addReview(svc, members, lessons, "M001", "L12", 4, "Zumba was so fun, great energy!");
        addReview(svc, members, lessons, "M005", "L12", 5, "Best Zumba class I've attended!");
        addReview(svc, members, lessons, "M002", "L13", 4, "Box Fit was intense but rewarding.");
        addReview(svc, members, lessons, "M006", "L13", 3, "Decent workout, instructor could explain more.");
        addReview(svc, members, lessons, "M007", "L14", 5, "Aquacise is so refreshing!");
        addReview(svc, members, lessons, "M008", "L14", 4, "Great low-impact session.");
        addReview(svc, members, lessons, "M009", "L15", 2, "Body Blitz was too tough for me.");
        addReview(svc, members, lessons, "M010", "L15", 4, "Hard but satisfying!");
        addReview(svc, members, lessons, "M001", "L21", 5, "Zumba again – always a blast!");
        addReview(svc, members, lessons, "M003", "L21", 4, "Really enjoyed the weekend Zumba.");
        addReview(svc, members, lessons, "M002", "L22", 3, "Aquacise was ok, pool was cold.");
        addReview(svc, members, lessons, "M004", "L22", 4, "Good session overall.");
        addReview(svc, members, lessons, "M007", "L24", 5, "Box Fit with Henry was great fun.");
        addReview(svc, members, lessons, "M001", "L31", 4, "Box Fit in the morning, felt great all day.");
        addReview(svc, members, lessons, "M003", "L32", 5, "Body Blitz was challenging but brilliant.");
        addReview(svc, members, lessons, "M002", "L33", 4, "Evening Zumba – perfect end to a Saturday.");
        addReview(svc, members, lessons, "M001", "L42", 5, "Yoga helped with my back pain.");
        addReview(svc, members, lessons, "M009", "L41", 4, "Aquacise on a Sunday morning – perfect!");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static void book(BookingService svc, List<Member> members,
                             List<Lesson> lessons, String memberId, String lessonId) {
        Member m = findMember(members, memberId);
        Lesson l = findLesson(lessons, lessonId);
        if (m != null && l != null) svc.bookLesson(m, l);
    }

    private static void addReview(BookingService svc, List<Member> members,
                                  List<Lesson> lessons, String memberId, String lessonId,
                                  int rating, String comment) {
        Member m = findMember(members, memberId);
        Lesson l = findLesson(lessons, lessonId);
        if (m != null && l != null) svc.addReview(m, l, rating, comment);
    }

    public static Member findMember(List<Member> members, String memberId) {
        return members.stream()
                .filter(m -> m.getMemberId().equals(memberId))
                .findFirst().orElse(null);
    }

    public static Lesson findLesson(List<Lesson> lessons, String lessonId) {
        return lessons.stream()
                .filter(l -> l.getLessonId().equals(lessonId))
                .findFirst().orElse(null);
    }
}
