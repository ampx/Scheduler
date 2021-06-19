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
        args.put("rows",3);
        args.put("columns",4);
        args.put("delaySec", 1);
        Time start = Time.now();
        Table result = executor.execute(args, false);
        Time end = Time.now();
        assertTrue(start.duration(end).getSeconds()>=1);
        assertTrue(result.getRows().size() == 3);
        assertTrue(result.getRows().get(0).length == 4);
    }
}