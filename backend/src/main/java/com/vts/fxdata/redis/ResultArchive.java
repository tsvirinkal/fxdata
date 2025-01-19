package com.vts.fxdata.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class ResultArchive {

    @Value("${fxdata.redis.archive.key.prefix}")
    private  String keyPrefix;
    @Value("${fxdata.redis.host}")
    private String hostname;

    @Value("${fxdata.redis.port}")
    private int port;

    public String readValue(String key) {
        try (Jedis jedis = new Jedis(hostname, port)) {
            return jedis.get(keyPrefix + key);
        }
    }

    public void writeValue(String key, String value) {
        try (Jedis jedis = new Jedis(hostname, port)) {
            jedis.set(keyPrefix + key, value);
        }
    }
}
