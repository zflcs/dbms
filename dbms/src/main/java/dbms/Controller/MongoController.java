package dbms.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import dbms.Service.ArticleService;
import dbms.Service.BeReadService;
import dbms.Service.PopularRankService;
import dbms.Service.ReadService;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;


import org.bson.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;





@RestController
public class MongoController {
    
    @RequestMapping("/query_user")
    String query_user(String uid, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

        String key = "query_user:uid=" + uid;
        if(RedisController.jedis.exists(key)) {
            System.out.println("Cache hit:" + key);
            return RedisController.jedis.get(key);
        }
        System.out.println("Cache miss:" + key);

        ArrayList<Document> docs = ReadService.query_read(new BasicDBObject("uid", uid));
        JSONObject res = new JSONObject();
        ArrayList<String> aids = new ArrayList<>();
        for(Document doc: docs) {
            aids.add(doc.getString("aid"));
        }
        ArrayList<Document> article_docs = ArticleService.query_article(new BasicDBObject("aid", new BasicDBObject("$in", aids)));
        int count = 0;
        for(Document doc: article_docs) {
            res.put("res" + count, doc.toJson());
            count++;
        }

        String ret = res.toString();
        RedisController.jedis.set(key, ret);

        return ret;
    }

    @RequestMapping("/query_article")
    String query_article(@RequestParam(value="aid") String aid, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

        String key = "query_article:aid=" + aid;
        if(RedisController.jedis.exists(key)) {
            System.out.println("Cache hit:" + key);
            return RedisController.jedis.get(key);
        }

        ArrayList<Document> docs = ArticleService.query_article(new BasicDBObject("aid", aid));
        JSONObject res = new JSONObject();
        res.put("res", docs.get(0).toString());

        String ret = res.toString();
        RedisController.jedis.set(key, ret);

        return ret;
    }

    @RequestMapping("/query_article_status")
    String query_article_status(@RequestParam(value="aid") String aid, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

        String key = "query_article_status:aid=" + aid;
        if(RedisController.jedis.exists(key)) {
            System.out.println("Cache hit:" + key);
            return RedisController.jedis.get(key);
        }

        ArrayList<Document> docs = BeReadService.query_be_read(new BasicDBObject("aid", Integer.parseInt(aid)));
        JSONObject res = new JSONObject();
        res.put("res", docs.get(0).toString());

        String ret = res.toString();
        RedisController.jedis.set(key, ret);

        return ret;
    }

    @RequestMapping("/popular_rank")
    String query_popular_rank(@RequestParam(value="time") Long time, @RequestParam(value="type") String type, @RequestParam(value="limit") int limit, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");

        String key = "query_article_status:time=" + time + "type:" + type + "limit:" + limit;
        if(RedisController.jedis.exists(key)) {
            System.out.println("Cache hit:" + key);
            return RedisController.jedis.get(key);
        }

        Timestamp tmp = new Timestamp(time);
        BasicDBObject condition = new BasicDBObject();
        LocalDateTime date = tmp.toLocalDateTime();
        int year = date.getYear();
        int month = date.getMonth().getValue();
        int day = date.getDayOfMonth();
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY,4);
        int week = date.get(weekFields.weekOfWeekBasedYear());
        LocalDateTime dateTime;
        if(type.equals("monthly")) {
            dateTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        } else if(type.equals("weekly")) {
            dateTime = LocalDateTime.of(year, month, 1, 0, 0, week);
        } else {
            dateTime = LocalDateTime.of(year, month, day, 0, 0, 0);
        }
        Timestamp tt = Timestamp.valueOf(dateTime);
        System.out.println(tt.toString());
        condition.append("timestamp", tt.getTime());
        condition.append("temporalGranularity", type);
        Document doc = PopularRankService.query_popular(condition).get(0);
        List<Integer> aids = doc.getList("articleAidList", Integer.class);
        ArrayList<String> target = new ArrayList<>();
        for(int i = 0; i < aids.size(); i++) {
            if(i < limit) {
                target.add(aids.get(i) + "");
            }
        }
        JSONObject res = new JSONObject();
        ArrayList<Document> article_docs = ArticleService.query_article(new BasicDBObject("aid", new BasicDBObject("$in", target)));
        int count = 0;
        for(Document article: article_docs) {
            res.put("res" + count, article.toJson());
            count++;
        }

        String ret = res.toString();
        RedisController.jedis.set(key, ret);

        return ret;
    }

}
