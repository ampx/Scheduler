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

    @PostMapping("/scheduler/query")
    public List<Table> query(@RequestBody Request request, @RequestHeader Map<String, String> headers) {
        String grafanaUser = headers.get("x-grafana-user");
        request.setUser(grafanaUser);
        List<Table> tables = new ArrayList<>(1);
        Table table = requestManager.addRequest(request);
        if (table != null) tables.add(table);
        return tables;
    }

    @PostMapping("/scheduler/search")
    public Iterable<String> search(@RequestBody Search search, @RequestHeader Map<String, String> headers) {
        String grafanaUser = headers.get("x-grafana-user");
        search.setUser(grafanaUser);
        if (search != null && "requests".equals(search.getName())) {
            if (search.getTarget() != null && "labels".equals(search.getTarget().getName())) {
                HashMap args = search.getTarget().getArgs();
                if (args != null) {
                    return requestManager.getUserRequestLabels(search.getUser(),
                            (String)args.get("source"), (String)args.get("tempLabel"));
                } else {
                    return requestManager.getUserRequestLabels(search.getUser(), null, null);
                }
            }
        } else {
            return requestManager.getJobList();
        }
        return null;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/scheduler")
    public String healthCheck() {
        return "LakeTools Grafana Backend";
    }
}
