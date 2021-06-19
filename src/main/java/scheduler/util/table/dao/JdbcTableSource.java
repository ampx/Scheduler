package scheduler.util.table.dao;

import scheduler.util.table.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class JdbcTableSource extends TableSource {

    protected JdbcTemplate jdbcTemplateObject;
    protected String tableName;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    private String databaseName;

    public Table getTable(String tableName) {
        String query = "select * from " + tableName;
        return getJdbcTemplateObject().query(query, (ResultSet rs) -> {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            String[] headers = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                String name = rsmd.getColumnName(i);
                headers[i - 1] = name;
            }
            Table table = new Table(columnCount);
            table.setHeaders(headers);
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(headers[i]);
                }
                table.addRow(row);
            }
            return table;
        });
    }

    public boolean insertTable(Table table, String tableName) {
        try {
            if (table != null) {
                String sqlCreate = "CREATE TABLE " + tableName + " (";
                String sqlInsert0 = "INSERT INTO EMP_ADDRESS(";
                Table.Column[] headers = table.getColumns();
                for (Table.Column header: headers) {
                    if (header.type == "number") {
                        sqlCreate += header.text + " double,";
                    } else if (header.type == "time") {
                        sqlCreate += header.text + " datetime,";
                    } else {
                        sqlCreate += header.text + " varchar(255),";
                    }
                    sqlInsert0 += header.text + ",";
                }
                sqlCreate = ");";
                getJdbcTemplateObject().execute(sqlCreate);
                sqlInsert0 += ") VALUES (";
                Statement statement = getJdbcTemplateObject().getDataSource().getConnection().createStatement();
                for (Object[] row: table.getRows()) {
                    String sqlInsertComplete = sqlInsert0;
                    for (Object obj: row) {
                        sqlInsertComplete += "'" + obj + "'";
                    }
                    sqlInsertComplete += ");";
                    statement.addBatch(sqlInsertComplete);
                }
                statement.executeBatch();
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public List<String> getPartitionList(String tableName){
        String query = "SELECT PARTITION_NAME FROM information_schema.partitions " +
                "WHERE TABLE_NAME = '" + tableName +"' AND PARTITION_NAME IS NOT NULL " +
                "ORDER BY PARTITION_NAME ASC";
        List<String> partitionList = getJdbcTemplateObject().query(query, new RowMapper<String>(){
            public String mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return rs.getString(1);
            }
        });
        return partitionList;
    }

    public boolean deletePartition(String tableName, String partitionName) {
        try {
            String sql = "ALTER TABLE " + tableName + " DROP PARTITION " + partitionName;
            getJdbcTemplateObject().execute(sql);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<String> getTableList(){
        String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = '" + databaseName + "'";
        List<String> tableList = getJdbcTemplateObject().query(query, new RowMapper<String>(){
            public String mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return rs.getString(1);
            }
        });
        return tableList;
    }

    public boolean deleteTable(String table) {
        try {
            String sql = "DROP TABLE " + table;
            getJdbcTemplateObject().execute(sql);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public JdbcTemplate getJdbcTemplateObject() {
        return jdbcTemplateObject;
    }

    public void setJdbcTemplateObject(JdbcTemplate jdbcTemplateObject) {
        this.jdbcTemplateObject = jdbcTemplateObject;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
