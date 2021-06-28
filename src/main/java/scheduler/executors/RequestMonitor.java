package scheduler.executors;

import scheduler.model.Request;
import scheduler.service.RequestManager;
import scheduler.util.table.model.Table;

import java.util.HashMap;

public class RequestMonitor extends Executor {

    RequestManager requestManager;

    public RequestMonitor(HashMap<String, Object> config, RequestManager requestManager) {
        super(config);
        this.requestManager = requestManager;
    }

    @Override
    public Table execute(HashMap arguments, boolean captureOutput) {
        String user = null;
        String source = null;
        String label = null;
        String target = null;
        HashMap args = null;
        if (arguments != null) {
            if (arguments.containsKey("user")) {
                user = ((String)arguments.get("user"));
            }
            if (arguments.containsKey("source")) {
                source = ((String)arguments.get("source"));
            }
            if (arguments.containsKey("label")) {
                label = ((String)arguments.get("label"));
            }
            if (arguments.containsKey("target")) {
                target = ((String)arguments.get("target"));
            }
            if (arguments.containsKey("args")) {
                args = ((HashMap)arguments.get("args"));
            }
        }
        Request originalRequest = requestManager.getSubmitRequest(user, source, label, target, args);
        if (originalRequest != null) {
            Table table = new Table(3);
            table.setHeaders(new String[]{"Target", "Status", "Submit time"});
            table.addRow(new String[]{originalRequest.getTarget(), originalRequest.getStatusString(),
                    originalRequest.getRequestTime().mysqlString()});
            return table;
        }
        return null;
    }
}
