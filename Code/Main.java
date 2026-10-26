
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
            System.out.println(" KEC STUDENT ACADEMIC EVALUATION SYSTEM");
            System.out.println("==========================================");
            System.out.println("1. Display All Students");
            System.out.println("2. Add Student");
            System.out.println("3. Update Student");
            System.out.println("4. Display All Courses");
            System.out.println("5. Add Course");
            System.out.println("6. Enroll Student");
            System.out.println("7. Display Enrollments");
            System.out.println("8. Update Attendance");
            System.out.println("9. Display Attendance Report");
            System.out.println("10. Enter Assessment Marks");
            System.out.println("11. View Assessment Marks");
            System.out.println("12. Generate Student Mark Sheet");
            System.out.println("13. Calculate SGPA");
            System.out.println("0. Exit");
            System.out.println("==========================================");
            System.out.print("Enter your choice: ");

            choice = readInt();

            try {
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

                    case 4:
                        courseDAO.displayAllCourses();
                        break;

                    case 5:
                        addCourse();
                        break;

                    case 6:
                        enrollStudent();
                        break;

                    case 7:
                        academicDAO.displayEnrollments();
                        break;

                    case 8:
                        updateAttendance();
                        break;

                    case 9:
                        academicDAO.displayAttendanceReport();
                        break;

                    case 10:
                        enterAssessmentMarks();
                        break;

                    case 11:
                         System.out.print("Enter student roll number: ");
                        String assessmentRoll = sc.nextLine().trim();

                        academicDAO.displayAssessmentMarksByRollNumber(assessmentRoll);
                        break;
                    case 12:
                        System.out.print("Enter roll number: ");
                        String markSheetRoll = sc.nextLine().trim();

                        System.out.print("Enter semester: ");
                        int markSheetSemester = readInt();

                        academicDAO.displayStudentMarkSheet(
                            markSheetRoll, markSheetSemester
                        );
                        break;

                    case 13:
                        System.out.print("Enter roll number: ");
                        String sgpaRoll = sc.nextLine().trim();

                        System.out.print("Enter semester: ");
                        int sgpaSemester = readInt();

                        double sgpa = academicDAO.calculateSGPA(
                            sgpaRoll, sgpaSemester
                        );

                        System.out.printf("SGPA: %.2f%n", sgpa);
                        System.out.printf(
                            "Equivalent Percentage: %.2f%%%n", sgpa * 10
                        );
                        break;

                    case 0:
                        System.out.println("Exiting the system. Goodbye!");
                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }

            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }

        } while (choice != 0);

        sc.close();
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
        System.out.print("Enter enrollment ID: ");
        int enrollmentId = readInt();

        System.out.print("Is the student absent for ESE? (yes/no): ");
        boolean absent = sc.nextLine().trim().equalsIgnoreCase("yes");

        Double cia1 = readOptionalMark("CIA1");
        Double cia2 = readOptionalMark("CIA2");
        Double assignments = readOptionalMark("Assignments");
        Double quizzes = readOptionalMark("Quizzes");
        Double lab = readOptionalMark("Lab evaluations");

        Double ese = null;

        if (!absent) {
            ese = readOptionalMark("ESE");
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
