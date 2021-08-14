package scheduler.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProcessExecutorTest {

    @Test
    void testCmdBuilder() throws JsonProcessingException, ConfigurationException {
        String argStr = "{\n" +
                "  \"process\":[\"python3\",\"script.py\"],\n" +
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
        assertTrue(cmdArray.length == 4);
        assertTrue(cmdArray[0].equals("python3"));
        assertTrue(cmdArray[1].equals("script.py"));
        assertTrue(cmdArray[2].equals("arg_name0=value0"));
        assertTrue(cmdArray[3].equals("arg_name1=1"));
    }

    @Test
    void testSafeArgs() throws JsonProcessingException, ConfigurationException {
        String processConfig = "{\n" +
                "  \"process\":[\"python3\"],\n" +
                "  \"argSet\":[\"arg_name0\",\"arg_name1\"],\n" +
                "  \"envVars\":[\"VALUE0=value0\",\"VALUE1=value1\"],\n" +
                "  \"homeDir\":\"/path/to/home/dir\",\n" +
                "  \"executeUsers\":\"test_user\"\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        ProcessExecutor processExecutor = new ProcessExecutor((HashMap<String, Object>) mapper.readValue(processConfig, HashMap.class));

        //verify that no arg or null is valid
        assertTrue(processExecutor.safeArgs(null));

        //invalidCharKey
        Map invalidConfig = new HashMap();
        invalidConfig.put("\\","a");
        assertFalse(processExecutor.safeArgs(invalidConfig));

        //invalidCharValue
        invalidConfig = new HashMap();
        invalidConfig.put("a","$");
        assertFalse(processExecutor.safeArgs(invalidConfig));

        //valid characters
        invalidConfig = new HashMap();
        invalidConfig.put("abcABC","123");
        assertTrue(processExecutor.safeArgs(invalidConfig));

        //mix valid/invalid characters
        invalidConfig = new HashMap();
        invalidConfig.put("abc|ABC","123");
        assertFalse(processExecutor.safeArgs(invalidConfig));

        //test special valid characters
        invalidConfig = new HashMap();
        invalidConfig.put("-_. ", null);
        assertTrue(processExecutor.safeArgs(invalidConfig));


    }
}