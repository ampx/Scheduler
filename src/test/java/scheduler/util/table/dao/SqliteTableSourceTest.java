package scheduler.util.table.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduler.util.table.model.Table;

import javax.naming.ConfigurationException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteTableSourceTest {

    SqliteTableSource tableSource;

    @BeforeEach
    public void init() throws ConfigurationException {
        new SqliteTableSource().deleteDb();
        tableSource = new SqliteTableSource();
    }

    public Table createTable() {
        Table table = new Table();
        Table.Column[] columns = new Table.Column[3];
        columns[0] = table.createStringColumn("stringColumn");
        columns[1] = table.createNumericColumn("numColumn");
        columns[2] = table.createTimestampColumn("timeColumn");

        table.columns = columns;

        Object[] row = new Object[3];
        row[0] = "stringValue";
        row[1] = 678;
        row[2] = 1450754160001L;
        List<Object[]> rows = new ArrayList<>();
        rows.add(row);
        table.rows = rows;
        return table;
    }

    @Test
    public void insertTableTest() {
        String tableName = "tableName";
        assertTrue(tableSource.insertTable(createTable(), tableName));
    }

    @Test
    public void readTableTest() {
        String tableName = "tableName";
        Table originalTable = createTable();
        tableSource.insertTable(originalTable, tableName);
        Table returnedTable = tableSource.getTable(tableName);

        for (int i = 0; i < 3; i++) {
            assertTrue(originalTable.columns[i].type.equals(returnedTable.columns[i].type));
            assertTrue(originalTable.columns[i].text.equals(returnedTable.columns[i].text));
        }
        Object[] originalRow = originalTable.getRows().get(0);
        Object[] returnedRow = returnedTable.getRows().get(0);
        assertTrue(originalRow[0].equals(returnedRow[0]));
        assertTrue(originalRow[1].equals(((Double)returnedRow[1]).intValue()));
        assertTrue(originalRow[2].equals(returnedRow[2]));
    }

    @Test
    public void getTableListTest() {
        String tableName = "tableName";
        assertTrue(tableSource.insertTable(createTable(), tableName));
        List<String> tables = tableSource.getTableList();
        assertTrue(tables.size() == 1);
        assertTrue(tables.get(0).equals(tableName));
    }

    @Test
    public void deleteTableTest() {
        String tableName = "tableName";
        assertTrue(tableSource.insertTable(createTable(), tableName));
        tableSource.deleteTable(tableName);
        List<String> tables = tableSource.getTableList();
        assertTrue(tables.size() == 0);
    }

    @AfterEach
    public void cleanup(){
        tableSource.deleteDb();
    }

}