package logic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import request.Request;
import util.time.model.Time;

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
        jgen.writeObjectField("user", request.getUser());
        jgen.writeObjectField("source", request.getSource());
        jgen.writeObjectField("type", request.getTypeString());
        jgen.writeObjectField("target", request.getTarget());
        if (request.getArgs() != null ) {
            jgen.writeObjectFieldStart("args");
            HashMap args = request.getArgs();
            for (Object argName : args.keySet()) {
                jgen.writeObjectField((String) argName, args.get(argName));
            }
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
}