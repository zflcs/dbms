package dbms;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.security.UserGroupInformation;

import dbms.Controller.HdfsController;

public class HdfsConfig {
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
}
