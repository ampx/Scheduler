package scheduler.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import scheduler.util.table.model.Table;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static scheduler.util.table.logic.TableUtil.toTable;

public class UriExecutor extends Executor {
    String uri;
    RestTemplate restTemplate;

    public UriExecutor(HashMap<String, Object> config) {
        super(config);
        if (config.containsKey("uri")) {
            this.uri = (String) config.get("uri");
        }
        restTemplate = new RestTemplate();
    }

    @Override
    public Table execute(HashMap arguments, String cacheName) {
        Table outputTable = null;
        try {
            arguments.putAll(fixedArgs);
            String outputStr = restTemplate.getForObject(uriArgAppender(uri, arguments), String.class);
            if (outputStr != null) {
                if (outputStr.startsWith("[")) {
                    HashMap[] result = new ObjectMapper().readValue(outputStr, HashMap[].class);
                    outputTable = toTable(result, 10);
                } else if (outputStr.startsWith("{")) {
                    HashMap result = new ObjectMapper().readValue(outputStr, HashMap.class);
                    outputTable = toTable(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputTable;
    }

    public URI uriArgAppender(String uriStr, HashMap arguments) throws URISyntaxException {
        URI uri = new URI(uriStr);
        String uriArguments = uri.getQuery();
        if (arguments != null) {
            for (Object key : arguments.keySet()) {
                if (uriArguments != null) {
                    uriArguments = uriArguments + "?" + key + "=" + arguments.get(key);
                } else {
                    uriArguments = key + "=" + arguments.get(key);
                }
            }
        }
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uriArguments, uri.getFragment());
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
