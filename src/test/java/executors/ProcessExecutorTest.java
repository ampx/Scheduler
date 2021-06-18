package executors;

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
                "  \"argSet\":[\"arg_name0\",\"arg_name1\",\"arg_name2\"],\n" +
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

    @Test
    void testConfigCheck() throws JsonProcessingException {
        String argStr = "{\n" +
                "  \"process\":\"python3\",\n" +
                "  \"argSet\":[\"arg_name0\",\"arg_name1\",\"arg_name2\"],\n" +
                "  \"envVars\":[\"VALUE0=value0\",\"VALUE1=value1\"],\n" +
                "  \"homeDir\":\"/path/to/home/dir\",\n" +
                "  \"executeUsers\":\"test_user\"\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        ProcessExecutor processExecutor = new ProcessExecutor((HashMap<String, Object>) mapper.readValue(argStr, HashMap.class));
        assertTrue(processExecutor.getProcess().equals("python3"));
        assertTrue(processExecutor.getArgEquality().equals("="));
        Set argSet = processExecutor.getArgSet();
        assertTrue(argSet.size()==3);
        assertTrue(argSet.contains("arg_name0") && argSet.contains("arg_name1") && argSet.contains("arg_name2"));
        assertTrue(processExecutor.getHomeDir().equals("/path/to/home/dir"));
        String[] envVars = processExecutor.getEnvVars();
        assertTrue(envVars.length == 2);
        assertTrue(envVars[0].equals("VALUE0=value0") && envVars[1].equals("VALUE1=value1"));
    }

}