package flc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import flc.data.DataInitializer;
import flc.model.Booking;
import flc.model.Day;
import flc.model.Exercise;
import flc.model.Lesson;
import flc.model.Member;
import flc.model.Review;
import flc.service.BookingService;
import flc.service.ReportService;

/**
 * Main Swing GUI for the Furzefield Leisure Centre booking system.
 */
public class FLCApp extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(0x0F1923);
    private static final Color PANEL_BG  = new Color(0x1A2535);
    private static final Color ACCENT    = new Color(0x00BFA5);
    private static final Color ACCENT2   = new Color(0xFF6F61);
    private static final Color TEXT      = new Color(0x2986cc);
    private static final Color SUBTEXT   = new Color(0x8A9BB0);
    private static final Color ROW_ALT   = new Color(0x1E2F42);
    private static final Color ROW_MAIN  = new Color(0x16212E);
    private static final Color GOLD      = new Color(0xFFD700);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_HEAD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 12);

    // ── Data ──────────────────────────────────────────────────────────────────
    private final List<Lesson>  allLessons;
    private final List<Member>  allMembers;
    private final BookingService bookingSvc = new BookingService();
    private final ReportService  reportSvc  = new ReportService();

    // ── Panels ────────────────────────────────────────────────────────────────
    private JTextArea statusArea;

    public FLCApp() {
        allLessons = DataInitializer.buildTimetable();
        allMembers = DataInitializer.buildMembers();
        DataInitializer.populateSampleData(allLessons, allMembers, bookingSvc);

        setTitle("Furzefield Leisure Centre — Booking System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(),   BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Furzefield Leisure Centre");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT);

        JLabel sub = new JLabel("Group Exercise Booking System");
        sub.setFont(FONT_BODY);
        sub.setForeground(SUBTEXT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(sub);

        p.add(left, BorderLayout.WEST);

        JLabel badge = new JLabel("8 Weekends  |  48 Lessons  |  10 Members");
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        badge.setForeground(SUBTEXT);
        p.add(badge, BorderLayout.EAST);

        return p;
    }

    // ── Tab container ─────────────────────────────────────────────────────────

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(FONT_HEAD);
        UIManager.put("TabbedPane.selected", PANEL_BG);

        tabs.addTab("📅  Timetable",   buildTimetableTab());
        tabs.addTab("🔖  Book Lesson", buildBookTab());
        tabs.addTab("🔄  Change Booking", buildChangeTab());
        tabs.addTab("⭐  Add Review",   buildReviewTab());
        tabs.addTab("📊  Reports",      buildReportsTab());
        return tabs;
    }

    // ── TIMETABLE TAB ─────────────────────────────────────────────────────────

    private JPanel buildTimetableTab() {
        JPanel p = darkPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Controls
        JPanel controls = darkPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JLabel filterLbl = label("Filter by:", SUBTEXT, FONT_BODY);

        JComboBox<String> dayBox = styledCombo(new String[]{"All Days", "Saturday", "Sunday"});
        JComboBox<String> exBox  = styledCombo(buildExerciseOptions());

        JButton searchBtn = accentButton("Search");

        controls.add(filterLbl);
        controls.add(label("Day:", SUBTEXT, FONT_BODY));
        controls.add(dayBox);
        controls.add(label("Exercise:", SUBTEXT, FONT_BODY));
        controls.add(exBox);
        controls.add(searchBtn);
        p.add(controls, BorderLayout.NORTH);

        // Table
        String[] cols = {"Weekend", "Day", "Time Slot", "Exercise", "Price (£)", "Booked", "Available"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildTable(model);
        refreshTimetableTable(model, allLessons);

        searchBtn.addActionListener(e -> {
            List<Lesson> filtered = new ArrayList<>(allLessons);
            String day = (String) dayBox.getSelectedItem();
            String ex  = (String) exBox.getSelectedItem();
            if (!"All Days".equals(day))
                filtered = bookingSvc.getLessonsByDay(filtered,
                        "Saturday".equals(day) ? Day.SATURDAY : Day.SUNDAY);
            if (!"All Exercises".equals(ex)) {
                Exercise exType = exerciseFromName(ex);
                if (exType != null)
                    filtered = bookingSvc.getLessonsByExercise(filtered, exType);
            }
            refreshTimetableTable(model, filtered);
            log("Showing " + filtered.size() + " lessons.");
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshTimetableTable(DefaultTableModel model, List<Lesson> lessons) {
        model.setRowCount(0);
        for (Lesson l : lessons) {
            model.addRow(new Object[]{
                "Weekend " + l.getWeekendNumber(),
                l.getDay().getDisplayName(),
                l.getTimeSlot().getDisplayName(),
                l.getExercise().getDisplayName(),
                String.format("%.2f", l.getPrice()),
                l.getBookedCount(),
                l.getAvailableSpaces()
            });
        }
    }

    // ── BOOK LESSON TAB ───────────────────────────────────────────────────────

    private JPanel buildBookTab() {
        JPanel p = darkPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = darkPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<String> memberBox  = styledCombo(buildMemberOptions());
        JComboBox<String> lessonBox  = styledCombo(buildLessonOptions(allLessons));
        JButton bookBtn = accentButton("Book Lesson");

        gbc.gridx = 0; gbc.gridy = 0; form.add(label("Member:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; memberBox.setPreferredSize(new Dimension(380, 30)); form.add(memberBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(label("Lesson:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; lessonBox.setPreferredSize(new Dimension(380, 30)); form.add(lessonBox, gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(bookBtn, gbc);

        bookBtn.addActionListener(e -> {
            String memberSel = (String) memberBox.getSelectedItem();
            String lessonSel = (String) lessonBox.getSelectedItem();
            if (memberSel == null || lessonSel == null) return;

            String memberId = memberSel.split("\\|")[0].trim();
            String lessonId = lessonSel.split("\\|")[0].trim();
            Member m = DataInitializer.findMember(allMembers, memberId);
            Lesson l = DataInitializer.findLesson(allLessons, lessonId);

            if (m == null || l == null) { log("Invalid selection."); return; }
            try {
                Booking b = bookingSvc.bookLesson(m, l);
                log("✅ Booking created: " + b.getBookingId() + " — " + m.getName()
                        + " booked " + l.getExercise().getDisplayName()
                        + " (W" + l.getWeekendNumber() + " " + l.getDay() + " " + l.getTimeSlot().getDisplayName() + ")");
                // Refresh lesson options
                refreshCombo(lessonBox, buildLessonOptions(allLessons));
            } catch (Exception ex) {
                log("❌ " + ex.getMessage());
            }
        });

        p.add(form, BorderLayout.NORTH);
        p.add(buildMemberBookingsPanel(), BorderLayout.CENTER);
        return p;
    }

    private JScrollPane buildMemberBookingsPanel() {
        String[] cols = {"Booking ID", "Member", "Weekend", "Day", "Time", "Exercise", "Price (£)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Member m : allMembers) {
            for (Booking b : m.getBookings()) {
                Lesson l = b.getLesson();
                model.addRow(new Object[]{
                    b.getBookingId(), m.getName(),
                    "W" + l.getWeekendNumber(), l.getDay().getDisplayName(),
                    l.getTimeSlot().getDisplayName(),
                    l.getExercise().getDisplayName(),
                    String.format("%.2f", l.getPrice())
                });
            }
        }
        JTable t = buildTable(model);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT, 1), " Current Bookings ",
            TitledBorder.LEFT, TitledBorder.TOP, FONT_HEAD, ACCENT));
        sp.getViewport().setBackground(ROW_MAIN);
        return sp;
    }

    // ── CHANGE BOOKING TAB ────────────────────────────────────────────────────

    private JPanel buildChangeTab() {
        JPanel p = darkPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = darkPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<String> memberBox   = styledCombo(buildMemberOptions());
        JComboBox<String> bookingBox  = new JComboBox<>();
        styleCombo(bookingBox);
        JComboBox<String> newLessonBox = styledCombo(buildLessonOptions(allLessons));
        JButton changeBtn = accentButton2("Change Booking");

        // When member changes, populate their bookings
        memberBox.addActionListener(e -> {
            String sel = (String) memberBox.getSelectedItem();
            if (sel == null) return;
            String memberId = sel.split("\\|")[0].trim();
            Member m = DataInitializer.findMember(allMembers, memberId);
            bookingBox.removeAllItems();
            if (m != null) {
                for (Booking b : m.getBookings()) {
                    Lesson l = b.getLesson();
                    bookingBox.addItem(b.getBookingId() + " | W" + l.getWeekendNumber()
                        + " " + l.getDay() + " " + l.getTimeSlot().getDisplayName()
                        + " – " + l.getExercise().getDisplayName());
                }
            }
        });
        // Trigger initial population
        memberBox.getActionListeners()[0].actionPerformed(null);

        gbc.gridx = 0; gbc.gridy = 0; form.add(label("Member:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; memberBox.setPreferredSize(new Dimension(380, 30)); form.add(memberBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(label("Current Booking:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; bookingBox.setPreferredSize(new Dimension(380, 30)); form.add(bookingBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(label("New Lesson:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; newLessonBox.setPreferredSize(new Dimension(380, 30)); form.add(newLessonBox, gbc);
        gbc.gridx = 1; gbc.gridy = 3; form.add(changeBtn, gbc);

        changeBtn.addActionListener(e -> {
            String memberSel = (String) memberBox.getSelectedItem();
            String bookingSel = (String) bookingBox.getSelectedItem();
            String newLessonSel = (String) newLessonBox.getSelectedItem();
            if (memberSel == null || bookingSel == null || newLessonSel == null) return;

            String memberId = memberSel.split("\\|")[0].trim();
            String bookingId = bookingSel.split("\\|")[0].trim();
            String lessonId  = newLessonSel.split("\\|")[0].trim();

            Member m = DataInitializer.findMember(allMembers, memberId);
            Lesson newLesson = DataInitializer.findLesson(allLessons, lessonId);
            if (m == null || newLesson == null) return;

            Booking target = m.getBookings().stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst().orElse(null);
            if (target == null) { log("Booking not found."); return; }

            try {
                bookingSvc.changeBooking(target, newLesson);
                log("🔄 Booking " + bookingId + " changed to "
                        + newLesson.getExercise().getDisplayName()
                        + " (W" + newLesson.getWeekendNumber() + " "
                        + newLesson.getDay() + " " + newLesson.getTimeSlot().getDisplayName() + ")");
                // Refresh booking dropdown
                memberBox.getActionListeners()[0].actionPerformed(null);
            } catch (Exception ex) {
                log("❌ " + ex.getMessage());
            }
        });

        p.add(form, BorderLayout.NORTH);
        return p;
    }

    // ── REVIEW TAB ────────────────────────────────────────────────────────────

    private JPanel buildReviewTab() {
        JPanel p = darkPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = darkPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<String> memberBox = styledCombo(buildMemberOptions());
        JComboBox<String> lessonBox = styledCombo(buildLessonOptions(allLessons));
        JComboBox<String> ratingBox = styledCombo(new String[]{
            "5 – Very Satisfied", "4 – Satisfied", "3 – Ok", "2 – Dissatisfied", "1 – Very Dissatisfied"
        });
        JTextField commentField = new JTextField(30);
        styleTextField(commentField);
        JButton submitBtn = accentButton("Submit Review");

        gbc.gridx = 0; gbc.gridy = 0; form.add(label("Member:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; memberBox.setPreferredSize(new Dimension(380, 30)); form.add(memberBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(label("Lesson:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; lessonBox.setPreferredSize(new Dimension(380, 30)); form.add(lessonBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(label("Rating:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; ratingBox.setPreferredSize(new Dimension(220, 30)); form.add(ratingBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; form.add(label("Comment:", TEXT, FONT_HEAD), gbc);
        gbc.gridx = 1; commentField.setPreferredSize(new Dimension(380, 30)); form.add(commentField, gbc);
        gbc.gridx = 1; gbc.gridy = 4; form.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            String memberSel = (String) memberBox.getSelectedItem();
            String lessonSel = (String) lessonBox.getSelectedItem();
            String ratingSel = (String) ratingBox.getSelectedItem();
            String comment   = commentField.getText().trim();
            if (memberSel == null || lessonSel == null || ratingSel == null) return;

            String memberId = memberSel.split("\\|")[0].trim();
            String lessonId = lessonSel.split("\\|")[0].trim();
            int rating = Integer.parseInt(ratingSel.split("–")[0].trim());

            Member m = DataInitializer.findMember(allMembers, memberId);
            Lesson l = DataInitializer.findLesson(allLessons, lessonId);
            if (m == null || l == null) return;

            try {
                bookingSvc.addReview(m, l, rating, comment.isEmpty() ? "No comment." : comment);
                log("⭐ Review submitted: " + m.getName() + " rated "
                        + l.getExercise().getDisplayName() + " " + rating + "/5");
                commentField.setText("");
            } catch (Exception ex) {
                log("❌ " + ex.getMessage());
            }
        });

        p.add(form, BorderLayout.NORTH);
        p.add(buildReviewsTable(), BorderLayout.CENTER);
        return p;
    }

    private JScrollPane buildReviewsTable() {
        String[] cols = {"Member", "Weekend", "Day", "Exercise", "Rating", "Label", "Comment"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Lesson l : allLessons) {
            for (Review r : l.getReviews()) {
                model.addRow(new Object[]{
                    r.getMember().getName(),
                    "W" + l.getWeekendNumber(),
                    l.getDay().getDisplayName(),
                    l.getExercise().getDisplayName(),
                    r.getRating() + "/5",
                    r.getRatingLabel(),
                    r.getComment()
                });
            }
        }
        JTable t = buildTable(model);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(GOLD, 1), " All Reviews ",
            TitledBorder.LEFT, TitledBorder.TOP, FONT_HEAD, GOLD));
        sp.getViewport().setBackground(ROW_MAIN);
        return sp;
    }

    // ── REPORTS TAB ───────────────────────────────────────────────────────────

    private JPanel buildReportsTab() {
        JPanel p = darkPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(12, 16, 12, 16));

        JTextArea area = new JTextArea();
        area.setFont(FONT_MONO);
        area.setBackground(new Color(0x0A1520));
        area.setForeground(new Color(0x98D8C8));
        area.setCaretColor(ACCENT);
        area.setEditable(false);
        area.setText(reportSvc.generateFullReport(allLessons));

        JScrollPane sp = new JScrollPane(area);
        sp.getViewport().setBackground(area.getBackground());

        JPanel btnRow = darkPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton r1 = accentButton("Attendance Report");
        JButton r2 = accentButton2("Income Report");
        JButton both = styledButton("Both Reports", SUBTEXT);

        r1.addActionListener(e   -> area.setText(reportSvc.generateAttendanceReport(allLessons)));
        r2.addActionListener(e   -> area.setText(reportSvc.generateIncomeReport(allLessons)));
        both.addActionListener(e -> area.setText(reportSvc.generateFullReport(allLessons)));

        btnRow.add(r1); btnRow.add(r2); btnRow.add(both);
        p.add(btnRow, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private JPanel buildStatus() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x0A1015));
        p.setBorder(new EmptyBorder(4, 12, 4, 12));

        statusArea = new JTextArea(2, 80);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusArea.setBackground(new Color(0x0A1015));
        statusArea.setForeground(new Color(0x6FB891));
        statusArea.setEditable(false);
        statusArea.setText("System ready. Sample data loaded: 8 weekends, 10 members, 22+ reviews.\n");

        p.add(statusArea, BorderLayout.CENTER);
        return p;
    }

    private void log(String msg) {
        statusArea.setText(msg + "\n" + statusArea.getText());
    }

    // ── Build helpers ─────────────────────────────────────────────────────────

    private String[] buildMemberOptions() {
        return allMembers.stream()
                .map(m -> m.getMemberId() + " | " + m.getName())
                .toArray(String[]::new);
    }

    private String[] buildExerciseOptions() {
        String[] arr = new String[Exercise.values().length + 1];
        arr[0] = "All Exercises";
        for (int i = 0; i < Exercise.values().length; i++)
            arr[i + 1] = Exercise.values()[i].getDisplayName();
        return arr;
    }

    private String[] buildLessonOptions(List<Lesson> lessons) {
        return lessons.stream()
                .filter(Lesson::hasSpace)
                .map(l -> l.getLessonId() + " | W" + l.getWeekendNumber()
                        + " " + l.getDay() + " " + l.getTimeSlot().getDisplayName()
                        + " – " + l.getExercise().getDisplayName()
                        + " (£" + String.format("%.2f", l.getPrice()) + ")"
                        + " [" + l.getAvailableSpaces() + " spaces]")
                .toArray(String[]::new);
    }

    private Exercise exerciseFromName(String name) {
        for (Exercise e : Exercise.values())
            if (e.getDisplayName().equals(name)) return e;
        return null;
    }

    private void refreshCombo(JComboBox<String> combo, String[] items) {
        combo.removeAllItems();
        for (String s : items) combo.addItem(s);
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private JPanel darkPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(PANEL_BG);
        return p;
    }

    private JLabel label(String text, Color color, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(font);
        return l;
    }

    private JButton accentButton(String text) { return styledButton(text, ACCENT); }
    private JButton accentButton2(String text) { return styledButton(text, ACCENT2); }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 18, 7, 18));
        return btn;
    }

    private <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        styleCombo(c);
        return c;
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(new Color(0x263545));
        c.setForeground(TEXT);
        c.setFont(FONT_BODY);
        c.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(new Color(0x263545));
        tf.setForeground(TEXT);
        tf.setFont(FONT_BODY);
        tf.setCaretColor(TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            new EmptyBorder(3, 6, 3, 6)));
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(ROW_MAIN);
        t.setForeground(TEXT);
        t.setFont(FONT_BODY);
        t.setRowHeight(26);
        t.setGridColor(new Color(0x2A3F55));
        t.setSelectionBackground(ACCENT.darker());
        t.setSelectionForeground(Color.WHITE);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);

        // Header
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(0x0F1F30));
        h.setForeground(ACCENT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Alternating rows
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(sel ? ACCENT.darker() : (row % 2 == 0 ? ROW_MAIN : ROW_ALT));
                setForeground(sel ? Color.WHITE : TEXT);
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        return t;
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {}
        SwingUtilities.invokeLater(() -> new FLCApp().setVisible(true));
    }
}
