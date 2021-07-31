package scheduler.executors;

import org.junit.jupiter.api.Test;
import scheduler.util.table.model.Table;
import scheduler.util.time.model.Time;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DummyExecutorTest {
    @Test
    void testExecutor() {
        DummyExecutor executor = new DummyExecutor(new HashMap<>());
        HashMap args = new HashMap();
        Integer delaySec  = 1;
        Integer numberOfMetrics = 7;
        args.put("numberOfMetrics", numberOfMetrics);
        args.put("delaySec", delaySec);
        Time start = Time.now();
        Table result = executor.execute(args, null);
        Time end = Time.now();
        assertTrue(start.duration(end).getSeconds()>=delaySec);
        assertTrue(result.getRows().get(0).length == numberOfMetrics + 1);
    }
}