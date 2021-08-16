package scheduler.logic;

import scheduler.model.Request;
import scheduler.util.table.model.Table;

import java.util.HashMap;
import java.util.List;

public class SystemObjectTableConverter {
    public static Table makeStatusTable(List<Request> requests) {
        if (requests != null) {
            Table table = new Table(4);
            table.setHeaders(new String[]{"Submit time", "Completion Time", "Status", "Notes"});
            for (Request request : requests) {
                String[] row = new String[4];
                String starttime = "";
                String endtime = "";
                String status = "";
                String message = "";
                if (request.getRequestTime() != null) {
                    starttime = request.getRequestTime().mysqlString();
                }
                row[0] = starttime;
                if (request.getCompletionTime() != null) {
                    endtime = request.getCompletionTime().mysqlString();
                }
                row[1] = endtime;
                if (request.getStatusString() != null) {
                    status = request.getStatusString();
                }
                row[2] = status;
                if (request.getExecutorMessage() != null) {
                    message = request.getExecutorMessage();
                }
                row[3] = message;
                table.addRow(row);
            }
            return table;
        }
        return null;
    }
}
