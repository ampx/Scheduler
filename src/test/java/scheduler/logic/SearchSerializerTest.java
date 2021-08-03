package scheduler.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import scheduler.model.Search;

import static org.junit.jupiter.api.Assertions.*;

class SearchSerializerTest {

    @Test
    public void singleTargetTest() throws JsonProcessingException {
        String initString = "{\"target\":\"parent\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(initString, Search.class);

        String serialized = new ObjectMapper().writeValueAsString(search);
        assertTrue(serialized.equals(initString));

    }

    @Test
    public void emptyTargetTest() throws JsonProcessingException {
        String initString = "{\"target\":\"\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(initString, Search.class);

        String serialized = new ObjectMapper().writeValueAsString(search);
        assertTrue(serialized.equals(initString));

    }

    @Test
    public void nestedTargetTest() throws JsonProcessingException {
        String initString = "{\"target\":\"parent.child0.child1\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(initString, Search.class);

        String serialized = new ObjectMapper().writeValueAsString(search);
        assertTrue(serialized.equals(initString));

    }

    @Test
    public void nestedTargetParentArgTest() throws JsonProcessingException {
        String initString = "{\"target\":\"parent?arg0=value0.child0.child1\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(initString, Search.class);

        String serialized = new ObjectMapper().writeValueAsString(search);
        assertTrue(serialized.equals(initString));

    }

    @Test
    public void nestedTargetChildArgTest() throws JsonProcessingException {
        String initString = "{\"target\":\"parent.child0.child1?arg0=value0\"}";
        ObjectMapper mapper = new ObjectMapper();
        Search search = mapper.readValue(initString, Search.class);

        String serialized = new ObjectMapper().writeValueAsString(search);
        assertTrue(serialized.equals(initString));

    }

}