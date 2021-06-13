package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import request.Request;
import service.RequestManager;
import util.table.logic.TableUtil;
import util.table.model.Table;

import javax.naming.ConfigurationException;
import java.util.*;

@RestController
public class GrafanaSchedulerController {

    String emptyResponse = TableUtil.getEmptyTable().toString();
    ObjectFactory objectFactory;
    RequestManager requestManager;

    public GrafanaSchedulerController(){
        try {
            objectFactory = new ObjectFactory(null);
            this.requestManager = objectFactory.getRequestManager();
        } catch (ConfigurationException e) {
            System.out.println("failed to setup, check your config file");
            System.out.println(e);
        }

    }

    @PostMapping("/api/v2/query")
    public String query(@RequestBody String requestJson) {
        String response = emptyResponse;
        Table result = null;
        try {
            HashMap<String, Object> grafanaRequest = new ObjectMapper().readValue(requestJson, HashMap.class);
            if (grafanaRequest.containsKey("targets") && ((ArrayList)grafanaRequest.get("targets")).size()>0){
                HashMap<String, Object> requestData = (HashMap<String, Object>)
                        ((HashMap<String, Object>) ((ArrayList)grafanaRequest.get("targets")).get(0)).get("data");
                result = processRequestData(requestData);
                if (result != null) {
                    response = result.toString();
                }
            }
        } catch (Exception e) {
            System.out.println(e);

        }
        return response;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v2")
    public String healthCheck() {
        return "LakeTools Grafana Backend";
    }

    @PostMapping("/api/v2/search")
    public String search(@RequestBody String json) {
        try {
            String result = "[{\"text\":\"*\", \"value\": \"*\"}]";
            HashMap<String, Object> target = null;
            if (json != null) {
                HashMap<String, Object> grafanaRequest = new ObjectMapper().readValue(json, HashMap.class);
                if (grafanaRequest.containsKey("target") && !grafanaRequest.get("target").equals("")) {
                    target = new ObjectMapper().readValue((String) grafanaRequest.get("target"), HashMap.class);
                }
            }
            Iterable<String> data = null;
            if (target != null) {
                data = getRequestLabels(target);
            } else {
                data = objectFactory.getJobManager().getJobList();
            }
            result = "[";
            Boolean start = true;
           for (String value:data) {
                if (start) {
                    start = false;
                    result += "{\"text\":\"*\", \"value\": \"*\"},{ \"text\":\"" + value + "\", \"value\": \"" + value + "\"}";
                } else {
                    result += ",{ \"text\":\"" + value + "\", \"value\": \"" + value + "\"}";
                }
            }
            result += "]";

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public List getRequestLabels(HashMap<String, Object> requestData) {
        String source = "*";
        if (requestData.containsKey("source")) {
            source = (String)requestData.get("source");
        }
        String user = "*";
        if (requestData.containsKey("user")) {
            user = (String)requestData.get("user");
        }
        return requestManager.getUserRequestLabels(user, source);
    }

    public Table processRequestData(HashMap<String, Object> requestData) {
        Table response = null;
        if (requestData != null && requestData.containsKey("type")){
            String type = (String) requestData.get("type");
            Request request;
            if (type.equals("get")) {
                request = Request.createGetRequest();
            } else if (type.equals("submit")) {
                request = Request.createSubmitRequest();
            } else if (type.equals("run")) {
                request = Request.createRunRequest();
            } else {
                return null;
            }
            String user = "*";
            if (requestData.containsKey("user")) {
                user = (String) requestData.get("user");
            }
            request.setUser(user);
            String requestSource = "*";
            if (requestData.containsKey("source")) {
                requestSource = (String) requestData.get("source");
            }
            request.setSource(requestSource);
            String label = null;
            if (requestData.containsKey("label") && ((String) requestData.get("label")).trim().length()> 0){
                if (!requestData.get("label").equals("*")) {
                    label = (String) requestData.get("label");
                }
            }
            request.setLabel(label);
            if (requestData.containsKey("args")){
                request.getArgs().putAll((HashMap) requestData.get("args"));
            }
            String target = (String) requestData.get("target");
            if (target == null) {
                return null;
            }
            request.setTarget(target);
            if (requestData.containsKey("data_dump")) {
                request.setDataDump((Boolean) requestData.get("data_dump"));
            }
            if (requestData.containsKey("output_capture")) {
                request.setOutputCapture((Boolean) requestData.get("output_capture"));
            }
            if (request.isGetRequest()) {
                response = requestManager.getCachedResult(request);
            } else if (request.isRunRequest()) {
                response = requestManager.runJob(request);
            } else if (request.isSubmitRequest()) {
                requestManager.submitJob(request);
            }
        }
        return response;
    }
}
