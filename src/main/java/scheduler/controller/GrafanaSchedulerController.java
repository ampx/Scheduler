package scheduler.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import scheduler.model.Request;
import scheduler.model.Search;
import scheduler.service.RequestManager;
import scheduler.util.table.model.Table;

import java.util.*;

@RestController
public class GrafanaSchedulerController {

    @Autowired
    RequestManager requestManager;
    Integer counter = 0;

    @PostMapping("/api/v2/query")
    public List<Table> query(@RequestBody Request request) {
        List<Table> tables = null;
        if (request != null) {
            counter++;
            tables = new ArrayList<>();
            Table table = null;
            if (request.isGetRequest()) {
                table = requestManager.getCachedResult(request);
            } else if (request.isRunRequest()) {
                table = requestManager.runJob(request);
            } else if (request.isSubmitRequest()) {
                requestManager.submitJob(request);
            }
            if (table != null) {
                tables.add(table);
            }
        }
        return tables;
    }

    @PostMapping("/api/v2/search")
    public Iterable<String> search(@RequestBody Search search) {
        if (search != null) {
            if (search.getUser() != null && search.getSource() != null) {
                return requestManager.getUserRequestLabels(search.getUser(), search.getSource());
            } else {
                return requestManager.getJobList();
            }
        } else {
            return null;
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v2")
    public String healthCheck() {
        return "LakeTools Grafana Backend";
    }
}
