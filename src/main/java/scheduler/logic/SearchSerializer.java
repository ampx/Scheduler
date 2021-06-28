package scheduler.logic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import scheduler.model.Request;
import scheduler.model.Search;

import java.io.IOException;
import java.util.HashMap;

public class SearchSerializer extends StdSerializer<Search> {
    public SearchSerializer() {
        this(null);
    }

    public SearchSerializer(Class<Search> t) {
        super(t);
    }

    @Override
    public void serialize(
            Search search, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (search.getSource() != null || search.getUser() != null) {
            String jsonStr = "{\"user\":\"" + search.getUser() + "\", " +
                    "\"tempLabel\":\"" + search.getTempLabel() + "\", " +
                    "\"source\":\"" + search.getSource() + "\"}";
            jgen.writeObjectField("target", jsonStr);
        }
        jgen.writeEndObject();
    }
}
