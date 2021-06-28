package scheduler.executors;

import scheduler.util.table.model.Table;

import java.util.HashMap;
import java.util.Random;

public class DummyExecutor extends Executor {

    Integer defaultRows = 5;
    Integer defaultColumns = 5;
    Integer defaultDelaySec = 5;
    Random rd = new Random();

    public DummyExecutor(HashMap<String, Object> config) {
        super(config);
    }

    @Override
    public Table execute(HashMap arguments, boolean captureOutput) {
        Integer rows = defaultRows;
        Integer columns = defaultColumns;
        Integer delaySec = defaultDelaySec;
        if (arguments != null) {
            if (arguments.containsKey("rows")) {
                rows = (Integer) arguments.get("rows");
            }
            if (arguments.containsKey("columns")) {
                columns = (Integer) arguments.get("columns");
            }
            if (arguments.containsKey("delaySec")) {
                delaySec = (Integer) arguments.get("delaySec");
            }
        }

        Table table = new Table(columns);
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[columns];
            for (int k = 0; k < columns; k++){
                row[k] = rd.nextInt();
            }
            table.addRow(row);
        }
        String[] headers = new String[columns];
        for (int k = 0; k < columns; k++){
            headers[k] = "column" + k;
        }
        table.setHeaders(headers);
        try {
            Thread.sleep(delaySec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return table;
    }
}
