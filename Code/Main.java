
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final StudentDAO studentDAO = new StudentDAO();
    private static final CourseDAO courseDAO = new CourseDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();

    
public static void main(String[] args) {
    int choice;

    do {
        System.out.println("\n==========================================");
        System.out.println("     KEC ACADEMIC MANAGEMENT SYSTEM");
        System.out.println("==========================================");
        System.out.println("1. Student Management");
        System.out.println("2. Course Management");
        System.out.println("3. Enrollment & Attendance");
        System.out.println("4. Assessment Management");
        System.out.println("5. Academic Reports");
        System.out.println("0. Exit");
        System.out.println("==========================================");
        System.out.print("Enter your choice: ");

        choice = readInt();

        try {
            switch (choice) {
                case 1:
                    studentMenu();
                    break;
                case 2:
                    courseMenu();
                    break;
                case 3:
                    enrollmentMenu();
                    break;
                case 4:
                    assessmentMenu();
                    break;
                case 5:
                    reportsMenu();
                    break;
                case 0:
                    System.out.println("Thank you for using KEC Academic Management System.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Operation failed: " + e.getMessage());
        }

    } while (choice != 0);

    sc.close();
}

private static void studentMenu() {
    int choice;
    do {
        System.out.println("\n--- STUDENT MANAGEMENT ---");
        System.out.println("1. Display All Students");
        System.out.println("2. Add Student");
        System.out.println("3. Update Student");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");

        choice = readInt();

        switch (choice) {
            case 1:
                studentDAO.displayAllStudents();
                break;
            case 2:
                addStudent();
                break;
            case 3:
                updateStudent();
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    } while (choice != 0);
}

private static void courseMenu() {
    int choice;
    do {
        System.out.println("\n--- COURSE MANAGEMENT ---");
        System.out.println("1. Display All Courses");
        System.out.println("2. Add Course");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");

        choice = readInt();

        switch (choice) {
            case 1:
                courseDAO.displayAllCourses();
                break;
            case 2:
                addCourse();
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    } while (choice != 0);
}

private static void enrollmentMenu() {
    int choice;
    do {
        System.out.println("\n--- ENROLLMENT & ATTENDANCE ---");
        System.out.println("1. Enroll Student");
        System.out.println("2. Display Enrollments");
        System.out.println("3. Update Attendance");
        System.out.println("4. Attendance Eligibility Report");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");

        choice = readInt();

        switch (choice) {
            case 1:
                enrollStudent();
                break;
            case 2:
                academicDAO.displayEnrollments();
                break;
            case 3:
                updateAttendance();
                break;
            case 4:
                academicDAO.displayAttendanceReport();
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    } while (choice != 0);
}

private static void assessmentMenu() {
    int choice;
    do {
        System.out.println("\n--- ASSESSMENT MANAGEMENT ---");
        System.out.println("1. Enter Assessment Marks");
        System.out.println("2. View Assessment Marks");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");

        choice = readInt();

        switch (choice) {
            case 1:
                enterAssessmentMarks();
                break;
            case 2:
                System.out.print("Enter student roll number: ");
                String roll = sc.nextLine().trim();
                academicDAO.displayAssessmentMarksByRollNumber(roll);
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    } while (choice != 0);
}


private static void reportsMenu() {
    int choice;

    do {
        System.out.println("\n--- ACADEMIC REPORTS ---");
        System.out.println("1. Generate Student Mark Sheet");
        System.out.println("2. Calculate SGPA");
        System.out.println("3. Department Performance Report");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter choice: ");

        choice = readInt();

        try {
            switch (choice) {
                case 1:
                    System.out.print("Enter roll number: ");
                    String markRoll = sc.nextLine().trim();

                    System.out.print("Enter semester: ");
                    int markSemester = readInt();

                    academicDAO.displayStudentMarkSheet(
                        markRoll, markSemester
                    );
                    break;

                case 2:
                    System.out.print("Enter roll number: ");
                    String sgpaRoll = sc.nextLine().trim();

                    System.out.print("Enter semester: ");
                    int sgpaSemester = readInt();

                    double sgpa = academicDAO.calculateSGPA(
                        sgpaRoll, sgpaSemester
                    );

                    System.out.printf("SGPA: %.2f%n", sgpa);
                    System.out.printf(
                        "Equivalent Percentage: %.2f%%%n",
                        sgpa * 10
                    );
                    break;

                case 3:
                    academicDAO.displayDepartmentPerformanceReport();
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println(
                "Unable to generate report: " + e.getMessage()
            );
        }

    } while (choice != 0);
}



    private static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid integer: ");
            sc.next();
        }

        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static void addStudent() {
        System.out.print("Enter roll number: ");
        String roll = sc.nextLine().trim();

        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter department: ");
        String department = sc.nextLine().trim();

        System.out.print("Enter current semester: ");
        int semester = readInt();

        studentDAO.addStudent(roll, name, department, semester);
    }

    private static void updateStudent() {
        System.out.print("Enter roll number: ");
        String roll = sc.nextLine().trim();

        System.out.print("Enter new name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter new department: ");
        String department = sc.nextLine().trim();

        System.out.print("Enter current semester: ");
        int semester = readInt();

        studentDAO.updateStudent(roll, name, department, semester);
    }

    private static void addCourse() {
        System.out.print("Enter course code: ");
        String code = sc.nextLine().trim();

        System.out.print("Enter course title: ");
        String title = sc.nextLine().trim();

        System.out.print("Enter credits: ");
        int credits = readInt();

        System.out.print("Enter course type (THEORY/PRACTICAL/EMBEDDED): ");
        String type = sc.nextLine().trim().toUpperCase();

        courseDAO.addCourse(code, title, credits, type);
    }

    private static void enrollStudent() {
        System.out.print("Enter student roll number: ");
        String roll = sc.nextLine().trim();

        System.out.print("Enter course code: ");
        String code = sc.nextLine().trim();

        System.out.print("Enter semester: ");
        int semester = readInt();

        academicDAO.enrollStudent(roll, code, semester);
    }

    private static void updateAttendance() {
        System.out.print("Enter enrollment ID: ");
        int enrollmentId = readInt();

        System.out.print("Enter total classes held: ");
        int held = readInt();

        System.out.print("Enter classes attended: ");
        int attended = readInt();

        academicDAO.updateAttendance(enrollmentId, held, attended);
    }

    
private static void enterAssessmentMarks() {
    System.out.println("\n===== ASSESSMENT MARKS ENTRY =====");
    System.out.println("1. Phase 1 - Mid-semester CIA");
    System.out.println("2. Phase 2 - Internal Assessment");
    System.out.println("3. Phase 3 - End-semester Examination");
    System.out.println("0. Return to Main Menu");
    System.out.print("Choose assessment phase: ");

    int phase = readInt();

    if (phase == 0) {
        return;
    }

    if (phase < 1 || phase > 3) {
        System.out.println("Invalid phase selection.");
        return;
    }

    System.out.print("Enter student roll number: ");
    String rollNumber = sc.nextLine().trim();

    System.out.print("Enter course code: ");
    String courseCode = sc.nextLine().trim();

    System.out.print("Enter semester: ");
    int semester = readInt();

    if (rollNumber.isEmpty() || courseCode.isEmpty() || semester <= 0) {
        System.out.println("Please enter valid student and course details.");
        return;
    }

    System.out.println("\nSelected student: " + rollNumber);
    System.out.println("Selected course: " + courseCode);
    System.out.println("Semester: " + semester);

    Double cia1 = null;
    Double cia2 = null;
    Double assignments = null;
    Double quizzes = null;
    Double lab = null;
    Double ese = null;
    boolean absent = false;

    switch (phase) {
        case 1:
            System.out.println("\n--- Phase 1: Mid-semester CIA ---");
            cia1 = readOptionalMark("CIA Test 1");
            cia2 = readOptionalMark("CIA Test 2");
            break;

        case 2:
            System.out.println("\n--- Phase 2: Internal Assessment ---");
            assignments = readOptionalMark("Assignment");
            quizzes = readOptionalMark("Quiz");
            lab = readOptionalMark("Lab evaluation");
            break;

        case 3:
            System.out.println("\n--- Phase 3: End-semester Examination ---");
            System.out.print("Was the student absent for ESE? (yes/no): ");
            absent = sc.nextLine().trim().equalsIgnoreCase("yes");

            if (!absent) {
                ese = readOptionalMark("ESE");
            }
            break;
    }

    int enrollmentId =
            academicDAO.findEnrollmentId(rollNumber, courseCode, semester);

    if (enrollmentId == -1) {
        System.out.println(
                "No matching enrollment found. Check the roll number, "
                + "course code and semester."
        );
        return;
    }

    academicDAO.updateAssessmentMarks(
            enrollmentId, cia1, cia2, assignments,
            quizzes, lab, ese, absent
    );
}

    private static Double readOptionalMark(String label) {
        System.out.print("Enter " + label + " marks (or -1 to skip): ");

        double mark = sc.nextDouble();
        sc.nextLine();

        if (mark == -1) {
            return null;
        }

        if (mark < 0 || mark > 100) {
            System.out.println("Marks must be between 0 and 100, or -1 to skip.");
            return readOptionalMark(label);
        }

        return mark;
    }
}
