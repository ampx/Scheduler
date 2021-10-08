package scheduler.executors;

import org.springframework.jdbc.core.JdbcTemplate;
import scheduler.util.table.model.Table;

import javax.naming.ConfigurationException;
import java.sql.*;
import java.util.*;

public class PreparedJdbcExecutor extends Executor{

    protected JdbcTemplate jdbcTemplateObject;
    PreparedStatement statement;
    String preparedSql = null;
    List<Param> entries = new ArrayList<>();
    String url = null;
    Properties properties = null;

    public PreparedJdbcExecutor(HashMap<String, Object> config) throws Exception {
        super(config);

        if (config.containsKey("params") && config.get("params") instanceof ArrayList) {
            for (HashMap param: (ArrayList<HashMap>)config.get("params")) {
                if (param.containsKey("name") && param.containsKey("type")) {
                    entries.add(new Param((String)param.get("name"), (String)param.get("type")));
                }
            }
        }
        if (config.containsKey("url")) {
            url = (String) config.get("url");
        } else {
            throw new ConfigurationException();
        }
        if (config.containsKey("properties")) {
            properties = new Properties();
            for (String propName: ((Map<String, Object>)config.get("properties")).keySet()) {
                properties.put(propName, ((Map<String, Object>)config.get("properties")).get(propName));
            }
        }
        //test connection
        Connection connection =  DriverManager.getConnection(url, properties);
        connection.close();
        if (config.containsKey("preparedSql")) {
            preparedSql = (String) config.get("preparedSql");
        }
    }

    @Override
    public Table execute(HashMap arguments, String cacheName) {
        Connection connection = null;
        Table table = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        try {
            connection = DriverManager.getConnection(url, properties);
            preparedStatement = prepareStatement(connection, arguments);

            Map paramsArg = (Map) arguments.get("params");
            if (paramsArg != null) {
                int i = 1;
                for (Param entry: entries){
                    preparedStatement.setObject(i++, paramsArg.get(entry.getName()));
                }
            }
        } catch (Exception e) {
            //setup issue
        }

        try {
            result = preparedStatement.executeQuery();

            ResultSetMetaData rsmd = result.getMetaData();
            int columnCount = rsmd.getColumnCount();
            Table.Column[] headers = new Table.Column[columnCount];
            table = new Table();
            for (int i = 1; i <= columnCount; i++) {
                String name = rsmd.getColumnName(i);
                Integer sqlType = rsmd.getColumnType(i);
                if ((sqlType >= Types.TINYINT && sqlType <= Types.BIGINT)
                        || (sqlType >= Types.NUMERIC && sqlType <= Types.DOUBLE)) {
                    headers[i - 1] = table.createNumericColumn(name);
                } else if (sqlType == Types.TIMESTAMP || sqlType == Types.DATE) {
                    headers[i - 1] = table.createTimestampColumn(name);
                } else {
                    headers[i - 1] = table.createStringColumn(name);
                }
            }


            while(result.next()) {
                Object[] row = new Object[columnCount];
                int thisColumn = 1;
                for (Table.Column column: table.getColumns()) {
                    if (column.isNumeric()) {
                        row[thisColumn++] = result.getDouble(column.text);
                    }else if (column.isString()) {
                        row[thisColumn++] = result.getString(column.text);
                    } else if (column.isTime()) {
                        row[thisColumn++] = result.getTimestamp(column.text).getNanos()/1000000;
                    }
                }
                table.addRow(row);
            }

            return table;
        } catch (Exception e) {
            //processing results issues
        }
        try {
            result.close();
        } catch (Exception e) {};
        try {
            statement.close();
        } catch (Exception e) {};
        try {
            connection.close();
        } catch (Exception e) {};
        return table;
    }

    protected PreparedStatement prepareStatement(Connection connection, HashMap arguments) throws Exception{

        PreparedStatement preparedStatement =
                connection.prepareStatement(preparedSql);

        Map paramsArg = (Map) arguments.get("params");
        if (paramsArg != null) {
            int i = 1;
            for (Param entry: entries){
                preparedStatement.setObject(i++, paramsArg.get(entry.getName()));
            }
        }
        return preparedStatement;
    }

    private class Param{
        String name;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        String type;
        public Param(String name, String type){
            this.name = name;
            this.type = type;
        }
    }
}
