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
        Search search = new Search();
        try {
            if (node.has("target")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode targetData = mapper.readTree(node.get("target").asText());
                if (targetData.has("user")) {
                    search.setUser(targetData.get("user").asText());
                }
                if (targetData.has("source")) {
                    search.setSource(targetData.get("source").asText());
                }
            }
        } catch (Exception e) {

        }
        return search;
    }
}
