
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO {

    public void displayAllStudents() {
        String sql = "SELECT * FROM students";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
        ) {
            while (resultSet.next()) {
                String rollNumber = resultSet.getString("roll_number");
                String name = resultSet.getString("name");
                String department = resultSet.getString("department");
                int semester = resultSet.getInt("current_semester");
                System.out.println("Roll Number: " + rollNumber);
                System.out.println("Name: " + name);
                System.out.println("Department: " + department);
                System.out.println("Current Semester: " + semester);
                System.out.println("----------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving students.");
            e.printStackTrace();
        }
    }

    public void addStudent(String rollNumber, String name, String department, int semester) {

        String sql = "INSERT INTO students " + "(roll_number, name, department, current_semester) " + "VALUES (?, ?, ?, ?)";
        try (
        Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, rollNumber);
            statement.setString(2, name);
            statement.setString(3, department);
            statement.setInt(4, semester);
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
            System.out.println("Student added successfully!");
            }

        } catch (SQLException e) {
            System.out.println("Error adding student.");
            e.printStackTrace();
        }
    }
    public void updateStudent(String rollnumber, String name, String department, int semester){
        String sql = "UPDATE students SET name = ?, department = ?, " + "current_semester = ? WHERE roll_number = ?";
        try(Connection connect = DBConnection.getConnection(); PreparedStatement stat = connect.prepareStatement(sql) ){
            stat.setString(1, name);
            stat.setString(2, department);
            stat.setInt(3, semester);
            stat.setString(4, rollnumber);
            int rowsUpdated = stat.executeUpdate();
            if(rowsUpdated > 0)System.out.println("Student Updated successfully");
            else System.out.println("Student not found");
        }catch(SQLException e){
             System.out.println("Error updating student.");
             e.printStackTrace();
        }
    }

}
