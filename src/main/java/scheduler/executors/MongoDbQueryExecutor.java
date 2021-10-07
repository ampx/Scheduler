package scheduler.executors;

import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.mongodb.Block;
import scheduler.util.table.model.Table;


/* NOTE: under development*/
import static com.mongodb.client.model.Filters.*;

public class MongoDbQueryExecutor extends Executor {

    MongoCollection<Document> collection;
    HashMap filter;

    public MongoDbQueryExecutor(HashMap<String, Object> config){
        super(config);
        MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mongoClient = new MongoClient(connectionString);

        MongoDatabase database = mongoClient.getDatabase("mydb");
        collection = database.getCollection("test");
    }

    @Override
    public Table execute(HashMap arguments, String cacheName) {
        Table result = new Table();
        Block<Document> createTable = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                if (result.getColumns() == null) {
                    Table.Column[] columns = new Table.Column[document.size()];
                    result.columns = columns;
                    int i = 0;
                    for (String key : document.keySet()) {
                        Object value = document.get(key);
                        if (value instanceof Number) columns[i++] = result.createNumericColumn(key);
                        if (value instanceof Date) columns[i++] = result.createTimestampColumn(key);
                        else columns[i++] = result.createStringColumn(key);
                    }
                }
                Object[] row = new Object[result.columns.length];
                int i = 0;
                for (Table.Column column : result.columns) {
                    Object value = document.get(column.text);
                    if (column.type == Table.ColumnType.time && value != null) {
                        row[i++] = ((Date)value).getTime()/1000;
                    }
                    else row[i++] = value;
                }
            }
        };
        List filterList = new ArrayList();
        for (Object key : arguments.keySet()) {
            filterList.add(eq((String) key, arguments.get(key)));
        }
        collection.find(and(filterList));
        return result;
    }
}