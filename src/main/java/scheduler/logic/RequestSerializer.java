package scheduler.logic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import scheduler.model.Request;

import java.io.IOException;
import java.util.HashMap;

public class RequestSerializer extends StdSerializer<Request> {
    public RequestSerializer() {
        this(null);
    }

    public RequestSerializer(Class<Request> t) {
        super(t);
    }

    @Override
    public void serialize(
            Request request, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeFieldName("targets");
        jgen.writeStartArray();
        jgen.writeStartObject();
        if(request.getTarget() != null) jgen.writeObjectField("target", request.getTarget());
        jgen.writeObjectFieldStart("data");
        if (request.getUser() != null) jgen.writeObjectField("user", request.getUser());
        if (request.getSource() != null) jgen.writeObjectField("source", request.getSource());
        if (request.getTypeString() != null) jgen.writeObjectField("type", request.getTypeString().toLowerCase());
        if (request.getDoCache() != null) jgen.writeObjectField("cache",request.getDoCache());
        if (request.getLabel() != null) jgen.writeObjectField("label",request.getLabel());
        if (request.getArgs() != null ) {
            jgen.writeObjectFieldStart("args");
            HashMap args = request.getArgs();
            for (Object argName : args.keySet()) {
                jgen.writeObjectField((String) argName, args.get(argName));
            }
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
        jgen.writeEndObject();
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
}