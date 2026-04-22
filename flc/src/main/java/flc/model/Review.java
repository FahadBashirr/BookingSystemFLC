package flc.model;

/**
 * Represents a review left by a member after attending a lesson.
 * Rating is an integer from 1 (Very dissatisfied) to 5 (Very Satisfied).
 */
public class Review {
    private final Member member;
    private final Lesson lesson;
    private final int rating;        // 1–5
    private final String comment;

    public Review(Member member, Lesson lesson, int rating, String comment) {
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        this.member  = member;
        this.lesson  = lesson;
        this.rating  = rating;
        this.comment = comment;
    }

    public Member getMember()  { return member; }
    public Lesson getLesson()  { return lesson; }
    public int getRating()     { return rating; }
    public String getComment() { return comment; }

    public String getRatingLabel() {
        return switch (rating) {
            case 1 -> "Very Dissatisfied";
            case 2 -> "Dissatisfied";
            case 3 -> "Ok";
            case 4 -> "Satisfied";
            case 5 -> "Very Satisfied";
            default -> "Unknown";
        };
    }

    @Override
    public String toString() {
        return member.getName() + " rated " + lesson.getExercise().getDisplayName()
                + " " + rating + "/5 (" + getRatingLabel() + "): " + comment;
    }
}
