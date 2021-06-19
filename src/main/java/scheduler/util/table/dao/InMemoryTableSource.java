package scheduler.util.table.dao;

import scheduler.util.table.model.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTableSource extends TableSource {

    HashMap<String, Table> cache = new HashMap<>();

    @Override
    public Table getTable(String tableName) {
        return cache.get(tableName);
    }

    @Override
    public boolean insertTable(Table table, String tableName) {
        cache.put(tableName, table);
        return true;
    }

    @Override
    public boolean deleteTable(String table) {
        cache.remove(table);
        return true;
    }

    @Override
    public List<String> getTableList() {
        return new ArrayList<String>(cache.keySet());
    }

}
