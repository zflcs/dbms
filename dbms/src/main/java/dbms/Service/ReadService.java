package dbms.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import dbms.MongoConfig;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

public class ReadService {
    private static final MongoCollection READ_BEIJING = MongoConfig.MONGO_DATABASE.getCollection("read_beijing");
    private static final MongoCollection READ_HONG_KONG = MongoConfig.MONGO_DATABASE.getCollection("read_hong_kong");

    public static ArrayList<Document> query_read(BasicDBObject condition) {
        ArrayList<Document> res = new ArrayList<>();
        FindIterable<Document> res_beijing = READ_BEIJING.find(condition);
        FindIterable<Document> res_hong_kong = READ_HONG_KONG.find(condition);
        for (Document doc : res_beijing) {
            res.add(doc);
        }
        for (Document doc : res_hong_kong) {
            res.add(doc);
        }
        return res;
    }

    public static void insert_read(ArrayList<Document> read_doc, String region) {
        if(!read_doc.isEmpty()) {
            if (region.equals("Beijing")) {
                READ_BEIJING.insertMany(read_doc);
            } else {
                READ_HONG_KONG.insertMany(read_doc);
            }
        }
    }
}
