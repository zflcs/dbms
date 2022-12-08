package dbms.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import dbms.MongoConfig;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;

public class ArticleService {
    private static final MongoCollection ARTICLE_SCIENCE = MongoConfig.MONGO_DATABASE.getCollection("article_science");
    private static final MongoCollection ARTICLE_TECH = MongoConfig.MONGO_DATABASE.getCollection("article_tech");

    public static ArrayList<Document> query_article(BasicDBObject condition) {
        ArrayList<Document> res = new ArrayList<>();
        FindIterable<Document> res_science = ARTICLE_SCIENCE.find(condition);
        FindIterable<Document> res_tech = ARTICLE_TECH.find(condition);
        for (Document doc : res_science) {
            res.add(doc);
        }
        for (Document doc : res_tech) {
            res.add(doc);
        }
        return res;
    }

    public static void insert_article(ArrayList<Document> article_doc) {
        if(!article_doc.isEmpty()) {
            if (article_doc.get(0).getString("category").equals("science")) {
                ARTICLE_SCIENCE.insertMany(article_doc);
            } else {
                ARTICLE_TECH.insertMany(article_doc);
            }
        }
    }
}
