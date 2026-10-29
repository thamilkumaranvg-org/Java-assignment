
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AcademicDAO {

    public void enrollStudent(String rollNumber,
                              String courseCode,
                              int semester) {

        String sql = "INSERT INTO enrollments " +
                     "(roll_number, course_code, semester) " +
                     "VALUES (?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {
            statement.setString(1, rollNumber);
            statement.setString(2, courseCode);
            statement.setInt(3, semester);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Student enrolled successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Error enrolling student.");
            e.printStackTrace();
        }
    }

    public void displayEnrollments() {

        String sql =
            "SELECT e.enrollment_id, e.roll_number, s.name, " +
            "e.course_code, c.title, e.semester " +
            "FROM enrollments e " +
            "JOIN students s ON e.roll_number = s.roll_number " +
            "JOIN courses c ON e.course_code = c.course_code " +
            "ORDER BY e.enrollment_id";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                System.out.println(
                    "Enrollment ID: " +
                    resultSet.getInt("enrollment_id")
                );
                System.out.println(
                    "Roll Number: " +
                    resultSet.getString("roll_number")
                );
                System.out.println(
                    "Student Name: " +
                    resultSet.getString("name")
                );
                System.out.println(
                    "Course Code: " +
                    resultSet.getString("course_code")
                );
                System.out.println(
                    "Course Title: " +
                    resultSet.getString("title")
                );
                System.out.println(
                    "Semester: " +
                    resultSet.getInt("semester")
                );
                System.out.println("----------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving enrollments.");
            e.printStackTrace();
        }
    }

public void updateAttendance(int enrollmentId, int classesHeld, int classesAttended) {

    if (classesHeld < 0 || classesAttended < 0 || classesAttended > classesHeld) {
        System.out.println("Invalid attendance values.");
        return;
    }

    String sql = "UPDATE enrollments " + "SET classes_held = ?, classes_attended = ? " + "WHERE enrollment_id = ?";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)
    ) {
        statement.setInt(1, classesHeld);
        statement.setInt(2, classesAttended);
        statement.setInt(3, enrollmentId);

        int rowsUpdated = statement.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("Attendance updated successfully!");
        } else {
            System.out.println("Enrollment not found.");
        }

    } catch (SQLException e) {
        System.out.println("Error updating attendance.");
        e.printStackTrace();
    }
}


public void displayAttendanceReport() {

    String sql =
        "SELECT s.roll_number, s.name, s.department, " +
        "c.course_code, c.title, e.semester, " +
        "e.classes_held, e.classes_attended " +
        "FROM enrollments e " +
        "JOIN students s ON e.roll_number = s.roll_number " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "ORDER BY s.department, s.roll_number, c.course_code";

    String format = "%-12s %-20s %-8s %-12s %8s %10s %-35s%n";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()
    ) {
        System.out.println("\n==================== ATTENDANCE REPORT ====================");

        System.out.printf(
            format,
            "Roll No.", "Student Name", "Dept.", "Course",
            "Held", "Attended", "Attendance / Status"
        );

        System.out.println(
            "---------------------------------------------------------------------------------------------------------------"
        );

        boolean found = false;

        while (result.next()) {
            found = true;

            int held = result.getInt("classes_held");
            int attended = result.getInt("classes_attended");

            String attendanceText;
            if (held <= 0) {
                attendanceText = "Not recorded | Pending";
            } else {
                double percentage = attended * 100.0 / held;
                String status;

                if (percentage >= 75.0) {
                    status = "Eligible";
                } else if (percentage >= 65.0) {
                    status = "Medical condonation";
                } else {
                    status = "SA";
                }

                attendanceText = String.format(
                    "%.2f%% | %s", percentage, status
                );
            }

            System.out.printf(
                "%-12s %-20s %-8s %-12s %8d %10d %-35s%n",
                result.getString("roll_number"),
                result.getString("name"),
                result.getString("department"),
                result.getString("course_code"),
                held,
                attended,
                attendanceText
            );
        }

        if (!found) {
            System.out.println("No enrollment records found.");
        }

        System.out.println(
            "==============================================================================================================="
        );

    } catch (SQLException e) {
        System.out.println("Error generating attendance report.");
        e.printStackTrace();
    }
}


public void updateAssessmentMarks(int enrollmentId, Double cia1, Double cia2, Double assignments, Double quizzes, Double labEvaluations, Double eseMarks, boolean eseAbsent) {

    if (eseAbsent && eseMarks != null) {
        System.out.println( "Absent students cannot have ESE marks.");
        return;
    }

    String sql =
    "INSERT INTO assessment_marks " +
    "(enrollment_id, cia1, cia2, assignments, quizzes, " +
    "lab_evaluations, ese_marks, ese_absent) " +
    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
    "ON CONFLICT (enrollment_id) DO UPDATE SET " +
    "cia1 = COALESCE(EXCLUDED.cia1, assessment_marks.cia1), " +
    "cia2 = COALESCE(EXCLUDED.cia2, assessment_marks.cia2), " +
    "assignments = COALESCE(EXCLUDED.assignments, assessment_marks.assignments), " +
    "quizzes = COALESCE(EXCLUDED.quizzes, assessment_marks.quizzes), " +
    "lab_evaluations = COALESCE(EXCLUDED.lab_evaluations, assessment_marks.lab_evaluations), " +
    "ese_marks = CASE " +
    "WHEN EXCLUDED.ese_absent THEN NULL " +
    "WHEN EXCLUDED.ese_marks IS NOT NULL THEN EXCLUDED.ese_marks " +
    "ELSE assessment_marks.ese_marks END, " +
    "ese_absent = CASE " +
    "WHEN EXCLUDED.ese_absent THEN TRUE " +
    "WHEN EXCLUDED.ese_marks IS NOT NULL THEN FALSE " +
    "ELSE assessment_marks.ese_absent END";
    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement =
                connection.prepareStatement(sql)
    ) {
        statement.setInt(1, enrollmentId);

        if (cia1 == null) statement.setNull(2, java.sql.Types.NUMERIC);
        else statement.setDouble(2, cia1);

        if (cia2 == null) statement.setNull(3, java.sql.Types.NUMERIC);
        else statement.setDouble(3, cia2);

        if (assignments == null) statement.setNull(4, java.sql.Types.NUMERIC);
        else statement.setDouble(4, assignments);

        if (quizzes == null) statement.setNull(5, java.sql.Types.NUMERIC);
        else statement.setDouble(5, quizzes);

        if (labEvaluations == null) statement.setNull(6, java.sql.Types.NUMERIC);
        else statement.setDouble(6, labEvaluations);

        if (eseMarks == null) statement.setNull(7, java.sql.Types.NUMERIC);
        else statement.setDouble(7, eseMarks);

        statement.setBoolean(8, eseAbsent);

        statement.executeUpdate();

        System.out.println("Assessment marks saved successfully!");

    } catch (SQLException e) {
        System.out.println("Error saving assessment marks.");
        e.printStackTrace();
    }
}
public void displayAssessmentMarks(int enrollmentId) {

    String sql =
        "SELECT s.roll_number, s.name, c.title, " +
        "a.cia1, a.cia2, a.assignments, a.quizzes, " +
        "a.lab_evaluations, a.ese_marks, a.ese_absent " +
        "FROM assessment_marks a " +
        "JOIN enrollments e ON a.enrollment_id = e.enrollment_id " +
        "JOIN students s ON e.roll_number = s.roll_number " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "WHERE a.enrollment_id = ?";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql)
    ) {
        statement.setInt(1, enrollmentId);

        try (ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                System.out.println("\n===== ASSESSMENT MARKS =====");
                System.out.println("Roll Number: " +
                    result.getString("roll_number"));
                System.out.println("Student Name: " +
                    result.getString("name"));
                System.out.println("Course: " +
                    result.getString("title"));

                System.out.println("CIA1: " +
                    result.getObject("cia1"));
                System.out.println("CIA2: " +
                    result.getObject("cia2"));
                System.out.println("Assignments: " +
                    result.getObject("assignments"));
                System.out.println("Quizzes: " +
                    result.getObject("quizzes"));
                System.out.println("Lab Evaluations: " +
                    result.getObject("lab_evaluations"));
                System.out.println("ESE Marks: " +
                    result.getObject("ese_marks"));
                System.out.println("ESE Absent: " +
                    result.getBoolean("ese_absent"));

                System.out.println("============================");
            } else {
                System.out.println(
                    "No assessment marks found for enrollment ID: "
                    + enrollmentId
                );
            }

        }

    } catch (SQLException e) {
        System.out.println("Error retrieving assessment marks.");
        e.printStackTrace();
    }
}
public double calculateCIAScore( double cia1,double cia2,double assignments,double quizzes,double labEvaluations) {

    return (cia1 + cia2 + assignments
            + quizzes + labEvaluations) / 5.0;
}
public double calculateCourseResult(
        String courseType,
        double ciaScore,
        double eseMarks) {

    double ciaWeight;
    double eseWeight;

    switch (courseType.toUpperCase()) {

        case "THEORY":
            ciaWeight = 40;
            eseWeight = 60;
            break;

        case "PRACTICAL":
            ciaWeight = 60;
            eseWeight = 40;
            break;

        case "EMBEDDED":
            ciaWeight = 50;
            eseWeight = 50;
            break;

        default:
            throw new IllegalArgumentException(
                "Invalid course type: " + courseType
            );
    }

    double weightedCIA =
        ciaScore * ciaWeight / 100.0;

    double weightedESE =
        eseMarks * eseWeight / 100.0;

    double totalMarks = weightedCIA + weightedESE;

    return totalMarks;
}

public String calculateGrade(String courseType, double totalMarks, double eseMarks, boolean eseAbsent, double attendancePercentage, boolean medicalCondonationApproved) {

    if (attendancePercentage < 65) {
        return "SA";
    }

    if (attendancePercentage < 75
            && !medicalCondonationApproved) {
        return "CONDITIONAL";
    }

    if (eseAbsent) {
        return "AB";
    }

    double minimumESE;

    switch (courseType.toUpperCase()) {
        case "THEORY":
            minimumESE = 45.0;
            break;

        case "PRACTICAL":
            minimumESE = 50.0;
            break;

        case "EMBEDDED":
            minimumESE = 45.0;
            break;

        default:
            throw new IllegalArgumentException(
                "Invalid course type: " + courseType
            );
    }

    if (eseMarks < minimumESE || totalMarks < 50) {
        return "RA";
    }

    if (totalMarks >= 91) return "O";
    if (totalMarks >= 81) return "A+";
    if (totalMarks >= 71) return "A";
    if (totalMarks >= 61) return "B+";
    return "B";
}
public int getGradePoint(String grade) {

    switch (grade.toUpperCase()) {
        case "O":
            return 10;

        case "A+":
            return 9;

        case "A":
            return 8;

        case "B+":
            return 7;

        case "B":
            return 6;

        case "RA":
        case "SA":
        case "AB":
            return 0;

        default:
            throw new IllegalArgumentException(
                "Invalid grade: " + grade
            );
    }
}
public double calculateSGPA(
        int[] credits,
        int[] gradePoints) {

    if (credits.length == 0 ||
            credits.length != gradePoints.length) {
        throw new IllegalArgumentException(
            "Credits and grade points must match."
        );
    }

    int totalCredits = 0;
    int weightedPoints = 0;

    for (int i = 0; i < credits.length; i++) {

        if (credits[i] <= 0 ||
                gradePoints[i] < 0 ||
                gradePoints[i] > 10) {
            throw new IllegalArgumentException(
                "Invalid credits or grade points."
            );
        }

        totalCredits += credits[i];
        weightedPoints += credits[i] * gradePoints[i];
    }

    return (double) weightedPoints / totalCredits;
}
public double calculateSGPA(String rollNumber, int semester) throws SQLException {

    String sql =
        "SELECT c.credits, am.cia1, am.cia2, " +
        "am.assignments, am.quizzes, " +
        "am.lab_evaluations, am.ese_marks, " +
        "am.ese_absent, c.course_type, " +
        "e.classes_held, e.classes_attended, " +
        "e.medical_condonation_approved " +
        "FROM enrollments e " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "LEFT JOIN assessment_marks am " +
        "ON e.enrollment_id = am.enrollment_id " +
        "WHERE e.roll_number = ? AND e.semester = ?";

    int totalCredits = 0;
    int weightedGradePoints = 0;

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, rollNumber);
        statement.setInt(2, semester);

        try (ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                int credits = result.getInt("credits");

                Double cia1 = getNullableDouble(result, "cia1");
                Double cia2 = getNullableDouble(result, "cia2");
                Double assignments = getNullableDouble(result, "assignments");
                Double quizzes = getNullableDouble(result, "quizzes");
                Double labEvaluations = getNullableDouble(result, "lab_evaluations");
                Double eseMarks = getNullableDouble(result, "ese_marks");

                boolean eseAbsent = result.getBoolean("ese_absent");

                if (cia1 == null || cia2 == null ||
                        assignments == null || quizzes == null ||
                        labEvaluations == null ||
                        (!eseAbsent && eseMarks == null)) {

                    throw new SQLException(
                        "Assessment marks are incomplete for a course."
                    );
                }

                double ciaScore = calculateCIAScore(
                    cia1, cia2, assignments, quizzes, labEvaluations
                );

                double eseScore = eseAbsent ? 0.0 : eseMarks;

                double totalMarks = calculateCourseResult(
                    result.getString("course_type"),
                    ciaScore,
                    eseScore
                );

                double attendancePercentage =
                    result.getInt("classes_held") == 0
                    ? 0.0
                    : 100.0 * result.getInt("classes_attended")
                      / result.getInt("classes_held");

                String grade = calculateGrade(
                    result.getString("course_type"),
                    totalMarks,
                    eseScore,
                    eseAbsent,
                    attendancePercentage,
                    result.getBoolean(
                        "medical_condonation_approved"
                    )
                );

                int gradePoint = getGradePoint(grade);

                totalCredits += credits;
                weightedGradePoints += credits * gradePoint;
            }
        }
    }

    if (totalCredits == 0) {
        throw new SQLException(
            "No enrolled courses found for this student and semester."
        );
    }

    return (double) weightedGradePoints / totalCredits;
}
private Double getNullableDouble(
        ResultSet result,
        String columnName) throws SQLException {

    Object value = result.getObject(columnName);

    if (value == null) {
        return null;
    }

    return ((Number) value).doubleValue();
}


public void displayStudentMarkSheet(
        String rollNumber,
        int semester) throws SQLException {

    String sql =
        "SELECT s.name, s.roll_number, s.department, " +
        "e.semester, c.course_code, c.title, c.credits, " +
        "c.course_type, e.classes_held, e.classes_attended, " +
        "e.medical_condonation_approved, " +
        "am.cia1, am.cia2, am.assignments, am.quizzes, " +
        "am.lab_evaluations, am.ese_marks, am.ese_absent " +
        "FROM enrollments e " +
        "JOIN students s ON e.roll_number = s.roll_number " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "LEFT JOIN assessment_marks am " +
        "ON e.enrollment_id = am.enrollment_id " +
        "WHERE e.roll_number = ? AND e.semester = ? " +
        "ORDER BY c.course_code";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
             connection.prepareStatement(sql)) {

        statement.setString(1, rollNumber);
        statement.setInt(2, semester);

        try (ResultSet rs = statement.executeQuery()) {

            boolean found = false;
            int registeredCredits = 0;
            int earnedCredits = 0;
            double weightedGradePoints = 0.0;
            boolean allPassed = true;
            boolean pending = false;

            System.out.println();
            System.out.println(
                "==========================================================================");
            System.out.println(
                "                 KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
            System.out.println(
                "                         PERUNDURAI, ERODE");
            System.out.println(
                "                 GRADE SHEET - REGULATION 2024");
            System.out.println(
                "==========================================================================");

            while (rs.next()) {

                if (!found) {
                    found = true;

                    System.out.printf(
                        "Roll Number : %-22s Student Name : %s%n",
                        rs.getString("roll_number"),
                        rs.getString("name"));

                    System.out.printf(
                        "Department  : %-22s Semester     : %d%n",
                        rs.getString("department"),
                        rs.getInt("semester"));

                    System.out.println(
                        "Regulation  : R2024                Date         : "
                        + java.time.LocalDate.now());

                    System.out.println(
                        "==========================================================================");

                    System.out.printf(
                        "%-10s %-29s %-11s %4s %7s%n",
                        "Code", "Course Title", "Type",
                        "Cr", "Attend%");

                    System.out.println(
                        "--------------------------------------------------------------------------");
                }

                String code = rs.getString("course_code");
                String title = rs.getString("title");
                String type = rs.getString("course_type");
                int credits = rs.getInt("credits");
                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");

                registeredCredits += credits;

                double attendance = held > 0
                    ? 100.0 * attended / held
                    : 0.0;

                Object cia1Obj = rs.getObject("cia1");
                Object cia2Obj = rs.getObject("cia2");
                Object assignmentObj = rs.getObject("assignments");
                Object quizObj = rs.getObject("quizzes");
                Object labObj = rs.getObject("lab_evaluations");
                Object eseObj = rs.getObject("ese_marks");

                boolean absent = rs.getBoolean("ese_absent");
                boolean condonation =
                    rs.getBoolean("medical_condonation_approved");

                System.out.printf(
                    "%-10s %-29.29s %-11s %4d %6.2f%%%n",
                    code, title, type, credits,
                    held > 0 ? attendance : 0.0);

                System.out.printf(
                    "  CIA1: %-7s CIA2: %-7s Assignment: %-7s%n",
                    formatMark(cia1Obj),
                    formatMark(cia2Obj),
                    formatMark(assignmentObj));

                System.out.printf(
                    "  Quiz: %-7s Lab: %-7s ESE: %-9s%n",
                    formatMark(quizObj),
                    formatMark(labObj),
                    absent ? "AB" : formatMark(eseObj));

                System.out.println(
                    "  ------------------------------------------------------------------------");

                System.out.print("  Attendance Status: ");

                if (held == 0) {
                    System.out.println("Pending - attendance not recorded");
                } else if (attendance >= 75.0) {
                    System.out.println("Eligible for ESE");
                } else if (attendance >= 65.0) {
                    System.out.println(condonation
                        ? "Conditional - Condonation Approved"
                        : "Conditional - Condonation Required");
                } else {
                    System.out.println("SA - Shortage of Attendance");
                }

                boolean marksComplete =
                    cia1Obj != null &&
                    cia2Obj != null &&
                    assignmentObj != null &&
                    quizObj != null &&
                    labObj != null &&
                    (eseObj != null || absent);

                if (!marksComplete || held == 0) {
                    System.out.println(
                        "  Course Result: PENDING - incomplete data");
                    pending = true;
                    allPassed = false;
                    System.out.println();
                    continue;
                }

                double ciaScore = calculateCIAScore(
                    ((Number) cia1Obj).doubleValue(),
                    ((Number) cia2Obj).doubleValue(),
                    ((Number) assignmentObj).doubleValue(),
                    ((Number) quizObj).doubleValue(),
                    ((Number) labObj).doubleValue());

                double eseScore = eseObj == null
                    ? 0.0
                    : ((Number) eseObj).doubleValue();

                double total = calculateCourseResult(
                    type, ciaScore, eseScore);

                double ciaWeight;
                double eseWeight;

                switch (type.toUpperCase()) {
                    case "THEORY":
                        ciaWeight = 40.0;
                        eseWeight = 60.0;
                        break;
                    case "PRACTICAL":
                        ciaWeight = 60.0;
                        eseWeight = 40.0;
                        break;
                    case "EMBEDDED":
                        ciaWeight = 50.0;
                        eseWeight = 50.0;
                        break;
                    default:
                        throw new IllegalArgumentException(
                            "Invalid course type: " + type);
                }

                double ciaContribution =
                    ciaScore * ciaWeight / 100.0;

                double eseContribution =
                    eseScore * eseWeight / 100.0;

                String grade = calculateGrade(
                    type,
                    total,
                    eseScore,
                    absent,
                    attendance,
                    condonation);

                int gradePoint = getGradePoint(grade);

                System.out.printf(
                    "  CIA Average: %.2f | Scaled CIA: %.2f%n",
                    ciaScore, ciaContribution);

                System.out.printf(
                    "  ESE Marks: %.2f | Scaled ESE: %.2f%n",
                    eseScore, eseContribution);

                System.out.printf(
                    "  Weighted Total: %.2f | Grade: %s | Grade Point: %d%n",
                    total, grade, gradePoint);

                if (grade.equals("CONDITIONAL")) {
                    System.out.println(
                        "  Result: CONDITIONAL - Medical condonation required");
                    pending = true;
                    allPassed = false;
                } else if (grade.equals("RA") ||
                           grade.equals("SA") ||
                           grade.equals("AB")) {
                    System.out.println("  Result: NOT PASSED");
                    allPassed = false;
                } else if (grade.equals("PENDING")) {
                    System.out.println("  Result: PENDING");
                    pending = true;
                    allPassed = false;
                } else {
                    System.out.println("  Result: PASS");
                    earnedCredits += credits;
                    weightedGradePoints += credits * gradePoint;
                }

                System.out.println(
                    "--------------------------------------------------------------------------");
            }

            if (!found) {
                System.out.println(
                    "No enrolled courses found for this student and semester.");
                return;
            }

            System.out.println();
            System.out.println(
                "======================= SEMESTER SUMMARY =======================");

            System.out.printf(
                "Registered Credits    : %d%n", registeredCredits);

            System.out.printf(
                "Earned Credits        : %d%n", earnedCredits);

            System.out.printf(
                "Weighted Grade Points : %.2f%n", weightedGradePoints);

            // Use the existing SGPA calculation for the semester.
            double sgpa = calculateSGPA(rollNumber, semester);

            System.out.printf(
                "Semester SGPA         : %.2f%n", sgpa);

            System.out.printf(
                "Equivalent Percentage : %.2f%%%n", sgpa * 10.0);

            if (pending) {
                System.out.println(
                    "Semester Outcome      : PENDING - Review required");
            } else if (allPassed) {
                System.out.println(
                    "Semester Outcome      : PASS - All courses cleared");
            } else {
                System.out.println(
                    "Semester Outcome      : NOT CLEARED");
            }

            System.out.println(
                "==========================================================================");
            System.out.println("                       GRADING DETAILS");
            System.out.println(
                "--------------------------------------------------------------------------");
            System.out.println("Grade     Marks Range     Grade Point");
            System.out.println("O         91 - 100         10");
            System.out.println("A+        81 - 90           9");
            System.out.println("A         71 - 80           8");
            System.out.println("B+        61 - 70           7");
            System.out.println("B         50 - 60           6");
            System.out.println("RA        Result below pass requirements");
            System.out.println("SA        Shortage of attendance");
            System.out.println("AB        Absent for ESE");
            System.out.println(
                "--------------------------------------------------------------------------");
            System.out.println("CONFIGURED COURSE WEIGHTAGES");
            System.out.println("THEORY    : CIA 40% | ESE 60% | ESE minimum 27/60");
            System.out.println("PRACTICAL : CIA 60% | ESE 40% | ESE minimum 20/40");
            System.out.println("EMBEDDED  : CIA 50% | ESE 50% | ESE minimum 22.5/50");
            System.out.println("Overall pass minimum: 50 marks");
            System.out.println(
                "==========================================================================");
        }
    }
}
private String formatMark(Object mark) {
    if (mark == null) {
        return "Pending";
    }

    return String.format("%.2f", ((Number) mark).doubleValue());
}
public void displayAssessmentMarksByRollNumber(String rollNumber) {

    String sql =
        "SELECT e.enrollment_id, c.course_code, c.title, e.semester " +
        "FROM enrollments e " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "WHERE e.roll_number = ? " +
        "ORDER BY e.semester, c.course_code";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)
    ) {
        statement.setString(1, rollNumber);

        try (ResultSet result = statement.executeQuery()) {

            java.util.List<Integer> enrollmentIds =
                new java.util.ArrayList<>();

            java.util.List<String> courseNames =
                new java.util.ArrayList<>();
                System.out.println("\n===== ENROLLED COURSES =====");
            while (result.next()) {
                int id = result.getInt("enrollment_id");
                String title = result.getString("title");
                String code = result.getString("course_code");
                int semester = result.getInt("semester");

                enrollmentIds.add(id);
                courseNames.add(title);

                System.out.printf(
                    "%d. %s (%s) - Semester %d%n",
                    enrollmentIds.size(), title, code, semester
                );
            }

            if (enrollmentIds.isEmpty()) {
                System.out.println("No courses found for this student.");
                return;
            }

            java.util.Scanner input = new java.util.Scanner(System.in);

            System.out.print("Select course number: ");

            if (!input.hasNextInt()) {
                System.out.println("Invalid course number.");
                input.nextLine();
                return;
            }

            int choice = input.nextInt();
            input.nextLine();

            if (choice < 1 || choice > enrollmentIds.size()) {
                System.out.println("Invalid course selection.");
                return;
            }

            displayAssessmentMarks(enrollmentIds.get(choice - 1));
        }

    } catch (SQLException e) {
        System.out.println("Error retrieving assessment marks.");
        e.printStackTrace();
    }
}

public int findEnrollmentId(String rollNumber, String courseCode, int semester) {
    String sql =
            "SELECT enrollment_id FROM enrollments " +
            "WHERE roll_number = ? AND course_code = ? AND semester = ?";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)
    ) {
        statement.setString(1, rollNumber);
        statement.setString(2, courseCode);
        statement.setInt(3, semester);

        try (ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getInt("enrollment_id");
            }
        }

    } catch (SQLException e) {
        System.out.println("Error finding student enrollment.");
        e.printStackTrace();
    }

    return -1;
}



public void displayDepartmentPerformanceReport() throws SQLException {

    String sql =
        "SELECT s.department, s.roll_number, s.name, " +
        "e.enrollment_id, e.semester, e.classes_held, " +
        "e.classes_attended, e.medical_condonation_approved, " +
        "c.course_code, c.title, c.course_type, c.credits, " +
        "am.cia1, am.cia2, am.assignments, am.quizzes, " +
        "am.lab_evaluations, am.ese_marks, am.ese_absent " +
        "FROM enrollments e " +
        "JOIN students s ON e.roll_number = s.roll_number " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "LEFT JOIN assessment_marks am " +
        "ON e.enrollment_id = am.enrollment_id " +
        "ORDER BY s.department, s.roll_number, c.course_code";

    class DepartmentStats {
        int enrollments = 0;
        int passed = 0;
        int failed = 0;
        int pending = 0;
        int registeredCredits = 0;
        int earnedCredits = 0;
    }

    java.util.Map<String, DepartmentStats> departments =
        new java.util.TreeMap<>();

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement statement =
             connection.prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {

        while (rs.next()) {

            String department = rs.getString("department");

            DepartmentStats stats = departments.computeIfAbsent(
                department, key -> new DepartmentStats());

            stats.enrollments++;

            int credits = rs.getInt("credits");
            stats.registeredCredits += credits;

            int held = rs.getInt("classes_held");
            int attended = rs.getInt("classes_attended");

            double attendance = held > 0
                ? 100.0 * attended / held
                : 0.0;

            Object cia1 = rs.getObject("cia1");
            Object cia2 = rs.getObject("cia2");
            Object assignments = rs.getObject("assignments");
            Object quizzes = rs.getObject("quizzes");
            Object lab = rs.getObject("lab_evaluations");
            Object ese = rs.getObject("ese_marks");

            boolean absent = rs.getBoolean("ese_absent");

            boolean complete =
                cia1 != null &&
                cia2 != null &&
                assignments != null &&
                quizzes != null &&
                lab != null &&
                (ese != null || absent);

            if (!complete || held == 0) {
                stats.pending++;
                continue;
            }

            double ciaScore = calculateCIAScore(
                ((Number) cia1).doubleValue(),
                ((Number) cia2).doubleValue(),
                ((Number) assignments).doubleValue(),
                ((Number) quizzes).doubleValue(),
                ((Number) lab).doubleValue());

            double eseScore = ese == null
                ? 0.0
                : ((Number) ese).doubleValue();

            double total = calculateCourseResult(
                rs.getString("course_type"),
                ciaScore,
                eseScore);

            String grade = calculateGrade(
                rs.getString("course_type"),
                total,
                eseScore,
                absent,
                attendance,
                rs.getBoolean("medical_condonation_approved"));

            if (grade.equals("CONDITIONAL") ||
                grade.equals("PENDING")) {
                stats.pending++;
            } else if (grade.equals("RA") ||
                       grade.equals("SA") ||
                       grade.equals("AB")) {
                stats.failed++;
            } else {
                stats.passed++;
                stats.earnedCredits += credits;
            }
        }
    }

    System.out.println();
    System.out.println(
        "==========================================================================");
    System.out.println(
        "                  KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
    System.out.println(
        "                          PERUNDURAI, ERODE");
    System.out.println(
        "             DEPARTMENT-WISE CLASS PERFORMANCE REPORT");
    System.out.println(
        "                         REGULATION 2024");
    System.out.println(
        "==========================================================================");

    if (departments.isEmpty()) {
        System.out.println("No enrollment records found.");
        return;
    }

    System.out.printf(
        "%-12s %8s %8s %8s %8s %10s %10s%n",
        "Department", "Enrolled", "Passed", "Failed",
        "Pending", "Pass %", "Earned Cr");

    System.out.println(
        "--------------------------------------------------------------------------");

    int totalEnrollments = 0;
    int totalPassed = 0;
    int totalFailed = 0;
    int totalPending = 0;
    int totalRegisteredCredits = 0;
    int totalEarnedCredits = 0;

    for (java.util.Map.Entry<String, DepartmentStats> entry
            : departments.entrySet()) {

        String department = entry.getKey();
        DepartmentStats s = entry.getValue();

        int finalized = s.passed + s.failed;

        double passPercentage = finalized > 0
            ? 100.0 * s.passed / finalized
            : 0.0;

        System.out.printf(
            "%-12s %8d %8d %8d %8d %9.2f%% %10d%n",
            department,
            s.enrollments,
            s.passed,
            s.failed,
            s.pending,
            passPercentage,
            s.earnedCredits);

        totalEnrollments += s.enrollments;
        totalPassed += s.passed;
        totalFailed += s.failed;
        totalPending += s.pending;
        totalRegisteredCredits += s.registeredCredits;
        totalEarnedCredits += s.earnedCredits;
    }

    int finalizedTotal = totalPassed + totalFailed;

    double overallPassPercentage = finalizedTotal > 0
        ? 100.0 * totalPassed / finalizedTotal
        : 0.0;

    System.out.println(
        "--------------------------------------------------------------------------");

    System.out.printf(
        "%-12s %8d %8d %8d %8d %9.2f%% %10d%n",
        "OVERALL",
        totalEnrollments,
        totalPassed,
        totalFailed,
        totalPending,
        overallPassPercentage,
        totalEarnedCredits);

    System.out.println(
        "==========================================================================");

    System.out.printf(
        "Total Registered Credits : %d%n", totalRegisteredCredits);

    System.out.printf(
        "Total Earned Credits     : %d%n", totalEarnedCredits);

    System.out.printf(
        "Finalized Results        : %d%n", finalizedTotal);

    System.out.printf(
        "Pending Results          : %d%n", totalPending);

    System.out.printf(
        "Overall Pass Percentage  : %.2f%%%n",
        overallPassPercentage);

    System.out.println(
        "Note: Pass percentage is based only on finalized results.");

    System.out.println(
        "==========================================================================");
    System.out.println("RESULT CLASSIFICATION");
    System.out.println("PASS : Student passed the course requirements");
    System.out.println("RA   : Failed the configured examination/pass requirements");
    System.out.println("SA   : Shortage of attendance");
    System.out.println("AB   : Absent for the ESE");
    System.out.println("PENDING : Marks, attendance or condonation requires review");
    System.out.println(
        "==========================================================================");
}

}
