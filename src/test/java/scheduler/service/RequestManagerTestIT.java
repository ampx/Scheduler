package scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import scheduler.config.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import scheduler.model.Request;
import scheduler.model.Search;
import scheduler.util.table.model.Table;

import javax.naming.ConfigurationException;
import java.util.HashMap;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=8080"})
class RequestManagerTestIT {

    String queryUrl = "http://localhost:8080//api/v2/query";
    String searchUrl = "http://localhost:8080//api/v2/search";
    RestTemplate restTemplate = new RestTemplate();

    /*
    Exercise a run scheduler.request & Uri processor
    - validate that can fetch and parse json response into a Table
    - validate that run can immediately return result
     */
    @Test
    void testRunRequest() throws ConfigurationException, JsonProcessingException {
        Request runRequest = Request.createRunRequest();
        runRequest.setUser("writer");
        runRequest.setSource("source0");
        runRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        runRequest.setArgs(args);
        runRequest.setOutputCapture(true);

        HttpEntity<Request> request = new HttpEntity<>(runRequest);
        Table[] result = restTemplate.postForObject(queryUrl, request, Table[].class);
        Table table = result[0];
        assertTrue(table.getRows().get(0)[0] != null);
    }

    /*
    submit a long running process (1 second)
    show that result is not available immediately after processing scheduler.request was submitted
    show that can fetch result after processing period passes
     */
    @Test
    void testSubmitRequest() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setUser("writer");
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setOutputCapture(true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("writer");
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setOutputCapture(true);
        Table[] result = restTemplate.postForObject(queryUrl, submitRequest, Table[].class);
        assertTrue(result.length == 0);
        result = restTemplate.postForObject(queryUrl, getRequest, Table[].class);
        assertTrue(result.length == 0);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result = restTemplate.postForObject(queryUrl, getRequest, Table[].class);
        assertTrue(result.length == 1);
        assertTrue(result[0] != null);
    }

    /*
    Test read only user -
     - show that no data is generated when read only user submits execution scheduler.request
     */
    @Test
    void testReadOnlyPermission() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setUser("reader");
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setOutputCapture(true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("reader");
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setOutputCapture(true);

        restTemplate.postForObject(queryUrl, submitRequest, Table[].class);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Table[] result = restTemplate.postForObject(queryUrl, getRequest, Table[].class);
        assertTrue(result.length == 0);
    }

    /*
    Test read only user -
     - show that read only user can get previously generated data
     */
    @Test
    void testReadOnlyPermission2() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setUser("writer");
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setOutputCapture(true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("reader");
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setOutputCapture(true);

        restTemplate.postForObject(queryUrl, submitRequest, Table[].class);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Table[] result = restTemplate.postForObject(queryUrl, getRequest, Table[].class);
        assertTrue(result.length == 1);
        assertTrue(result[0] != null);
    }

    /*Test User labeled requests - check if can submit and get result using labeled requests
     */
    @Test
    void testLabeledRequests() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setUser("writer");
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        submitRequest.setLabel("label0");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setOutputCapture(true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("writer");
        getRequest.setSource("source0");
        getRequest.setLabel("label0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setOutputCapture(true);
        restTemplate.postForObject(queryUrl, submitRequest, Table[].class);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Table[] result = restTemplate.postForObject(queryUrl, getRequest, Table[].class);
        assertTrue(result[0] != null);
    }

    /*Test User labeled requests - verify that can get users labeled requests
     */
    @Test
    void testLabeledRequests2() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setUser("writer");
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        submitRequest.setLabel("label0");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setOutputCapture(true);
        restTemplate.postForObject(queryUrl, submitRequest, Table[].class);

        Search search = new Search();
        search.setSource("source0");
        search.setUser("writer");
        String[] label_list = restTemplate.postForObject(searchUrl, search, String[].class);

        assertTrue(label_list.length == 1);
        assertTrue(label_list[0].equals("label0"));
    }

    /*
    Make sure that cache cleanup works
     */
    @Test
    void testCacheCleanup() throws ConfigurationException {
        ObjectFactory objectFactory = new ObjectFactory();
        RequestManager requestManager = objectFactory.getRequestManager(null, null, null);
        requestManager.disableCleanup();
        requestManager.cacheTTLMins = 1;
        Request jobRequest = Request.createSubmitRequest();
        jobRequest.setUser("test_user0");
        jobRequest.setSource("itTestSource");
        jobRequest.setTarget("test_job0");
        jobRequest.setArgs(new HashMap(){{put("output", "req0");put("delay", "1");}});
        jobRequest.setOutputCapture(true);
        assertTrue(requestManager.submitJob(jobRequest) == true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("test_user0");
        getRequest.setSource("itTestSource");
        getRequest.setTarget("test_job0");
        getRequest.setArgs(new HashMap(){{put("output", "req0");put("delay", "1");}});
        assertTrue(requestManager.getCachedResult(getRequest) == null);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(requestManager.getCachedResult(getRequest) instanceof Table);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(requestManager.getCachedResult(getRequest) == null);
    }

}