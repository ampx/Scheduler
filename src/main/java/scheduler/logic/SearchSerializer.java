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
        String searchTree = null;
        if (search != null) {
            searchTree = "";
            do {
                String target = search.getName();
                String argsStr = "";
                if (search.getArgs() != null) {
                    HashMap args = search.getArgs();
                    argsStr = "?";
                    for (Object key : args.keySet()) {
                        if (argsStr.length() > 1) {
                            argsStr = argsStr + "&" + key + "=" + args.get(key);
                        } else {
                            argsStr = argsStr + key + "=" + args.get(key);
                        }
                    }
                }
                search = search.getTarget();
                if (searchTree.length() > 0) {
                    searchTree += "." + target + argsStr;
                } else {
                    searchTree += target + argsStr;
                }
            } while (search != null);
        }
        jgen.writeObjectField("target", searchTree);
        jgen.writeEndObject();
    }

    public String buildSearchString(Search search) {
        String searchString = "{";
        searchString = "\"" + search.getName() + "\":";
        if (search.getTarget().getTarget() != null && search.getTarget().getArgs() != null) {

        } else {

        }
        searchString += "}";
        return searchString;
    }
}
