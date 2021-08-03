package scheduler.executors;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UriExecutorTest {

    @Test
    public void testUriGenerator() throws URISyntaxException {
        String uriStr = "http://localhost:8080/api";
        String argStr = "?arg0=value0";
        HashMap argMap = new HashMap();
        argMap.put("arg0","value0");
        UriExecutor uriExecutor = new UriExecutor(new HashMap<>());
        URI uriStrGenerated = uriExecutor.uriArgAppender(uriStr, argMap);
        assertEquals(uriStr+argStr, uriStrGenerated.toString());
    }

}