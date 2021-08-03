package scheduler.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import scheduler.model.Search;

import static org.junit.jupiter.api.Assertions.*;

class SearchDeserializerTest {

    @Test
    public void testHandlingMultipleNestedObjects() throws JsonProcessingException {
        String jsonSearch = "{\"target\":\"parent.child0\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(jsonSearch, Search.class);
        assertTrue(search.getName().equals("parent"));
        assertTrue(search.getTarget() != null);
        assertTrue(search.getTarget().getName().equals("child0"));
    }

    @Test
    public void testHandlingSingleObject() throws JsonProcessingException {
        String jsonSearch = "{\"target\":\"parent\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(jsonSearch, Search.class);
        assertTrue(search.getName().equals("parent"));
        assertTrue(search.getTarget() == null);
    }

    @Test
    public void testHandlingEmptyTarget() throws JsonProcessingException {
        String jsonSearch = "{\"target\":\"\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(jsonSearch, Search.class);
        assertTrue(search != null);
        assertTrue(search.getName() == "");
        assertTrue(search.getTarget() == null);
    }

    @Test
    public void testHandlingParentArgs() throws JsonProcessingException {
        String jsonSearch = "{\"target\":\"parent?arg0=value0&arg1=value1.child0\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(jsonSearch, Search.class);
        assertTrue(search.getName().equals("parent"));
        assertTrue(search.getTarget() != null);
        assertTrue(search.getTarget().getName().equals("child0"));
        assertTrue(search.getArgs().get("arg0").equals("value0"));
        assertTrue(search.getArgs().get("arg1").equals("value1"));
    }

    @Test
    public void testHandlingChildArgs() throws JsonProcessingException {
        String jsonSearch = "{\"target\":\"parent.child0?arg0=value0&arg1=value1\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(jsonSearch, Search.class);
        assertTrue(search.getName().equals("parent"));
        assertTrue(search.getTarget() != null);
        assertTrue(search.getTarget().getName().equals("child0"));
        assertTrue(search.getTarget().getArgs().get("arg0").equals("value0"));
        assertTrue(search.getTarget().getArgs().get("arg1").equals("value1"));
    }

}