package service;

import util.table.dao.JdbcTableSource;
import util.table.model.Table;
import util.time.model.Time;

public class CacheManager {
    JdbcTableSource jdbcQuery;

    public String getCacheTimestampPattern() {
        return cacheTimestampPattern;
    }

    public void setCacheTimestampPattern(String cacheTimestampPattern) {
        this.cacheTimestampPattern = cacheTimestampPattern;
    }

    public JdbcTableSource getJdbcQuery() {
        return jdbcQuery;
    }

    public void setJdbcQuery(JdbcTableSource jdbcQuery) {
        this.jdbcQuery = jdbcQuery;
    }

    String cacheTimestampPattern = "uuMMddHHmm";

    public Table getTable(String tableName){
        Table table = null;
        try {
            table = jdbcQuery.getTable(tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    public boolean cleanupCache(Time cutOffTime){
        return jdbcQuery.cleanupTables(cutOffTime, cacheTimestampPattern);
    }

    public Table makeStatusTable(String status) {
        Table table = new Table(1);
        table.setHeaders(new String[]{"Status"});
        table.addRow(new String[]{status});
        return table;
    }
}
