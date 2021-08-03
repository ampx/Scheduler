package scheduler.util.table.dao;

import scheduler.util.table.model.Table;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

public class SqliteTableSource extends TableSource{

    String path = "./";
    String dbName = "scheduler.db";
    Integer timeoutMillis = 200;
    Integer batch_size = 500;

    public SqliteTableSource(String path) throws ConfigurationException {
        this.path = path;
        setupDb();
    }

    public SqliteTableSource() throws ConfigurationException {
        setupDb();
    }

    private void setupDb() throws ConfigurationException {
        try {
            if (!Files.exists(Paths.get(this.path))) {
                Files.createDirectory(Paths.get(this.path));
            }
            if (!Files.isWritable(Paths.get(this.path))) {
                throw new ConfigurationException("Initial configuration failed... bookmark path is not writable");
            }
        } catch (Exception e) {
            throw new ConfigurationException("Initial configuration failed... Check configuration. Caused by:" + e);
        }
    }

    @Override
    public Table getTable(String tableName) {
        Table table = null;
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + dbName;
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            String sql = "SELECT * FROM " + tableName;
            ResultSet rs = stmt.executeQuery(sql);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            Table.Column[] headers = new Table.Column[columnCount];
            table = new Table();
            for (int i = 1; i <= columnCount; i++) {
                String name = rsmd.getColumnName(i);
                if (rsmd.getColumnType(i) == Types.DOUBLE) {
                    headers[i - 1] = table.createNumericColumn(name);
                } else if (rsmd.getColumnType(i) == Types.BIGINT) {
                    headers[i - 1] = table.createTimestampColumn(name);
                } else {
                    headers[i - 1] = table.createStringColumn(name);
                }
            }
            table.columns = headers;
            List<Object[]> rows = new ArrayList<>(100);
            table.rows = rows;
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                rows.add(row);
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return table;
    }

    @Override
    public boolean insertTable(Table table, String tableName) {
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + dbName;
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try{
            conn.setAutoCommit(false);
            stmt.executeUpdate(createStatement(table, tableName));
            PreparedStatement pstmt = conn.prepareStatement(insertStatement(table, tableName));
            pstmt.setQueryTimeout(timeoutMillis);
            int count = 0;
            for(Object[] row : table.rows){
                for (int i = 0 ; i < row.length; i++) {
                    pstmt.setObject(i + 1, row[i]);
                }
                pstmt.addBatch();
                count++;
                if(count % batch_size == 0){
                    pstmt.executeBatch();
                    conn.commit();
                }
            }
            pstmt.executeBatch();
            conn.commit();
            pstmt.close();
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException internal_e) {
                internal_e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean deleteTable(String tableName) {
        Statement stmt = null;
        Connection conn = null;
        String sql = "DROP TABLE " + tableName;
        try {
            String url = "jdbc:sqlite:" + path + "/" + dbName;
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException internal_e) {
                internal_e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public List<String> getTableList() {
        List<String> tables = new ArrayList<>();
        Statement stmt = null;
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + path + "/" + dbName;
            conn = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            stmt = conn.createStatement();
            stmt.setQueryTimeout(timeoutMillis);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            String sql = "SELECT name FROM sqlite_master WHERE type='table'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    private String createStatement(Table table, String tableName){
        Table.Column[] headers = table.getColumns();
        String sqlCreate = "CREATE TABLE " + tableName + " (";
        String rowDescription = "";
        for (Table.Column header: headers) {
            if (rowDescription.length() > 0) rowDescription += ",";
            if (header.type == Table.ColumnType.number) {
                rowDescription += header.text + " double";
            } else if (header.type == Table.ColumnType.time) {
                rowDescription += header.text + " bigint";
            } else {
                rowDescription += header.text + " varchar(255)";
            }
        }
        sqlCreate += rowDescription + ")";
        return sqlCreate;
    }

    private String insertStatement(Table table, String tableName) {
        Table.Column[] headers = table.getColumns();
        int columnsCounter = 0;
        String headersListStr = "";
        String valuesTemplateListStr = "";
        for (Table.Column header: headers) {
            if (columnsCounter > 0) {
                valuesTemplateListStr += ",";
                headersListStr += ",";
            }
            valuesTemplateListStr += "?";
            String headerStr = header.text;
            if (headerStr != null && headerStr != "") {
                headersListStr += headerStr;
            } else {
                headersListStr += "row"+columnsCounter;
            }
            columnsCounter++;
        }
        String sql = "INSERT INTO " + tableName +
                " (" + headersListStr +") " + " VALUES (" + valuesTemplateListStr + ")";
        return sql;
    }

    public boolean deleteDb() {
        try {
            if (Files.exists(Paths.get(path + "/" + dbName))) {
                Files.delete(Paths.get(path + "/" + dbName ));
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
