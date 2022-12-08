package dbms.Controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
public class MongoController {
    
    @RequestMapping("/query_user")
    JSONObject query_user(String uid) {
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
        return res;
    }

    @RequestMapping("/query_article")
    JSONObject query_article(@RequestParam(value="aid") String aid) {
        System.out.println(aid);
        ArrayList<Document> docs = ArticleService.query_article(new BasicDBObject("aid", aid));
        JSONObject res = new JSONObject();
        res.put("res", docs.get(0).toString());
        return res;
    }

    @RequestMapping("/query_article_status")
    JSONObject query_article_status(@RequestParam(value="aid") String aid) {
        System.out.println(aid);
        ArrayList<Document> docs = BeReadService.query_be_read(new BasicDBObject("aid", Integer.parseInt(aid)));
        JSONObject res = new JSONObject();
        res.put("res", docs.get(0).toString());
        return res;
    }

    @RequestMapping("/popular_rank")
    JSONObject query_popular_rank(@RequestParam(value="time") Long time, @RequestParam(value="type") String type, @RequestParam(value="limit") int limit) {
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
        return res;
    }

}
