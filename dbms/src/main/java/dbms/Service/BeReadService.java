package dbms.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import dbms.MongoConfig;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

public class BeReadService {
    private static final MongoCollection BE_READ_SCIENCE = MongoConfig.MONGO_DATABASE.getCollection("be_read_science");
    private static final MongoCollection BE_READ_TECH = MongoConfig.MONGO_DATABASE.getCollection("be_read_tech");

    public static ArrayList<Document> query_be_read(BasicDBObject condition) {
        ArrayList<Document> res = new ArrayList<>();
        FindIterable<Document> res_science = BE_READ_SCIENCE.find(condition);
        FindIterable<Document> res_tech = BE_READ_TECH.find(condition);
        for (Document doc : res_science) {
            res.add(doc);
        }
        for (Document doc : res_tech) {
            res.add(doc);
        }
        return res;
    }
}
