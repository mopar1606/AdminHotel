import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel_db?useSSL=false&serverTimezone=UTC", "root", "");
            Statement stmt = cn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM recibo LIMIT 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(rsmd.getColumnName(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
