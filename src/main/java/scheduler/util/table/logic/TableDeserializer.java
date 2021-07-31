package scheduler.util.table.logic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import scheduler.util.table.model.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TableDeserializer extends StdDeserializer<Table> {

    public TableDeserializer() {
        this(null);
    }

    public TableDeserializer(Class<Table> t) {
        super(t);
    }

    @Override
    public Table deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Table table = new Table();

        if (node.has("columns")) {
            JsonNode columnsJson = node.get("columns");
            Table.Column[] columns = new Table.Column[columnsJson.size()];
            for (int i = 0; i < columnsJson.size(); i++) {
                JsonNode columnJson = columnsJson.get(i);
                Table.Column column = table.createColumn(columnJson.get("text").asText(),
                        Table.ColumnType.valueOf(columnJson.get("type").asText()));
                columns[i] = column;
            }
            table.columns = columns;
        }
        if (node.has("rows")) {
            JsonNode rowsJson = node.get("rows");
            List<Object[]> rows = new ArrayList<>(rowsJson.size());
            for(int i = 0; i < rowsJson.size(); i ++) {
                rows.add(new ObjectMapper().convertValue(rowsJson, Object[].class));
            }
            table.setRows(rows);
        }
        return table;
    }
}
