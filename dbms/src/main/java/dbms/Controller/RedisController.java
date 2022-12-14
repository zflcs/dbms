package dbms.Controller;

import redis.clients.jedis.Jedis;

public class RedisController {
    public static Jedis jedis = new Jedis("localhost", 6379);
}
