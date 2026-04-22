package flc.service;

import flc.model.*;

import java.util.*;

/**
 * Generates the two required reports for the FLC system.
 */
public class ReportService {

    /**
     * Report 1: For each lesson (grouped by weekend → day → time slot),
     * prints the number of members booked and the average rating.
     *
     * @param allLessons all lessons in the system
     * @return formatted report string
     */
    public String generateAttendanceReport(List<Lesson> allLessons) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("   REPORT 1: ATTENDANCE & RATINGS PER LESSON\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        // Group by weekend
        Map<Integer, List<Lesson>> byWeekend = new TreeMap<>();
        for (Lesson l : allLessons) {
            byWeekend.computeIfAbsent(l.getWeekendNumber(), k -> new ArrayList<>()).add(l);
        }

        for (Map.Entry<Integer, List<Lesson>> wEntry : byWeekend.entrySet()) {
            sb.append(String.format("  ── Weekend %d ──────────────────────────────────────\n",
                    wEntry.getKey()));

            // Group by day
            Map<Day, List<Lesson>> byDay = new LinkedHashMap<>();
            for (Day d : Day.values()) byDay.put(d, new ArrayList<>());
            for (Lesson l : wEntry.getValue()) byDay.get(l.getDay()).add(l);

            for (Map.Entry<Day, List<Lesson>> dEntry : byDay.entrySet()) {
                sb.append(String.format("    %s:\n", dEntry.getKey().getDisplayName()));
                List<Lesson> dayLessons = dEntry.getValue();
                dayLessons.sort(Comparator.comparing(Lesson::getTimeSlot));
                for (Lesson l : dayLessons) {
                    double avg = l.getAverageRating();
                    String avgStr = avg == 0.0 ? "No reviews" : String.format("%.1f / 5.0", avg);
                    sb.append(String.format(
                            "      %-10s  %-12s  Members: %d/%d   Avg Rating: %s\n",
                            l.getTimeSlot().getDisplayName(),
                            l.getExercise().getDisplayName(),
                            l.getBookedCount(), Lesson.MAX_CAPACITY, avgStr));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Report 2: Calculates total income per exercise type (all slots combined)
     * and identifies the highest earner.
     *
     * @param allLessons all lessons in the system
     * @return formatted report string
     */
    public String generateIncomeReport(List<Lesson> allLessons) {
        // Aggregate income per exercise
        Map<Exercise, Double> incomeMap = new TreeMap<>(Comparator.comparing(Exercise::getDisplayName));
        Map<Exercise, Integer> bookingCount = new TreeMap<>(Comparator.comparing(Exercise::getDisplayName));

        for (Lesson l : allLessons) {
            incomeMap.merge(l.getExercise(), l.getTotalIncome(), Double::sum);
            bookingCount.merge(l.getExercise(), l.getBookedCount(), Integer::sum);
        }

        // Find the highest income exercise
        Exercise topExercise = null;
        double topIncome = -1;
        for (Map.Entry<Exercise, Double> e : incomeMap.entrySet()) {
            if (e.getValue() > topIncome) {
                topIncome = e.getValue();
                topExercise = e.getKey();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("   REPORT 2: INCOME BY EXERCISE TYPE\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        sb.append(String.format("  %-15s  %-8s  %-12s  %s\n",
                "Exercise", "Price", "Total Members", "Total Income"));
        sb.append("  ─────────────────────────────────────────────────────\n");

        for (Exercise ex : Exercise.values()) {
            double income = incomeMap.getOrDefault(ex, 0.0);
            int members = bookingCount.getOrDefault(ex, 0);
            String marker = ex == topExercise ? "  ◄ HIGHEST" : "";
            sb.append(String.format("  %-15s  £%-7.2f  %-12d  £%-10.2f%s\n",
                    ex.getDisplayName(), ex.getPrice(), members, income, marker));
        }

        sb.append("\n");
        if (topExercise != null) {
            sb.append(String.format(
                    "  ★ Highest Income Exercise: %s  (Total: £%.2f)\n",
                    topExercise.getDisplayName(), topIncome));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Convenience: returns both reports concatenated.
     */
    public String generateFullReport(List<Lesson> allLessons) {
        return generateAttendanceReport(allLessons) + "\n" + generateIncomeReport(allLessons);
    }
}
