package scheduler.util.table.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import scheduler.util.table.logic.TableDeserializer;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = TableDeserializer.class)
public class Table {
    private Integer numColumns;
    public Column[] columns;
    public List<Object[]> rows;
    String type = "table";

    public Table(Integer numColumns){
        this.setNumColumns(numColumns);
        this.columns = new Column[numColumns];
        rows = new ArrayList<>();
    }

    public Table() {

    }

    public void setHeaders(String[] headers){
        for (int i = 0; i < headers.length; i++) {
            Column column = new Column();
            column.text = headers[i];
            columns[i] = column;
        }
    }

    public Column[] getColumns(){
        return columns;
    }

    public void addRow(Object[] row){
        rows.add(row);
    }

    public List<Object[]> getRows(){
        return rows;
    }

    public void setRows(List<Object[]> rows) {
        this.rows = rows;
    }

    public Integer getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(Integer numColumns) {
        this.numColumns = numColumns;
    }

    public class Column{
        public Column(){

        }
        public ColumnType type = ColumnType.string;
        public String text;
    }

    public enum ColumnType {
        string,
        number,
        time;
    }

    public Column createNumericColumn(String name) {
        Column column = new Column();
        column.text = name;
        column.type = ColumnType.number;
        return column;
    }

    public Column createTimestampColumn(String name) {
        Column column = new Column();
        column.text = name;
        column.type = ColumnType.time;
        return column;
    }

    public Column createStringColumn(String name) {
        Column column = new Column();
        column.text = name;
        column.type = ColumnType.string;
        return column;
    }

    public Column createColumn(String name, ColumnType type) {
        Column column = new Column();
        column.text = name;
        column.type = type;
        return column;
    }

}

