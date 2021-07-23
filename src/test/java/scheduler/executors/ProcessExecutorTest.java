package scheduler.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProcessExecutorTest {

    @Test
    void testCmdBuilder() throws JsonProcessingException {
        String argStr = "{\n" +
                "  \"process\":\"python3\",\n" +
                "  \"argSet\":[\"arg_name0\",\"arg_name1\"],\n" +
                "  \"envVars\":[\"VALUE0=value0\",\"VALUE1=value1\"],\n" +
                "  \"homeDir\":\"/path/to/home/dir\",\n" +
                "  \"executeUsers\":\"test_user\"\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        ProcessExecutor processExecutor = new ProcessExecutor((HashMap<String, Object>) mapper.readValue(argStr, HashMap.class));
        HashMap args = new HashMap();
        args.put("arg_name0", "value0");
        args.put("arg_name1", 1);
        String[] cmdArray = processExecutor.cmdAppender(args);
        assertTrue(cmdArray.length == 3);
        assertTrue(cmdArray[0].equals("python3"));
        assertTrue(cmdArray[1].equals("arg_name0=value0"));
        assertTrue(cmdArray[2].equals("arg_name1=1"));
    }
}