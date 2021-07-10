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
    protected String argDeliminator;
    protected String argEquality;
    Boolean captureOutput = true;

    public UriExecutor(HashMap<String, Object> config) {
        super(config);
        if (config.containsKey("uri")) {
            this.uri = (String) config.get("uri");
        }
        if (config.containsKey("argDelim")) setArgDeliminator((String) config.get("argDelim"));
        else setArgDeliminator("&");
        if (config.containsKey("argEquality")) setArgEquality((String) config.get("argEquality"));
        else setArgEquality("=");
        if (config.containsKey("captureOutput")) captureOutput = (Boolean)config.get("captureOutput");
        restTemplate = new RestTemplate();
    }

    public void setArgDeliminator(String argDeliminator) {
        this.argDeliminator = argDeliminator;
    }

    public void setArgEquality(String argEquality) {
        this.argEquality = argEquality;
    }

    @Override
    public Table execute(HashMap arguments, String cacheName) {
        Table outputTable = null;
        try {
            arguments = filterRequestArgs(arguments);
            arguments.putAll(fixedArgs);
            if (captureOutput) {
                String outputStr = restTemplate.getForObject(uriArgAppender(uri, arguments), String.class);
                if (outputStr.startsWith("[")) {
                    HashMap[] result = new ObjectMapper().readValue(outputStr, HashMap[].class);
                    outputTable = toTable(result, 10);
                } else if (outputStr.startsWith("{")) {
                    HashMap result = new ObjectMapper().readValue(outputStr, HashMap.class);
                    outputTable = toTable(result);
                }
            } else {
                restTemplate.getForObject(uriArgAppender(uri, arguments), String.class);
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
                    uriArguments = uriArguments + argDeliminator + key + argEquality + arguments.get(key);
                } else {
                    uriArguments = key + argEquality + arguments.get(key);
                }
            }
        }
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uriArguments, uri.getFragment());
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
