package dbms.Service;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.internal.operation.AggregateOperation;

import dbms.MongoConfig;


public class UserService {
    private static final MongoCollection USER_BEIJING = MongoConfig.MONGO_DATABASE.getCollection("user_beijing");
    private static final MongoCollection USER_HONG_KONG = MongoConfig.MONGO_DATABASE.getCollection("user_hong_kong");

    public static ArrayList<Document> query_user(BasicDBObject condition) {
        FindIterable<Document> res_beijing = USER_BEIJING.find(condition);
        FindIterable<Document> res_hong_kong = USER_HONG_KONG.find(condition);
        ArrayList<Document> res = new ArrayList<>();
        for (Document doc : res_beijing) {
            res.add(doc);
        }
        for (Document doc : res_hong_kong) {
            res.add(doc);
        }
        return res;
    }

    public static void insert_user(ArrayList<Document> user_doc) {
        if(!user_doc.isEmpty()) {
            if (user_doc.get(0).getString("region").equals("Beijing")) {
                USER_BEIJING.insertMany(user_doc);
            } else {
                USER_HONG_KONG.insertMany(user_doc);
            }
        }
    }

    public static void update_user(BasicDBObject condition, BasicDBObject update) {
        USER_BEIJING.updateMany(condition, update);
        USER_HONG_KONG.updateMany(condition, update);
    }

    public static void delete_user(BasicDBObject condition) {
        USER_BEIJING.deleteMany(condition);
        USER_HONG_KONG.deleteMany(condition);
    }
}
