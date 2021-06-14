package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectFactory;
import controller.GrafanaSchedulerController;
import org.junit.jupiter.api.Test;
import request.Request;
import util.table.model.Table;

import javax.naming.ConfigurationException;
import java.util.HashMap;


import static org.junit.jupiter.api.Assertions.*;

class RequestManagerTestIT {

    /*
    Exercise a run request & Uri processor
    - validate that can fetch and parse json response into a Table
    - validate that run can immediately return result
     */
    @Test
    void testRunRequest() throws ConfigurationException, JsonProcessingException {
        GrafanaSchedulerController controller = new GrafanaSchedulerController();
        String query = "{\n" +
                "\t\"user\": \"test_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"run\",\n" +
                "\t\"target\": \"get_executor\",\n" +
                "\t\"output_capture\":true\n" +
                "}";
        Table result = controller.processRequestData(new ObjectMapper().readValue(query, HashMap.class));
        assertTrue(result.getRows().get(0)[0] != null);
    }

    /*
    submit a long running process (1 second)
    show that result is not available immediately after processing request was submitted
    show that can fetch result after processing period passes
     */
    @Test
    void testSubmitRequest() throws ConfigurationException, JsonProcessingException {
        GrafanaSchedulerController controller = new GrafanaSchedulerController();
        String submit_query = "{\n" +
                "\t\"user\": \"test_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"submit\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        String get_query = "{\n" +
                "\t\"user\": \"test_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"get\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        controller.processRequestData(new ObjectMapper().readValue(submit_query, HashMap.class));
        HashMap getMap = new ObjectMapper().readValue(get_query, HashMap.class);
        assertTrue(controller.processRequestData(getMap) == null);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(controller.processRequestData(getMap) instanceof Table);
    }

    /*
    Test read only user -
     - show that no data is generated when read only user submits execution request
     */
    @Test
    void testReadOnlyPermission() throws ConfigurationException, JsonProcessingException {
        GrafanaSchedulerController controller = new GrafanaSchedulerController();
        String submit_query = "{\n" +
                "\t\"user\": \"read_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"submit\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        String get_query = "{\n" +
                "\t\"user\": \"read_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"get\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        controller.processRequestData(new ObjectMapper().readValue(submit_query, HashMap.class));
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap getMap = new ObjectMapper().readValue(get_query, HashMap.class);
        assertTrue(controller.processRequestData(getMap) == null);
    }

    /*
    Test read only user -
     - show that read only user can get previously generated data
     */
    @Test
    void testReadOnlyPermission2() throws ConfigurationException, JsonProcessingException {
        GrafanaSchedulerController controller = new GrafanaSchedulerController();
        String submit_query = "{\n" +
                "\t\"user\": \"write_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"submit\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        String get_query = "{\n" +
                "\t\"user\": \"read_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"get\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        controller.processRequestData(new ObjectMapper().readValue(submit_query, HashMap.class));
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap getMap = new ObjectMapper().readValue(get_query, HashMap.class);
        assertTrue(controller.processRequestData(getMap) instanceof Table);
    }

    /*Test User labeled requests - check if can submit and get result using labeled requests
     */
    @Test
    void testLabeledRequests() throws ConfigurationException, JsonProcessingException {
        GrafanaSchedulerController controller = new GrafanaSchedulerController();

        String submit_query = "{\n" +
                "\t\"user\": \"write_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"submit\",\n" +
                "\t\"label\": \"label0\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        String get_query = "{\n" +
                "\t\"user\": \"write_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"get\",\n" +
                "\t\"label\": \"label0\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"output_capture\":true\n" +
                "}";
        controller.processRequestData(new ObjectMapper().readValue(submit_query, HashMap.class));
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap getMap = new ObjectMapper().readValue(get_query, HashMap.class);
        assertTrue(controller.processRequestData(getMap) instanceof Table);
    }

    /*Test User labeled requests - check if can submit and get result using labeled requests
     */
    @Test
    void testLabeledRequests2() throws ConfigurationException, JsonProcessingException {
        GrafanaSchedulerController controller = new GrafanaSchedulerController();
        String submit_query = "{\n" +
                "\t\"user\": \"write_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"submit\",\n" +
                "\t\"label\": \"label0\",\n" +
                "\t\"target\": \"process_executor\",\n" +
                "\t\"args\": {\"output\": \"req0\", \"delay\": \"1\"},\n" +
                "\t\"output_capture\":true\n" +
                "}";
        controller.processRequestData(new ObjectMapper().readValue(submit_query, HashMap.class));
        String get_query = "{\n" +
                "\t\"user\": \"write_user\",\n" +
                "\t\"source\":\"source0\",\n" +
                "\t\"type\":\"run\",\n" +
                "\t\"label\": \"label0\",\n" +
                "\t\"target\": \"requests\"\n" +
                "}";
        HashMap getMap = new ObjectMapper().readValue(get_query, HashMap.class);
        assertTrue(controller.getRequestLabels(getMap).get(0).equals("label0"));
    }

    /*
    Make sure that cache cleanup works
     */
    @Test
    void testCacheCleanup() throws ConfigurationException {
        ObjectFactory objectFactory = new ObjectFactory(null);
        RequestManager requestManager = objectFactory.getRequestManager();
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