package scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
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
        properties = {"server.port=8080","inMemoryCache=true"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RequestManagerTestIT {

    String queryUrl = "http://localhost:8080/scheduler/query";
    String searchUrl = "http://localhost:8080/scheduler/search";
    RestTemplate restTemplate = new RestTemplate();

    public HttpHeaders getReadUserHeader(){
        String readUser = "reader";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-grafana-user", readUser);
        headers.set("schedulerKey","superSecretKey");
        return headers;
    }

    public HttpHeaders getExecuteUserHeader(){
        String executeUsers = "writer";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-grafana-user", executeUsers);
        headers.set("schedulerKey","superSecretKey");
        return headers;
    }

    public HttpHeaders getExecuteUserHeaderInvalidKey(){
        String executeUsers = "writer";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-grafana-user", executeUsers);
        headers.set("schedulerKey","superSecretKey");
        return headers;
    }

    String testTarget = "test";

    /*
    Exercise a run scheduler.request & Uri processor
    - validate that can fetch and parse json response into a Table
    - validate that run can immediately return result
     */
    @Test
    void testRunRequest() throws ConfigurationException, JsonProcessingException {

        Request runRequest = Request.createRunRequest();
        runRequest.setSource("source0");
        runRequest.setTarget(testTarget);
        HashMap args = new HashMap();
        args.put("delaySec",1);
        runRequest.setArgs(args);
        runRequest.setDoCache(true);

        HttpEntity<Request> request = new HttpEntity<>(runRequest, getExecuteUserHeader());
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
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setDoCache(true);
        HttpEntity<Request> submitEntity = new HttpEntity<>(submitRequest, getExecuteUserHeader());

        Request getRequest = Request.createGetRequest();
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setDoCache(true);
        HttpEntity<Request> getEntity = new HttpEntity<>(getRequest, getExecuteUserHeader());
        Table[] result = restTemplate.postForObject(queryUrl, submitEntity, Table[].class);
        assertTrue(result.length == 0);
        result = restTemplate.postForObject(queryUrl, getEntity, Table[].class);
        assertTrue(result.length == 0);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result = restTemplate.postForObject(queryUrl, getEntity, Table[].class);
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
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setDoCache(true);

        Request getRequest = Request.createGetRequest();
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setDoCache(true);

        HttpEntity<Request> submitEntity = new HttpEntity<>(submitRequest, getReadUserHeader());
        HttpEntity<Request> getEntity = new HttpEntity<>(getRequest, getReadUserHeader());
        restTemplate.postForObject(queryUrl, submitEntity, Table[].class);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Table[] result = restTemplate.postForObject(queryUrl, getEntity, Table[].class);
        assertTrue(result.length == 0);
    }

    /*
    Test read only user -
     - show that read only user can get previously generated data
     */
    @Test
    void testReadOnlyPermission2() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);

        Request getRequest = Request.createGetRequest();
        getRequest.setSource("source0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);

        HttpEntity<Request> submitEntity = new HttpEntity<>(submitRequest, getExecuteUserHeader());
        HttpEntity<Request> getEntity = new HttpEntity<>(getRequest, getReadUserHeader());

        restTemplate.postForObject(queryUrl, submitEntity, Table[].class);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Table[] result = restTemplate.postForObject(queryUrl, getEntity, Table[].class);
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
        submitRequest.setDoCache(true);

        Request getRequest = Request.createGetRequest();
        getRequest.setUser("writer");
        getRequest.setSource("source0");
        getRequest.setLabel("label0");
        getRequest.setTarget("test");
        getRequest.setArgs(args);
        getRequest.setDoCache(true);

        HttpEntity<Request> submitEntity = new HttpEntity<>(submitRequest, getExecuteUserHeader());
        HttpEntity<Request> getEntity = new HttpEntity<>(getRequest, getExecuteUserHeader());

        restTemplate.postForObject(queryUrl, submitEntity, Table[].class);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Table[] result = restTemplate.postForObject(queryUrl, getEntity, Table[].class);
        assertTrue(result[0] != null);
    }

    /*Test User labeled requests - verify that can get users labeled requests
     */
    @Test
    void testLabeledRequests2() throws ConfigurationException, JsonProcessingException {
        Request submitRequest = Request.createSubmitRequest();
        submitRequest.setSource("source0");
        submitRequest.setTarget("test");
        submitRequest.setLabel("label0");
        HashMap args = new HashMap();
        args.put("delaySec",1);
        submitRequest.setArgs(args);
        submitRequest.setDoCache(true);

        HttpEntity<Request> submitEntity = new HttpEntity<>(submitRequest, getExecuteUserHeader());
        restTemplate.postForObject(queryUrl, submitEntity, Table[].class);

        Search requestSearch = new Search();
        requestSearch.setName("requests");
        Search labels = new Search();
        requestSearch.setTarget(labels);
        labels.setName("labels");
        HashMap searchArg = new HashMap(){{
            put("source","source0");
        }};
        labels.setArgs(searchArg);

        HttpEntity<Search> searchEntity = new HttpEntity<>(requestSearch, getExecuteUserHeader());
        String[] label_list = restTemplate.postForObject(searchUrl, searchEntity, String[].class);

        assertTrue(label_list.length > 1);
        assertTrue(label_list[1].equals("label0"));
    }

    /*Test that can get a list of available executors
     */
    @Test
    void testExecutorList()  {
        Search search = new Search();
        String[] executor_list = restTemplate.postForObject(searchUrl, search, String[].class);

        assertTrue(executor_list.length >= 1);
        assertTrue(executor_list[0].equals("test"));
    }

}