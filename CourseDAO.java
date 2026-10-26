
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseDAO {

    public void addCourse(String courseCode, String title,
                          int credits, String courseType) {

        String sql = "INSERT INTO courses " +
                     "(course_code, title, credits, course_type) " +
                     "VALUES (?, ?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {
            statement.setString(1, courseCode);
            statement.setString(2, title);
            statement.setInt(3, credits);
            statement.setString(4, courseType.toUpperCase());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Course added successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Error adding course.");
            e.printStackTrace();
        }
    }

    public void displayAllCourses() {

        String sql = "SELECT * FROM courses ORDER BY course_code";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                System.out.println(
                    "Course Code: " +
                    resultSet.getString("course_code")
                );
                System.out.println(
                    "Title: " + resultSet.getString("title")
                );
                System.out.println(
                    "Credits: " + resultSet.getInt("credits")
                );
                System.out.println(
                    "Course Type: " +
                    resultSet.getString("course_type")
                );
                System.out.println("----------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving courses.");
            e.printStackTrace();
        }
    }
}
