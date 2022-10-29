import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.testng.annotations.Test;


public class TestSQLite {

  @Test
  public void test1() throws SQLException {
    Connection connection = DriverManager.getConnection("jdbc:sqlite:/tmp/test.db");
    Statement statement = connection.createStatement();

    statement.executeUpdate("drop table person");
    statement.executeUpdate("create table person (id integer, name string)");
    for (int i = 0; i < 10; ++ i) {
      statement.executeUpdate(String.format("insert into person values(%d, 'tom%d')", i, i));
    }

    ResultSet results = statement.executeQuery("select * from person");
    int row = 0;
    while (results.next()) {
      System.out.printf("record %d: name=%s, id=%d\n",
          row, results.getString("name"), results.getInt("id"));
      row++;
    }
  }
}
