package dbms;

import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import dbms.Service.UserService;

public class MongoConfig {
    public static final String URI = "mongodb://172.20.1.11:27017/demo";
    public static final MongoClient MONGO_CLIENT = MongoClients.create(URI);
    public static final MongoDatabase MONGO_DATABASE = MONGO_CLIENT.getDatabase("demo");

    // public static void main(String[] args) {
    //     ArrayList<Document> res = UserService.query_user(new BasicDBObject("uid", "2"));
    //     System.out.println();
    //     for(Document doc: res) {
    //         System.out.println(doc.toString());
    //     }
    // }
}
