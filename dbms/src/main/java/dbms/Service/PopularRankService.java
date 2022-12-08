package dbms.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import dbms.MongoConfig;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

public class PopularRankService {
    private static final MongoCollection DAILY = MongoConfig.MONGO_DATABASE.getCollection("popular_rank_daily");
    private static final MongoCollection WEEKLY = MongoConfig.MONGO_DATABASE.getCollection("popular_rank_weekly");
    private static final MongoCollection MONTHLY = MongoConfig.MONGO_DATABASE.getCollection("popular_rank_monthly");

    public static ArrayList<Document> query_popular(BasicDBObject condition) {
        ArrayList<Document> res = new ArrayList<>();
        FindIterable<Document> daily = DAILY.find(condition);
        FindIterable<Document> weekly = WEEKLY.find(condition);
        FindIterable<Document> monthly = MONTHLY.find(condition);
        for (Document doc : daily) {
            res.add(doc);
        }
        for (Document doc : weekly) {
            res.add(doc);
        }
        for (Document doc : monthly) {
            res.add(doc);
        }
        return res;
    }
}
