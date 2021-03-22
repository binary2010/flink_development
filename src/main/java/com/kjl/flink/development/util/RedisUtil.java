package com.kjl.flink.development.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import redis.clients.jedis.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Slf4j
public class RedisUtil implements Serializable {
    //    private JedisPool pool = new JedisPool(new JedisPoolConfig(),"172.18.173.116",6379,
//            5000,"2017@Hkuszh!Redis#");
    private JedisPool pool = new JedisPool(new JedisPoolConfig(), "10.2.84.129", 5689,
            5000,
            "c83cb69965f1dcf8f6923f229bdb541e04309a3dcf11b4ecda529c9ed0127fd358fc2a49a71a038766644b42f0bd3de9",
            0);

    public static void main(String[] args) {
        RedisUtil redisUtil = new RedisUtil();

//        String key = "messageZSet:*";
//        ScanParams scanParams = new ScanParams().match(key).count(10000);
//        String cur = ScanParams.SCAN_POINTER_START;
//        boolean cycleIsFinished = false;
//        int count=0;
//        Jedis jedis=redisUtil.getJedis();
//        while(!cycleIsFinished) {
//            ScanResult<String> scanResult =jedis.scan(cur,scanParams);
//            List<String> result = scanResult.getResult();
//            for(String listKey:result) {
//                List<String> listValue = jedis.lrange(listKey,0,-1);
//                for(String value:listValue) {
//                    jedis.hset("hset:" + listKey, value, "ok");
//                    jedis.sadd("set:"+listKey,value);
//                }
//            }
//            count+=result.size();
//            cur = scanResult.getStringCursor();
//            if ("0".equals(cur)) {
//                cycleIsFinished = true;
//            }
//            //jedis.close();
//            log.info("count:{},cur:{}",count,cur);
//        }

        String key = "messageZSet:*";
        ScanParams scanParams = new ScanParams().match(key).count(10000);
        String cur = ScanParams.SCAN_POINTER_START;
        boolean cycleIsFinished = false;
        int count = 0;
        Jedis jedis = redisUtil.getJedis();

        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        while(!cycleIsFinished) {
//            ScanResult<String> scanResult =jedis.scan(cur,scanParams);
//            List<String> result = scanResult.getResult();
//            for(String listKey:result) {
//                List<String> listValue = jedis.lrange(listKey,0,-1);
//            }
//            count+=result.size();
//            cur = scanResult.getStringCursor();
//            if ("0".equals(cur)) {
//                cycleIsFinished = true;
//            }
//            log.info("count:{},cur:{}",count,cur);
//        }
//        stopWatch.stop();
//        log.info("list count:{},time:{}",count,stopWatch.getTime());
//        count=0;
//        stopWatch.reset();
//
//
//        key = "hset:*";
//        scanParams = new ScanParams().match(key).count(10000);
//        cur = ScanParams.SCAN_POINTER_START;
//        cycleIsFinished = false;
//        jedis=redisUtil.getJedis();
//        stopWatch.start();
//        while(!cycleIsFinished) {
//            ScanResult<String> scanResult =jedis.scan(cur,scanParams);
//            List<String> result = scanResult.getResult();
//            for(String listKey:result) {
//                Map<String,String> listValue = jedis.hgetAll(listKey);
//            }
//            count+=result.size();
//            cur = scanResult.getStringCursor();
//            if ("0".equals(cur)) {
//                cycleIsFinished = true;
//            }
//            log.info("count:{},cur:{}",count,cur);
//        }
//        stopWatch.stop();
//        log.info("hset count:{},time:{}",count,stopWatch.getTime());
//        count=0;
//        stopWatch.reset();


        key = "set:*";
        scanParams = new ScanParams().match(key).count(10000);
        cur = ScanParams.SCAN_POINTER_START;
        cycleIsFinished = false;
        jedis = redisUtil.getJedis();
        stopWatch.start();
        while (!cycleIsFinished) {
            ScanResult<String> scanResult = jedis.scan(cur, scanParams);
            List<String> result = scanResult.getResult();
            for (String listKey : result) {
                Set<String> listValue = jedis.smembers(listKey);
            }
            count += result.size();
            //cur = scanResult.getStringCursor();
            cur = scanResult.getCursor();
            if ("0".equals(cur)) {
                cycleIsFinished = true;
            }
            log.info("count:{},cur:{}", count, cur);
        }
        stopWatch.stop();
        log.info("set count:{},time:{}", count, stopWatch.getTime());
        count = 0;
        stopWatch.reset();

        System.out.println(count);
    }

    public JedisPool getPool() {
        return this.pool;
    }

    public void setPool(JedisPool configPool) {
        this.pool = configPool;
    }

    public Jedis getJedis() {
        if (pool == null) {
            throw new NullPointerException();
        }
        return pool.getResource();
    }

    public String flushAll() {
        Jedis jedis = getJedis();
        String stata = jedis.flushAll();
        returnJedis(jedis);
        return stata;
    }

    public void returnJedis(Jedis jedis) {
        if (null != jedis && null != pool) {
            pool.close();
            //pool.returnResource(jedis);
        }
    }
}
