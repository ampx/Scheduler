package scheduler.logic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import scheduler.model.Request;
import scheduler.model.Search;
import scheduler.util.time.model.Time;

import java.io.IOException;
import java.util.HashMap;

public class SearchDeserializer extends StdDeserializer<Search> {

    static ObjectMapper mapper = new ObjectMapper();

    public SearchDeserializer() {
        this(null);
    }

    public SearchDeserializer(Class<Search> t) {
        super(t);
    }

    @Override
    public Search deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);
        Search rootSearch = new Search();
        Search searchNode = rootSearch;
        try {
            if (node.has("target")) {
                //At the moment target value can be passed in only as a string value
                //have a workaround in here to parse json object from a string
                String targetStrRaw = node.get("target").asText();
                String[] targetsStr = targetStrRaw.split("\\.");
                for (String targetStr: targetsStr){
                    if (searchNode.getName() != null) {
                        searchNode = searchNode.createTarget();
                    }
                    if (targetStr.contains("?")){
                        String[] targetWithArg = targetStr.split("\\?");
                        searchNode.setName(targetWithArg[0]);
                        if (targetWithArg.length > 1) {
                            String[] argumentsStr = targetWithArg[1].split("&");
                            HashMap args = new HashMap(argumentsStr.length);
                            searchNode.setArgs(args);
                            for (String argStr : argumentsStr) {
                                String[] argStrSplit = argStr.split("=");
                                if (argStrSplit.length == 2) {
                                    args.put(argStrSplit[0], argStrSplit[1]);
                                }
                            }
                        }
                    } else {
                        searchNode.setName(targetStr);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootSearch;
    }
}
