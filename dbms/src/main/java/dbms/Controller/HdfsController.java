package dbms.Controller;

import com.alibaba.fastjson.JSON;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.security.UserGroupInformation;

@RestController
public class HdfsController {

    public static UserGroupInformation UGI;
    public static Configuration CONF;
    public static FileSystem FS;

    static {
        HdfsController.UGI = UserGroupInformation.createRemoteUser("hadoop");
        HdfsController.CONF = new Configuration();
        HdfsController.CONF.set("fs.defaultFS", "hdfs://127.0.0.1:9000");
        try {
            HdfsController.FS = FileSystem.get(HdfsController.CONF);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/all_file_name")
    String get_all_file_names(@RequestParam(value="aid") int article_id, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        String key = "all_file_name:aid=" + article_id;
        System.out.println("key=" + key);
        if(RedisController.jedis.exists(key)) {
            System.out.println("Cache hit:" + key);
            return RedisController.jedis.get(key);
        }
        System.out.println("Cache miss:" + key);
        Path path = new Path("/user/hadoop/articles/article" + article_id);
        ArrayList<String> paths = new ArrayList<>();
        try {
            FileStatus[] stats = FS.listStatus(path);
            for (FileStatus stat : stats) {
                paths.add(stat.getPath().getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String ret = JSON.toJSONString(paths);
        RedisController.jedis.set(key, ret);

        return ret;
    }


    @RequestMapping(value = "/file_content")
    void file_content(@RequestParam(value="aid") int aid, @RequestParam(value="filename") String filename, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); 
        Path text = new Path("/user/hadoop/articles/article" + aid + "/" + filename);
        try {
            FSDataInputStream ins = FS.open(text);
            int ch = ins.read();
            while (ch != -1) {
                response.getOutputStream().write(ch);
                ch = ins.read();
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
