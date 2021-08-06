package scheduler.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import scheduler.model.Request;
import scheduler.util.table.model.Table;

import javax.naming.ConfigurationException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"inMemoryCache=true"})
class RequestManagerTest {

    @Autowired
    RequestManager requestManager;

    /*
    Make sure that cache cleanup works
     */
    @Test
    void testCacheCleanup() throws ConfigurationException {
        requestManager.disableCleanup();
        requestManager.cacheTTLMins = 1;
        Request jobRequest = Request.createSubmitRequest();
        jobRequest.setUser("writer");
        jobRequest.setSource("source0");
        jobRequest.setTarget("test");
        jobRequest.setArgs(new HashMap(){{put("delaySec",1);}});
        jobRequest.setDoCache(true);
        assertTrue(requestManager.submitJob(jobRequest) == true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("writer");
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(new HashMap(){{put("delaySec",1);}});
        assertTrue(requestManager.getCachedResult(getRequest) == null);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(requestManager.getCachedResult(getRequest) instanceof Table);
        requestManager.enableCleanup();
        try {
            Thread.sleep(61000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(requestManager.getCachedResult(getRequest) == null);
    }

}