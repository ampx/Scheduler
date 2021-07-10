package scheduler.executors;

import scheduler.util.table.model.Table;

import java.util.HashMap;
import java.util.Random;

public class DummyExecutor extends Executor {

    Integer defaultMetricsMin = 0;
    Integer defaultMetricsMax = 10;
    Integer defaultNumberOfMetrics = 3;
    Integer defaultDelaySec = 5;

    public DummyExecutor(HashMap<String, Object> config) {
        super(config);
    }

    @Override
    public Table execute(HashMap arguments, String cacheName) {
        Integer metricMin = defaultMetricsMin;
        Integer metricMax = defaultMetricsMax;
        Integer numberOfMetrics = defaultNumberOfMetrics;
        Integer delaySec = defaultDelaySec;
        Integer dataPoints = 20;
        Long startTime = 0L;
        Long endTime = startTime + 10;
        if (arguments != null) {
            if (arguments.containsKey("metricMax")) {
                metricMax = (Integer) arguments.get("metricMax");
            }
            if (arguments.containsKey("numberOfMetrics")) {
                numberOfMetrics = (Integer) arguments.get("numberOfMetrics");
            }
            if (arguments.containsKey("delaySec")) {
                delaySec = (Integer) arguments.get("delaySec");
            }
            if (arguments.containsKey("startTime")) {
                startTime = (Long) arguments.get("startTime");
            }
            if (arguments.containsKey("endTime")) {
                endTime = (Long) arguments.get("endTime");
            }
        }

        Long timeStep = (endTime - startTime)/dataPoints;

        Table table = new Table(numberOfMetrics + 1);
        Long thisTime = startTime;
        for (int i = 0; i < dataPoints; i++) {
            Object[] row = new Object[numberOfMetrics + 1];
            row[0] = thisTime;
            thisTime += timeStep;
            for (int k = 0; k < numberOfMetrics; k++){
                row[k+1] = getRandomNumber(metricMin, metricMax);
            }
            table.addRow(row);
        }
        String[] headers = new String[numberOfMetrics + 1];
        headers[0] = "time";
        for (int k = 0; k < numberOfMetrics; k++){
            headers[k+1] = "metric" + k;
        }
        table.setHeaders(headers);
        table.columns[0].type = "time";
        for (int k = 0; k < numberOfMetrics; k++){
            table.columns[k+1].type = "number";
        }
        try {
            Thread.sleep(delaySec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return table;
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public long getRandomNumber(long min, long max) {
        return (long) ((Math.random() * (max - min)) + min);
    }
}
