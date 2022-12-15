package dbms.Controller;

import redis.clients.jedis.Jedis;

public class RedisController {
    public static Jedis jedis = new Jedis("172.20.1.12", 6379);
}
