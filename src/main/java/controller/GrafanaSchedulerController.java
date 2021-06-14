package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public List<Table> query(@RequestBody Request request) {
        List<Table> tables = null;
        if (request != null) {
            tables = new ArrayList<>();
            if (request.isGetRequest()) {
                tables.add(requestManager.getCachedResult(request));
            } else if (request.isRunRequest()) {
                tables.add(requestManager.runJob(request));
            } else if (request.isSubmitRequest()) {
                requestManager.submitJob(request);
            }
        }
        return tables;
    }

    @PostMapping("/api/v2/search")
    public Iterable<String> search(@RequestBody HashMap request) {
        if (request != null && request.containsKey("target")) {
            HashMap target = null;
            try {
                target = new ObjectMapper().readValue((String) request.get("target"), HashMap.class);
            } catch (JsonProcessingException e) {

            }
            if (target != null) {
                return requestManager.getUserRequestLabels((String)target.get("user"), (String)target.get("source"));
            } else if (target.equals("")) {
                return objectFactory.getJobManager().getJobList();
            }
        }
        return new ArrayList<>();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v2")
    public String healthCheck() {
        return "LakeTools Grafana Backend";
    }
}
