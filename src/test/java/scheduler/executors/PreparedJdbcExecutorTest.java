package scheduler.executors;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PreparedJdbcExecutorTest {

    String url = "jdbc:sqlite:./jdbcExecutorTest.db";

    @Test
    public void preparedJdbcExecutorTest() throws Exception {
        HashMap args = new HashMap();
        args.put("url", url);
        //args.put("properties", new HashMap() {{put();}});
        args.put("preparedSql", "");
        PreparedJdbcExecutor executor = new PreparedJdbcExecutor(args);

    }

    public void prepareSourceJdbcData() throws Exception{
        table.put("numeric_byte", new Byte((byte)0));
        table.put("numeric_short", new Short((short)1));
        table.put("numeric_int", new Integer(2));
        table.put("numeric_long", new Long(3));
        table.put("numeric_float", new Float(4));
        table.put("numeric_double", new Double(5));
        table.put("date_date", "2021-12-19");
        table.put("date_datetime", "2021-12-19 12:02:59");
        table.put("text_varchar", "varchar");
        table.put("text_text", "text");

        String create_table_sql = "create table " + tableName + "(" +
                "numeric_byte TINYINT," +
                "numeric_short SMALLINT," +
                "numeric_int INT," +
                "numeric_long BIGINT," +
                "numeric_float FLOAT," +
                "numeric_double DOUBLE," +
                "date_date DATE," +
                "date_datetime DATETIME," +
                "text_varchar VARCHAR(256)," +
                "text_text TEXT" +
                ");";

        Boolean first = true;
        String insertKeysStr = "";
        String insertValuesStr = "";
        for (String key : (Set<String>)table.keySet()) {
            if (first) {
                insertKeysStr += key;
                insertValuesStr += table.get(key);
                first = false;
            } else {
                insertKeysStr += "," + key;
                insertValuesStr += "," + table.get(key);
            }
        }

        String insertSql = "insert into " + tableName + " (" + insertKeysStr + ") values (" + insertValuesStr + ")";
        Statement stmt = null;
        Connection conn = null;
        conn = DriverManager.getConnection(url);
        stmt = conn.createStatement();
        stmt.setQueryTimeout(1000);
        stmt.executeUpdate(create_table_sql);
        stmt.executeUpdate(insertSql);
        stmt.executeUpdate(insertSql);
        stmt.close();
        conn.close();
    }

    HashMap table = new HashMap();
    String tableName = "tableName";

}