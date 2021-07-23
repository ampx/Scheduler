package scheduler.executors;

import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.mongodb.Block;
import scheduler.util.table.model.Table;

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
                if (result.getColumns())
                System.out.println(document.toJson());
                document.size();
            }
        };
        //BasicDBObject query = BasicDBObject.parse((String) arguments.get("find"));
        //collection.find(query).forEach(createTable);
        List filterList = new ArrayList();
        for (Object key : arguments.keySet()) {
            filterList.add(eq((String) key, arguments.get(key)));
        }
        collection.find(and(filterList));
        return result.get();
    }
}