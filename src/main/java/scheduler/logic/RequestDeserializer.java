package scheduler.logic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import scheduler.model.Request;
import scheduler.util.time.model.Time;

import java.io.IOException;
import java.util.HashMap;

public class RequestDeserializer extends StdDeserializer<Request> {

    public RequestDeserializer() {
        this(null);
    }

    public RequestDeserializer(Class<Request> t) {
        super(t);
    }

    @Override
    public Request deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        Request request;
        String targetName = node.get("targets").get(0).get("target").asText();
        JsonNode targetData = node.get("targets").get(0).get("data");
        String type = targetData.get("type").asText();
        if (type.equals("get")) {
            request = Request.createGetRequest();
        } else if (type.equals("submit")) {
            request = Request.createSubmitRequest();
        } else if (type.equals("run")) {
            request = Request.createRunRequest();
        } else {
            return null;
        }
        if (targetData.has("user")) {
            request.setUser(targetData.get("user").asText());
        }
        if (targetData.has("source")) {
            request.setSource(targetData.get("source").asText());
        }
        if (targetData.has("label") && targetData.get("label").asText().trim().length()> 0) {
            request.setLabel(targetData.get("label").asText());
        }
        if (targetData.has("args")) {
            ObjectMapper mapper = new ObjectMapper();
            request.setArgs(mapper.convertValue(targetData.get("args"), HashMap.class));
        }
        if (targetData.has("target")) {
            targetName = targetData.get("target").asText();
        }
        request.setTarget(targetName);
        if (targetData.has("data_dump")) {
            request.setDataDump(targetData.get("data_dump").asBoolean());
        }
        if (targetData.has("output_capture")) {
            request.setOutputCapture(targetData.get("output_capture").asBoolean());
        }
        return request;
    }
}
