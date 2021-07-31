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
            jgen.writeFieldName("target");
            jgen.writeStartObject();
            if (search.getUser() != null) jgen.writeObjectField("user",search.getUser());
            if (search.getTempLabel() != null) jgen.writeObjectField("tempLabel",search.getTempLabel());
            if (search.getSource() != null) jgen.writeObjectField("source",search.getSource());
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
    }
}
