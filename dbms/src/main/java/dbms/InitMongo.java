package dbms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.core.io.ClassPathResource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import dbms.Controller.MongoController;


public class InitMongo {
    private static final MongoCollection USER_BEIJING = MongoConfig.MONGO_DATABASE.getCollection("user_beijing");
    private static final MongoCollection USER_HONG_KONG = MongoConfig.MONGO_DATABASE.getCollection("user_hong_kong");
    private static final MongoCollection ARTICLE_SCIENCE = MongoConfig.MONGO_DATABASE.getCollection("article_science");
    private static final MongoCollection ARTICLE_TECH = MongoConfig.MONGO_DATABASE.getCollection("article_tech");
    private static final MongoCollection READ_BEIJING = MongoConfig.MONGO_DATABASE.getCollection("read_beijing");
    private static final MongoCollection READ_HONG_KONG = MongoConfig.MONGO_DATABASE.getCollection("read_hong_kong");
    private static final MongoCollection BE_READ_SCIENCE = MongoConfig.MONGO_DATABASE.getCollection("be_read_science");
    private static final MongoCollection BE_READ_TECH = MongoConfig.MONGO_DATABASE.getCollection("be_read_tech");
    private static final MongoCollection DAILY = MongoConfig.MONGO_DATABASE.getCollection("popular_rank_daily");
    private static final MongoCollection WEEKLY = MongoConfig.MONGO_DATABASE.getCollection("popular_rank_weekly");
    private static final MongoCollection MONTHLY = MongoConfig.MONGO_DATABASE.getCollection("popular_rank_monthly");

    public static void main(String[] args) {
        try {
            // 初始化 user 表
            ClassPathResource user_res = new ClassPathResource("user.dat");
            InputStream user_ins = user_res.getInputStream();
            String[] users = new BufferedReader(new InputStreamReader(user_ins))
                .lines().parallel().collect(Collectors.joining("\n")).split("\n");
            List<Document> user_beijing = new ArrayList<>();
            List<Document> user_hong_kong = new ArrayList<>();
            HashMap<Integer, String> uid2region = new HashMap<>();
            for(String user: users) {
                JSONObject user_json = JSONObject.parseObject(user);
                String region = user_json.getString("region");
                uid2region.put(user_json.getInteger("uid"), region);
                if (region.equals("Hong Kong")) {
                    user_hong_kong.add(Document.parse(user));
                } else if (region.equals("Beijing")) {
                    user_beijing.add(Document.parse(user));
                }
                if (user_beijing.size() == 10000) {
                    USER_BEIJING.insertMany(user_beijing);
                    user_beijing.clear();
                }
                if (user_hong_kong.size() == 10000) {
                    USER_HONG_KONG.insertMany(user_hong_kong);
                    user_hong_kong.clear();
                }
            }
            USER_BEIJING.insertMany(user_beijing);
            USER_HONG_KONG.insertMany(user_hong_kong);
            
            // 初始化 article 表
            ClassPathResource article_res = new ClassPathResource("article.dat");
            InputStream article_ins = article_res.getInputStream();
            String[] articles = new BufferedReader(new InputStreamReader(article_ins))
                .lines().parallel().collect(Collectors.joining("\n")).split("\n");
            List<Document> article_science = new ArrayList<>();
            List<Document> article_tech = new ArrayList<>();
            HashMap<Integer, Timestamp> aid2time = new HashMap<>();
            HashMap<Integer, String> aid2category = new HashMap<>();
            for(String article: articles) {
                JSONObject article_json = JSONObject.parseObject(article);
                Timestamp timestamp = article_json.getTimestamp("timestamp");
                Integer aid = article_json.getInteger("aid");
                aid2time.put(aid, timestamp);
                String category = article_json.getString("category");
                aid2category.put(aid, category);
                if (category.equals("science")) {
                    article_science.add(Document.parse(article));
                } else if (category.equals("technology")) {
                    article_tech.add(Document.parse(article));
                }
                if (article_science.size() == 10000) {
                    ARTICLE_SCIENCE.insertMany(article_science);
                    article_science.clear();
                }
                if (article_tech.size() == 10000) {
                    ARTICLE_TECH.insertMany(article_tech);
                    article_tech.clear();
                }
            }
            ARTICLE_SCIENCE.insertMany(article_science);
            ARTICLE_TECH.insertMany(article_tech);

            // 初始化 read 表，需要先查 user 表，再根据 user 的 region 进行插入
            ClassPathResource read_res = new ClassPathResource("read.dat");
            InputStream read_ins = read_res.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(read_ins));
            List<Document> read_beijing = new ArrayList<>();
            List<Document> read_hong_kong = new ArrayList<>();
            String read = null;
            HashMap<Integer, ArrayList<String>> aid2readlist = new HashMap<>();
            HashMap<Integer, ArrayList<Integer>> aid2commentlist = new HashMap<>();
            HashMap<Integer, ArrayList<Integer>> aid2agreelist = new HashMap<>();
            HashMap<Integer, ArrayList<Integer>> aid2sharelist = new HashMap<>();
            HashMap<String, ArrayList<Integer>> timeset = new HashMap<>();
            while((read = bf.readLine()) != null){
                JSONObject read_json = JSONObject.parseObject(read);
                String id = read_json.getString("id");
                Integer uid = read_json.getInteger("uid");
                Integer aid = read_json.getInteger("aid");
                Timestamp timestamp = read_json.getTimestamp("timestamp");
                LocalDateTime localDate1 = timestamp.toLocalDateTime();
                WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY,4);
                String time_month = (timestamp.getYear() + 1900) + "-" + (timestamp.getMonth() + 1) + "-xx-xx";
                String time_week = (timestamp.getYear() + 1900) + "-"+ (timestamp.getMonth() + 1) + "-" + "xx-" + (localDate1.get(weekFields.weekOfWeekBasedYear()));
                String time_day = (timestamp.getYear() + 1900) + "-" + (timestamp.getMonth() + 1) + "-" + (timestamp.getDate()) + "-xx";
                ArrayList<Integer> month_list = timeset.getOrDefault(time_month, new ArrayList<>());
                month_list.add(aid);
                timeset.put(time_month, month_list);
                ArrayList<Integer> week_list = timeset.getOrDefault(time_week, new ArrayList<>());
                week_list.add(aid);
                timeset.put(time_week, week_list);
                ArrayList<Integer> day_list = timeset.getOrDefault(time_day, new ArrayList<>());
                day_list.add(aid);
                timeset.put(time_day, day_list);
                ArrayList<String> readlist = aid2readlist.getOrDefault(aid, new ArrayList<>());
                readlist.add(id);
                aid2readlist.put(aid, readlist);
                Boolean is_agree = read_json.getBoolean("agreeOrNot");
                if(is_agree) {
                    ArrayList<Integer> agreelist = aid2agreelist.getOrDefault(aid, new ArrayList<>());
                    agreelist.add(uid);
                    aid2agreelist.put(aid, agreelist);
                }
                Boolean is_comment = read_json.getBoolean("commentOrNot");
                if(is_comment) {
                    ArrayList<Integer> commentlist = aid2commentlist.getOrDefault(aid, new ArrayList<>());
                    commentlist.add(uid);
                    aid2commentlist.put(aid, commentlist);
                }
                Boolean is_share = read_json.getBoolean("shareOrNot");
                if(is_share) {
                    ArrayList<Integer> sharelist = aid2sharelist.getOrDefault(aid, new ArrayList<>());
                    sharelist.add(uid);
                    aid2sharelist.put(aid, sharelist);
                }
                String region = uid2region.get(uid);
                if (region.equals("Hong Kong")) {
                    read_hong_kong.add(Document.parse(read));
                } else if (region.equals("Beijing")) {
                    read_beijing.add(Document.parse(read));
                }
                if (read_beijing.size() == 10000) {
                    READ_BEIJING.insertMany(read_beijing);
                    read_beijing.clear();
                }
                if (read_hong_kong.size() == 10000) {
                    READ_HONG_KONG.insertMany(read_hong_kong);
                    read_hong_kong.clear();
                }
            }
            READ_BEIJING.insertMany(read_beijing);
            READ_HONG_KONG.insertMany(read_hong_kong);

            // 初始化 Be-Read 表 
            for (Integer aid: aid2category.keySet()) {
                JSONObject be_read_json = new JSONObject();
                be_read_json.put("id", "br" + aid);
                be_read_json.put("timestamp", aid2time.get(aid));
                be_read_json.put("aid", aid);
                ArrayList<String> readlist = aid2readlist.getOrDefault(aid, new ArrayList<>());
                be_read_json.put("readNum", readlist.size());
                be_read_json.put("readUidList", readlist);
                ArrayList<Integer> agreelist = aid2agreelist.getOrDefault(aid, new ArrayList<>());
                be_read_json.put("agreeNum", agreelist.size());
                be_read_json.put("agreeUidList", agreelist);
                ArrayList<Integer> commentlist = aid2commentlist.getOrDefault(aid, new ArrayList<>());
                be_read_json.put("commentNum", commentlist.size());
                be_read_json.put("commentUidList", commentlist);
                ArrayList<Integer> sharelist = aid2sharelist.getOrDefault(aid, new ArrayList<>());
                be_read_json.put("shareNum", sharelist.size());
                be_read_json.put("shareUidList", sharelist);

                String category = aid2category.get(aid);
                if(category.equals("science")) {
                    BE_READ_SCIENCE.insertOne(Document.parse(be_read_json.toString()));
                } else {
                    BE_READ_TECH.insertOne(Document.parse(be_read_json.toString()));
                }
            }

            // 初始化 popular_rank 表
            ClassPathResource read_total = new ClassPathResource("read.dat");
            InputStream read_total_ins = read_total.getInputStream();
            BufferedReader read_total_bf = new BufferedReader(new InputStreamReader(read_total_ins));
            String read_detail = null;
            int prid = 0;
            for(String time: timeset.keySet()) {
                ArrayList<Integer> aid_list = timeset.get(time);
                Set<Integer> uniqueSet = new HashSet<>(aid_list);
                Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
                for (Integer temp : uniqueSet) {
                    map.put(temp, Collections.frequency(aid_list, temp));
                    // System.out.println(temp + ": " + Collections.frequency(aid_list, temp));
                }
                List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer,Integer>>(map.entrySet());
                Collections.sort(list, new Comparator<Map.Entry<Integer,Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer,Integer> o1, Map.Entry<Integer,Integer> o2) {
                        return o2.getValue() - o1.getValue();
                    }
                });
                ArrayList<Integer> sort_list = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : list) {
                    sort_list.add(entry.getKey());
                }
                String[] dates = time.split("-");
                JSONObject popular_rank = new JSONObject();
                popular_rank.put("id", "pr" + prid);
                popular_rank.put("articleAidList", sort_list);

                int day = (dates[2].equals("xx")) ? 1 : Integer.parseInt(dates[2]);
                int week = (dates[3].equals("xx")) ? 0 : Integer.parseInt(dates[3]);
                LocalDateTime date = LocalDateTime.of(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), day, 0, 0, week);
                Timestamp timestamp = Timestamp.valueOf(date);
                popular_rank.put("timestamp", timestamp);
                if(dates[2].equals("xx") && dates[3].equals("xx")) {
                    // monthly
                    popular_rank.put("temporalGranularity", "monthly");
                    WEEKLY.insertOne(Document.parse(popular_rank.toString()));
                } else if (dates[2].equals("xx") && !dates[3].equals("xx")) {
                    // weekly
                    popular_rank.put("temporalGranularity", "weekly");
                    MONTHLY.insertOne(Document.parse(popular_rank.toString()));
                } else {
                    // daily
                    popular_rank.put("temporalGranularity", "daily");
                    DAILY.insertOne(Document.parse(popular_rank.toString()));
                }
                prid++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
