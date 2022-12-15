package dbms;

import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisTest {
    @Test
    public void redisTest01() {
        //连接本地的 Redis 服务
        Jedis jedis = new Jedis("172.20.1.12", 6379);
        // 如果 Redis 服务设置了密码，需要用下面这行代码输入密码
        // jedis.auth("123456");
        System.out.println("连接成功");
        //查看服务是否运行
        System.out.println("服务正在运行: "+jedis.ping());
        jedis.set("A", "Hello");
        System.out.println(jedis.get("A"));
        if(jedis.get("B")==null) {
            System.out.println("NO");
        }
    }
}
