package scheduler.util.table.dao;

import scheduler.util.table.model.Table;
import scheduler.util.time.logic.TimeUtil;
import scheduler.util.time.model.Time;

import java.util.List;

public abstract class TableSource {

    public abstract Table getTable(String tableName);

    public abstract boolean insertTable(Table table, String tableName);

    public abstract boolean deleteTable(String table);

    public abstract List<String> getTableList();

    public boolean cleanupTables(Time cutoffDate, String pattern) {
        boolean success = false;
        try {
            List<String> tableList = getTableList();
            List<String> databaseStaleList = TimeUtil.getStaleRecords(cutoffDate, pattern, tableList);
            for (String table : databaseStaleList) {
                deleteTable(table);
            }
            success = true;
        } catch (Exception e){
            success = false;
        }
        return success;
    }


}
