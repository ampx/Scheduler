package scheduler.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorTest {
    @Test
    void testSanitizeRequestArgs() throws JsonProcessingException, ConfigurationException {
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
        args.put("invalid_arg","invalid");
        HashMap sanitizeRequestArgs = processExecutor.sanitizeRequestArgs(args);
        assertTrue(sanitizeRequestArgs.size() == 2);
        assertTrue(sanitizeRequestArgs.get("arg_name0").equals("value0"));
        assertTrue(sanitizeRequestArgs.get("arg_name1").equals(1));
    }

}