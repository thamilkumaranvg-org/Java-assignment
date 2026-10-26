
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
        "SELECT e.enrollment_id, e.roll_number, s.name, " +"e.course_code, c.title, e.classes_held, " + "e.classes_attended, " +
        "CASE " + "WHEN e.classes_held = 0 THEN NULL " + "ELSE ROUND(100.0 * e.classes_attended / " +"e.classes_held, 2) " +
        "END AS attendance_percentage " + "FROM enrollments e " + "JOIN students s ON e.roll_number = s.roll_number " +
        "JOIN courses c ON e.course_code = c.course_code " +
        "ORDER BY e.enrollment_id";

    try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()
    ) {
        while (resultSet.next()) {

            System.out.println(
                "Enrollment ID: " +
                resultSet.getInt("enrollment_id")
            );
            System.out.println(
                "Student: " + resultSet.getString("name")
            );
            System.out.println(
                "Roll Number: " +
                resultSet.getString("roll_number")
            );
            System.out.println(
                "Course: " + resultSet.getString("title")
            );

            double attendance =
                resultSet.getDouble("attendance_percentage");

            if (resultSet.wasNull()) {
                System.out.println("Attendance: Not recorded");
                System.out.println("Status: Pending");
            } else {
                System.out.printf(
                    "Attendance: %.2f%%%n", attendance
                );

                if (attendance >= 75.0) {
                    System.out.println("Status: Eligible");
                } else if (attendance >= 65.0) {
                    System.out.println(
                        "Status: Conditional - Medical condonation required"
                    );
                } else {
                    System.out.println(
                        "Status: SA - Barred from ESE"
                    );
                }
            }

            System.out.println("----------------------------");
        }

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

        try (ResultSet result = statement.executeQuery()) {

            boolean found = false;
            System.out.println("\n======================================");
            System.out.println("          STUDENT MARK SHEET");
            System.out.println("======================================");
            


            while (result.next()) {
                     if (!found) {
                    found = true;

                    System.out.println("Name       : " + result.getString("name"));
                    System.out.println("Roll Number: " + result.getString("roll_number"));
                    System.out.println("Department : " + result.getString("department"));
                    System.out.println("Semester   : " + result.getInt("semester"));
                    System.out.println("--------------------------------------");
                }
                System.out.println("\nCourse: " +
                    result.getString("title"));
                System.out.println("Course Code: " +
                    result.getString("course_code"));
                System.out.println("Credits: " +
                    result.getInt("credits"));
                System.out.println("Course Type: " +
                    result.getString("course_type"));

                System.out.println("\n--- Assessment Marks ---");
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

                Object ese = result.getObject("ese_marks");

                System.out.println("ESE: " +
                    (result.getBoolean("ese_absent")
                        ? "AB"
                        : ese == null ? "Pending" : ese));

                int held = result.getInt("classes_held");
                int attended = result.getInt("classes_attended");

                System.out.println("\n--- Attendance ---");

                if (held == 0) {
                    System.out.println("Attendance: Pending");
                } else {
                    double attendance =
                        100.0 * attended / held;

                    System.out.printf(
                        "Attendance: %.2f%%%n", attendance);

                    if (attendance >= 75) {
                        System.out.println("Status: Eligible");
                    } else if (attendance >= 65) {
                        System.out.println(
                            result.getBoolean(
                                "medical_condonation_approved")
                                ? "Status: Conditional - Approved"
                                : "Status: Conditional - Awaiting condonation");
                    } else {
                        System.out.println("Status: SA - Barred from ESE");
                    }
                }

                System.out.println("\n--- Course Result ---");

                Object[] marks = {
                    result.getObject("cia1"),
                    result.getObject("cia2"),
                    result.getObject("assignments"),
                    result.getObject("quizzes"),
                    result.getObject("lab_evaluations")
                };

                boolean complete = true;

                for (Object mark : marks) {
                    if (mark == null) {
                        complete = false;
                        break;
                    }
                }

                if (!complete || (ese == null &&
                        !result.getBoolean("ese_absent"))) {
                    System.out.println(
                        "Result: Pending - assessment marks incomplete");
                } else if (held == 0) {
                    System.out.println(
                        "Result: Pending - attendance not recorded");
                } else {
                    double ciaScore = calculateCIAScore(
                        ((Number) marks[0]).doubleValue(),
                        ((Number) marks[1]).doubleValue(),
                        ((Number) marks[2]).doubleValue(),
                        ((Number) marks[3]).doubleValue(),
                        ((Number) marks[4]).doubleValue()
                    );

                    double eseScore = ese == null
                        ? 0.0
                        : ((Number) ese).doubleValue();

                    double total = calculateCourseResult(
                        result.getString("course_type"),
                        ciaScore,
                        eseScore
                    );
                    double ciaWeight;
                    double eseWeight;

                    switch (result.getString("course_type").toUpperCase()) {
                        case "THEORY":
                        ciaWeight = 40  ;
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
                        throw new IllegalArgumentException("Invalid course type");
                    }

System.out.printf("CIA Contribution: %.2f%n",
        ciaScore * ciaWeight / 100.0);
System.out.printf("ESE Contribution: %.2f%n",
        eseScore * eseWeight / 100.0);

                    double attendance =
                        100.0 * attended / held;

                    String grade = calculateGrade(
                        result.getString("course_type"),
                        total,
                        eseScore,
                        result.getBoolean("ese_absent"),
                        attendance,
                        result.getBoolean(
                            "medical_condonation_approved")
                    );

                    if (grade.equals("CONDITIONAL")) {
                        System.out.println(
                            "Result: Pending - medical condonation required");
                    } else {
                        System.out.printf(
                            "CIA Score: %.2f%n", ciaScore);
                        System.out.printf(
                            "Weighted Total: %.2f%n", total);
                        System.out.println("Grade: " + grade);
                        System.out.println(
                            "Grade Point: " + getGradePoint(grade));
                    }
                }

                System.out.println("======================================");
            }

            if (!found) {
                System.out.println(
                    "No enrolled courses found for this student and semester.");
            }
        }
    }
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

}
