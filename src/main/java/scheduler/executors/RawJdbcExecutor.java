package scheduler.executors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;


public class RawJdbcExecutor extends PreparedJdbcExecutor{
    public RawJdbcExecutor(HashMap<String, Object> config) throws Exception {
        super(config);
    }

    protected PreparedStatement prepareStatement(Connection connection, HashMap arguments) throws Exception{

        String sql = (String) arguments.get("sql");

        PreparedStatement preparedStatement =
                connection.prepareStatement(sql);

        return preparedStatement;
    }

}
