package dbms.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.ByteBuffer;

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
        HdfsController.CONF.set("fs.defaultFS", "hdfs://172.20.1.0:9000");
        try {
            HdfsController.FS = FileSystem.get(HdfsController.CONF);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/txt_content")
    void text(@RequestParam(value="article_id") int article_id, @RequestParam(value="txt_name") String txt_name, HttpServletResponse response) {
        Path text = new Path("/user/hadoop/articles/article" + article_id + "/" + txt_name);
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

    @RequestMapping(value = "/img_content")
    void img(@RequestParam(value="article_id") int article_id, @RequestParam(value="img_name") String img_name, HttpServletResponse response) {
        Path img_path = new Path("/user/hadoop/articles/article" + article_id + "/" + img_name);
        try {
            FSDataInputStream ins = FS.open(img_path);
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

    @RequestMapping(value = "/video_content")
    void video(@RequestParam(value="article_id") int article_id, @RequestParam(value="video_name") String video_name, HttpServletResponse response) {
        Path video_path = new Path("/user/hadoop/articles/article" + article_id + "/" + video_name);
        System.out.println(video_path.toUri().toString());
        try {
            FSDataInputStream ins = FS.open(video_path);
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
