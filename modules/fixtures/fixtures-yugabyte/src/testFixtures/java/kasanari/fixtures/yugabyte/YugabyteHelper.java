package kasanari.fixtures.yugabyte;

import java.sql.DriverManager;
import java.sql.SQLException;

public class YugabyteHelper {
    private final YugabyteFixtureContainer yugabyte;

    public YugabyteHelper(YugabyteFixtureContainer yugabyte) {
        this.yugabyte = yugabyte;
    }

    public void truncateTable(String table) {
        var sql = "TRUNCATE TABLE " + table + " CASCADE";
        try (var connection = DriverManager.getConnection(
                yugabyte.jdbcUrl(),
                yugabyte.username(),
                yugabyte.password()
        ); var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to truncate table " + table, e);
        }
    }
}
