package dbms.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    final private Gson gson = new Gson();

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

    @CrossOrigin
    @RequestMapping(value = "/all_file_name")
    String get_all_file_names(@RequestParam(value="article_id") int article_id, HttpServletResponse response) {
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
        return gson.toJson(paths);
    }

    @CrossOrigin
    @RequestMapping(value = "/file_content")
    void get_file(@RequestParam(value="filepath") String filepath, HttpServletResponse response) {
        Path text = new Path(filepath);
        try {
            FSDataInputStream ins = FS.open(text);
            // ByteBuffer bf = new ByteBuffer();
            // ins.read(bf);
            int ch = ins.read();
            while (ch != -1) {
                response.getOutputStream().write(ch);
                ch = ins.read();
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return "ok";
    }
}
