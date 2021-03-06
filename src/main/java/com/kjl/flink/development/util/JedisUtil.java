package com.kjl.flink.development.util;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.*;
import redis.clients.jedis.util.SafeEncoder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author KJL
 */
@Slf4j
public class JedisUtil implements Serializable {

//    String host = "10.2.84.129";
//    int port = 6379;
//    String password = "2018@HkuszhRedis!!!";
//
//    private final int expire = 60000;
//

//
//    public JedisUtil(){
//        getJedisPool();
//    }

    //    public JedisUtil(String host,int port,int timeout,String password,int database){
//        if(jedisPool==null) {
//            jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout, password, database);
//        }
//    }

   private JedisPool jedisPool;

    public JedisPool getJedisPool() {
//        if(jedisPool==null){
//            jedisPool=new JedisPool(new JedisPoolConfig(),
//                    host, port, 1000, password, 3);
//        }
        return jedisPool;
    }

    public void setJedisPool(JedisPool pool) {
        this.jedisPool = pool;
    }

    

   

    public void expire(String key, int seconds) {
        if (seconds < 0) {
            return;
        }
        try(Jedis jedis=jedisPool.getResource()) {
            jedis.expire(key, seconds);
        }
    }


    @Deprecated
    public String flushAll() {
        try(Jedis jedis=jedisPool.getResource()) {
            String stata = jedis.flushAll();
            return stata;
        }
    }

    public String flushDB() {
        try(Jedis jedis=jedisPool.getResource()) {
            //String stata = jedis.flushAll();
            String stata = jedis.flushDB();
            return stata;
        }
    }


    public String rename(String oldkey, String newkey) {
        return rename(SafeEncoder.encode(oldkey), SafeEncoder.encode(newkey));
    }

    public long renamenx(String oldkey, String newkey) {
        try(Jedis jedis=jedisPool.getResource()) {
            long status = jedis.renamenx(oldkey, newkey);

            return status;
        }

    }

    /*
     * ??????key
     */
    public String rename(byte[] oldkey, byte[] newkey) {
        try(Jedis jedis=jedisPool.getResource()) {
            String status = jedis.rename(oldkey, newkey);

            return status;
        }
    }

    /*
     * ??????key?????????????????????????????????             ??????????????????????????????
     */
    public long expired(String key, int seconds) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.expire(key, seconds);

            return count;
        }
    }

    /*
     * ??????key??????????????????????????????????????????????????????????????? 1970???1???1??????00:00:00,???????????????)????????????
     */
    public long expireAt(String key, long timestamp) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.expireAt(key, timestamp);

            return count;
        }
    }

    /*
     * ??????key???????????????       ????????????????????????????????????????????????key????????????????????????
     */
    public long ttl(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.ttl(key);

            return len;
        }
    }

    /*
     * ?????????key?????????????????????  ???????????????????????????????????????????????????key
     *
     * ????????????????????????1???key???????????????????????????????????????0
     */
    public long persist(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.persist(key);

            return count;
        }
    }

    /*
     * ??????keys?????????????????????????????????key
     *
     * ??????????????????????????????
     */
    public long del(String... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.del(keys);

            return count;
        }
    }

    /*
     * ??????keys?????????????????????????????????key
     */
    public long del(byte[]... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.del(keys);

            return count;
        }
    }

    /*
     * ??????key????????????
     */
    public boolean exists(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            boolean exists = jedis.exists(key);

            return exists;
        }
    }

    /*
     * ???List???set???SortSet ??????????????????????????????????????????????????????????????????
     *
     * ??????????????????????????????????????? sort key Desc?????????
     */
    public List<String> sort(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            List<String> list = jedis.sort(key);


            return list;
        }
    }

    /*
     * ???List???set???SortSet ??????????????????????????????????????????????????????????????????
     *
     * ??????????????????????????????????????? sort key Desc?????????
     */
    public List<String> sort(String key, SortingParams parame) {
        try(Jedis jedis=jedisPool.getResource()) {
            List<String> list = jedis.sort(key, parame);

            return list;
        }
    }
    /*
     * ????????????key???????????????
     */

    public String type(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            String type = jedis.type(key);

            return type;
        }
    }

    /*
     * ??????????????????????????????
     *
     * key?????????????????? *?????????????????? ???????????????
     */
    public Set<String> Keys(String pattern) {
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.keys(pattern);


            return set;
        }
    }

    /*************************set??????*******************************/
    /*
     * ???set???????????????????????????member?????????????????????0???????????????1
     */
    public long sadd(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {

            Long s = jedis.sadd(key, member);

            return s;
        }
    }

    public long sadd(byte[] key, byte[] member) {
        try(Jedis jedis=jedisPool.getResource()) {
            Long s = jedis.sadd(key, member);

            return s;
        }
    }

    /*
     * ????????????key???????????????
     *
     * @return ????????????
     */
    public long scard(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            Long count = jedis.scard(key);

            return count;
        }
    }

    /*
     * ?????????????????????????????????????????????????????????????????????
     *
     * @return ????????????????????????
     */
    public Set<String> sdiff(String... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.sdiff(keys);

            return set;
        }
    }

    /*
     * ???????????????????????? SDIFF ????????????????????????????????? destination ??????????????????????????????????????????,?????????????????????????????????
     *
     * @return  ?????????????????????
     */
    public long sdiffstore(String newkey, String... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            Long count = jedis.sdiffstore(newkey, keys);

            return count;
        }
    }

    /*
     * sinter ?????????????????????????????????????????????????????????????????????????????????????????????set
     * @return ?????????????????????
     */
    public Set<String> sinter(String... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.sinter(keys);

            return set;
        }
    }

    /*
     * sinterstore ????????????????????? SINTER ????????????????????????????????? destination ?????????????????????????????????????????????
     * ?????? destination ???????????????????????????????????????destination ????????? key ??????
     *
     * @return ?????????????????????
     */
    public long sinterstore(String dstkey, String... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.sinterstore(dstkey, keys);

            return count;
        }
    }

    /*
     * sismember ????????????????????????????????????
     * @param String member ???????????????
     * @return ????????????1??????????????????0
     */
    public boolean sismember(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
        Boolean s = jedis.sismember(key, member);
        
        return s;
        }
    }
    /*
     * smembers ??????????????????????????????
     * @return ????????????
     */

    public Set<String> smembers(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.smembers(key);

            return set;
        }
    }

    public Set<byte[]> smembers(byte[] key) {
        try(Jedis jedis=jedisPool.getResource()) {
            Set<byte[]> set = jedis.smembers(key);

            return set;
        }
    }

    /*
     * smove ????????????????????????????????????????????? </br>
     * ?????????????????????????????????????????????????????????????????????????????????0</br>
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param srckey ?????????  dstkey????????????   member?????????????????????
     *
     * @return ????????? 1?????? 0??????
     */
    public long smove(String srckey, String dstkey, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            Long s = jedis.smove(srckey, dstkey, member);

            return s;
        }
    }

    /*
     * spop ????????????????????????  ????????????????????????????????????????????????
     *
     * @return ????????????????????????
     */
    public String spop(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            String s = jedis.spop(key); //s ????????????????????????

            return s;
        }
    }

    /*
     * ??????????????????????????????
     * ???????????? key ????????????????????? member ????????????????????? member ???????????????????????? key ??????????????????????????????????????????
     *
     * @param key member??????????????????
     * @return ????????? ????????????1????????????????????????0
     */
    public long srem(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            Long s = jedis.srem(key, member);

            return s;
        }
    }


    /******************************SortSet******************************/

    /*
     * zadd ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param double score ?????? member???????????????
     * @return ????????? 1?????? 0????????????member???
     */
    public long zadd(String key, double score, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.zadd(key, score, member);

            return s;
        }
    }

    /*
     * ??????????????????????????????
     * @param String key
     * @return ??? key ???????????????????????????????????????????????????????????? ??? key ????????????????????? 0 ???
     */
    public long zcard(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.zcard(key);

            return count;
        }
    }

    /*
     * zcount ??????????????????????????????????????????
     *
     * @param double min??????????????????   max??????????????????
     */
    public long zcount(String key, double min, double max) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.zcount(key, min, max);

            return count;
        }
    }

    /*
     * zrange ??????????????????key???????????????????????????0???-1??????????????????????????????
     */
    public Set<String> zrange(String key, int start, int end) {

        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.zrange(key, 0, -1);

            return set;
        }
    }


    /*
     * zrevrange  ??????????????? key ????????????????????????????????????????????????????????? score ?????????(????????????)?????????
     */
    public Set<String> zrevrange(String key, int start, int end) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.zrevrange(key, start, end);

            return set;
        }
    }

    /*
     * zrangeByScore  ??????????????????????????????
     */
    public Set<String> zrangeByScore(String key, double min, double max) {
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.zrangeByScore(key, min, max);

            return set;
        }
    }

    /*
     * ??????????????????????????????????????????
     */
    public long zlength(String key) {
        long len = 0;
        Set<String> set = zrange(key, 0, -1);
        len = set.size();
        return len;
    }

    /*
     * zincrby  ???????????? key ????????? member ??? score ??????????????? increment
     *
     * @return member ???????????? score ??????????????????????????????
     */

    public double zincrby(String key, double score, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            double s = jedis.zincrby(key, score, member);

            return s;
        }
    }
    /*
     * zrank ??????????????? key ????????? member ???????????????????????????????????? score ?????????(????????????)????????????
     */

    public long zrank(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            long index = jedis.zrank(key, member);

            return index;
        }
    }

    /*
     *zrevrank   ??????????????? key ????????? member ???????????????????????????????????? score ?????????(????????????)?????????
     */
    public long zrevrank(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            long index = jedis.zrevrank(key, member);

            return index;
        }
    }

    /*
     * zrem ??????????????? key ?????????????????????????????????????????????????????????????????? key ???????????????????????????????????????????????????????????? Redis 2.4 ??????????????? ZREM ?????????????????????????????????
     * @return ???????????????????????????????????????????????????????????????
     */
    public long zrem(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.zrem(key, member);

            return count;
        }

    }

    /*
     *zremrangebyrank ??????????????? key ??????????????????(rank)???????????????????????????
     *@return ????????????????????????
     */
    public long zremrangeByRank(String key, int start, int end) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.zremrangeByRank(key, start, end);

            return count;
        }

    }


    /*
     * zremrangeByScore  ?????????????????????????????????
     */
    public long zremrangeByScore(String key, double min, double max) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.zremrangeByScore(key, min, max);

            return count;
        }
    }


    /*
     * ????????????????????????????????????
     */
    public double zscore(String key, String member) {
        try(Jedis jedis=jedisPool.getResource()) {
            Double score = jedis.zscore(key, member);

            if (score != null) {
                return score;
            }
            return 0;
        }
    }


    /*******************************hash***********************************/
    /**
     * ???hash????????????????????????
     *
     * @param key   key
     * @param fieid ???????????????
     * @return ????????????1?????????0??????
     */
    public long hdel(String key, String fieid) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.hdel(key, fieid);

            return s;
        }
    }

    public long hdel(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.del(key);

            return s;
        }
    }

    /**
     * ??????hash??????????????????????????????
     *
     * @param key
     * @param fieid ???????????????
     * @return 1?????????0?????????
     */
    public boolean hexists(String key, String fieid) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            boolean s = jedis.hexists(key, fieid);

            return s;
        }
    }

    /**
     * ??????hash???????????????????????????
     *
     * @param key
     * @param fieid ???????????????
     * @return ??????????????????
     */
    public String hget(String key, String fieid) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            String s = jedis.hget(key, fieid);

            return s;
        }
    }

    public byte[] hget(byte[] key, byte[] fieid) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            byte[] s = jedis.hget(key, fieid);
            return s;
        }
    }

    /**
     * ???Map???????????????hash??????????????????
     *
     * @param key
     * @return Map<Strinig, String>
     */
    public Map<String, String> hgetAll(String key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            Map<String, String> map = jedis.hgetAll(key);

            return map;
        }
    }

    /**
     * ????????????????????????
     *
     * @param key
     * @param fieid
     * @param value
     * @return ????????? 1?????????0?????????fieid??????????????????????????????0
     **/
    public long hset(String key, String fieid, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.hset(key, fieid, value);

            return s;
        }
    }

    public long hset(String key, String fieid, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.hset(key.getBytes(), fieid.getBytes(), value);

            return s;
        }
    }

    /**
     * ??????????????????????????????fieid?????????????????????
     *
     * @param key
     * @param fieid
     * @param value
     * @return ????????? 1?????????0??????fieid??????
     **/
    public long hsetnx(String key, String fieid, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.hsetnx(key, fieid, value);

            return s;
        }
    }

    /**
     * ??????hash???value?????????
     *
     * @param key
     * @return List<String>
     */
    public List<String> hvals(String key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            List<String> list = jedis.hvals(key);

            return list;
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param key
     * @param fieid ????????????
     * @param value ???????????????,???????????????
     * @return ??????????????????????????????????????????
     */
    public long hincrby(String key, String fieid, long value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long s = jedis.hincrBy(key, fieid, value);

            return s;
        }
    }

    /**
     * ????????????hash????????????????????????,??????Map??????keySet??????
     *
     * @param key
     * @return Set<String> ?????????????????????
     */
    public Set<String> hkeys(String key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            Set<String> set = jedis.hkeys(key);

            return set;
        }
    }

    /**
     * ??????hash???????????????????????????Map???size??????
     *
     * @param key
     * @return long ???????????????
     */
    public long hlen(String key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.hlen(key);
            return len;
        }
    }

    /**
     * ????????????key??????????????????value?????????List,???????????????key?????????,List???????????????null
     *
     * @param key
     * @param fieids ????????????
     * @return List<String>
     */
    public List<String> hmget(String key, String... fieids) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            List<String> list = jedis.hmget(key, fieids);
            return list;
        }
    }

    public List<byte[]> hmget(byte[] key, byte[]... fieids) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            List<byte[]> list = jedis.hmget(key, fieids);
            return list;
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param key
     * @param map <String,String> ????????????
     * @return ?????????????????????OK
     */
    public String hmset(String key, Map<String, String> map) {
        try(Jedis jedis=jedisPool.getResource()) {
            String s = jedis.hmset(key, map);

            return s;
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param key
     * @param map <String,String> ????????????
     * @return ?????????????????????OK
     */
    public String hmset(byte[] key, Map<byte[], byte[]> map) {
        try(Jedis jedis=jedisPool.getResource()) {
            String s = jedis.hmset(key, map);

            return s;
        }
    }

    // *******************************************Strings*******************************************//

    /**
     * ??????key????????????
     *
     * @param key
     * @return ???
     */
    public String get(String key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            String value = jedis.get(key);

            return value;
        }
    }

    /**
     * ??????key????????????
     *
     * @param key
     * @return ???
     */
    public byte[] get(byte[] key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            byte[] value = jedis.get(key);

            return value;
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param key
     * @param seconds ??????????????????????????????
     * @param value
     * @return String ????????????
     */
    public String setEx(String key, int seconds, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            String str = jedis.setex(key, seconds, value);

            return str;
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param key
     * @param seconds ??????????????????????????????
     * @param value
     * @return String ????????????
     */
    public String setEx(byte[] key, int seconds, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            String str = jedis.setex(key, seconds, value);

            return str;
        }
    }

    /**
     * ????????????????????????????????????key?????????????????????
     *
     * @param key
     * @param value
     * @return long ????????????1???????????????key????????????0????????????key??????
     */
    public long setnx(String key, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long str = jedis.setnx(key, value);

            return str;
        }
    }

    /**
     * ????????????,???????????????????????????????????????value
     *
     * @param key
     * @param value
     * @return ?????????
     */
    public String set(String key, String value) {
        return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    /**
     * ????????????,???????????????????????????????????????value
     *
     * @param key
     * @param value
     * @return ?????????
     */
    public String set(String key, byte[] value) {
        return set(SafeEncoder.encode(key), value);
    }

    /**
     * ????????????,???????????????????????????????????????value
     *
     * @param key
     * @param value
     * @return ?????????
     */
    public String set(byte[] key, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            String status = jedis.set(key, value);

            return status;
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????<br/>
     * ???:String str1="123456789";<br/>
     * ???str1?????????setRange(key,4,0000)???str1="123400009";
     *
     * @param key
     * @param offset
     * @param value
     * @return long value?????????
     */
    public long setRange(String key, long offset, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.setrange(key, offset, value);

            return len;
        }
    }

    /**
     * ????????????key?????????value
     *
     * @param key
     * @param value
     * @return long ?????????value?????????
     **/
    public long append(String key, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.append(key, value);

            return len;
        }
    }

    /**
     * ???key?????????value???????????????????????????value???????????????????????????????????????
     *
     * @param key
     * @param number ???????????????
     * @return long ?????????????????????
     */
    public long decrBy(String key, long number) {
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.decrBy(key, number);

            return len;
        }
    }

    /**
     * <b>????????????????????????id?????????</b><br/>
     * ???key?????????value???????????????????????????value???????????????????????????????????????
     *
     * @param key
     * @param number ???????????????
     * @return long ???????????????
     */
    public long incrBy(String key, long number) {
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.incrBy(key, number);

            return len;
        }
    }

    /**
     * ?????????key?????????value????????????
     *
     * @param key
     * @param startOffset ????????????(??????)
     * @param endOffset   ????????????(??????)
     * @return String ????????????
     */
    public String getrange(String key, long startOffset, long endOffset) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            String value = jedis.getrange(key, startOffset, endOffset);

            return value;
        }
    }

    /**
     * ?????????????????????key?????????value<br/>
     * ??????key?????????????????????value,????????????null
     *
     * @param key
     * @param value
     * @return String ??????value???null
     */
    public String getSet(String key, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            String str = jedis.getSet(key, value);

            return str;
        }
    }

    /**
     * ??????????????????,???????????????key???????????????List?????????????????????null
     *
     * @param keys
     * @return List<String> ????????????
     */
    public List<String> mget(String... keys) {
        try(Jedis jedis=jedisPool.getResource()) {
            List<String> str = jedis.mget(keys);

            return str;
        }
    }

    /**
     * ??????????????????
     *
     * @param keysvalues ???:keysvalues="key1","value1","key2","value2";
     * @return String ?????????
     */
    public String mset(String... keysvalues) {
        try(Jedis jedis=jedisPool.getResource()) {
            String str = jedis.mset(keysvalues);

            return str;
        }
    }

    /**
     * ??????key?????????????????????
     *
     * @param key
     * @return value????????????
     */
    public long strlen(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            long len = jedis.strlen(key);

            return len;
        }
    }

    // *******************************************Lists*******************************************//

    /**
     * List??????
     *
     * @param key
     * @return ??????
     */
    public long llen(String key) {
        return llen(SafeEncoder.encode(key));
    }

    /**
     * List??????
     *
     * @param key
     * @return ??????
     */
    public long llen(byte[] key) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.llen(key);
            return count;
        }
    }

    /**
     * ????????????,?????????List?????????????????????
     *
     * @param key
     * @param index ??????
     * @param value ???
     * @return ?????????
     */
    public String lset(byte[] key, int index, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            String status = jedis.lset(key, index, value);

            return status;
        }
    }

    /**
     * ????????????,?????????List?????????????????????
     *
     * @param key
     * @param index ??????
     * @param value ???
     * @return ?????????
     */
    public String lset(String key, int index, String value) {
        return lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
    }




    /**
     * ??????List?????????????????????
     *
     * @param key
     * @param index ??????
     * @return ???
     **/
    public String lindex(String key, int index) {
        return SafeEncoder.encode(lindex(SafeEncoder.encode(key), index));
    }

    /**
     * ??????List?????????????????????
     *
     * @param key
     * @param index ??????
     * @return ???
     **/
    public byte[] lindex(byte[] key, int index) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            byte[] value = jedis.lindex(key, index);

            return value;
        }
    }

    /**
     * ???List???????????????????????????List
     *
     * @param key
     * @return ???????????????
     */
    public String lpop(String key) {
        return SafeEncoder.encode(lpop(SafeEncoder.encode(key)));
    }

    /**
     * ???List???????????????????????????List
     *
     * @param key
     * @return ???????????????
     */
    public byte[] lpop(byte[] key) {
        try(Jedis jedis=jedisPool.getResource()) {
            byte[] value = jedis.lpop(key);

            return value;
        }
    }

    /**
     * ???List??????????????????????????????List
     *
     * @param key
     * @return ???????????????
     */
    public String rpop(String key) {
        try(Jedis jedis=jedisPool.getResource()) {
            String value = jedis.rpop(key);

            return value;
        }
    }

    /**
     * ???List??????????????????
     *
     * @param key
     * @param value
     * @return ????????????
     */
    public long lpush(String key, String value) {
        return lpush(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    /**
     * ???List??????????????????
     *
     * @param key
     * @param value
     * @return ????????????
     */
    public long rpush(String key, String value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.rpush(key, value);
            return count;
        }
    }

    /**
     * ???List??????????????????
     *
     * @param key
     * @param value
     * @return ????????????
     */
    public long rpush(byte[] key, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.rpush(key, value);

            return count;
        }
    }

    /**
     * ???List???????????????
     *
     * @param key
     * @param value
     * @return ????????????
     */
    public long lpush(byte[] key, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.lpush(key, value);

            return count;
        }
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param key
     * @param start
     * @param end
     * @return List
     */
    public List<String> lrange(String key, long start, long end) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            List<String> list = jedis.lrange(key, start, end);

            return list;
        }
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param key
     * @param start
     * @param end   ???????????????????????????????????????
     * @return List
     */
    public List<byte[]> lrange(byte[] key, int start, int end) {
        // ShardedJedis sjedis = getShardedJedis();
        try(Jedis jedis=jedisPool.getResource()) {
            List<byte[]> list = jedis.lrange(key, start, end);

            return list;
        }
    }

    /**
     * ??????List???c????????????????????????????????????value
     *
     * @param key
     * @param c     ??????????????????????????????????????????List???????????????????????????????????????
     * @param value ???????????????
     * @return ????????????List???????????????
     */
    public long lrem(byte[] key, int c, byte[] value) {
        try(Jedis jedis=jedisPool.getResource()) {
            long count = jedis.lrem(key, c, value);

            return count;
        }
    }

    /**
     * ??????List???c????????????????????????????????????value
     *
     * @param key
     * @param c     ??????????????????????????????????????????List???????????????????????????????????????
     * @param value ???????????????
     * @return ????????????List???????????????
     */
    public long lrem(String key, int c, String value) {
        return lrem(SafeEncoder.encode(key), c, SafeEncoder.encode(value));
    }

    /**
     * ???????????????????????????start???end???????????????
     *
     * @param key
     * @param start ?????????????????????(0?????????????????????)
     * @param end   ?????????????????????????????????-1????????????????????????-2???-3???????????????
     * @return ???????????????
     */
    public String ltrim(byte[] key, int start, int end) {
        try(Jedis jedis=jedisPool.getResource()) {
            String str = jedis.ltrim(key, start, end);

            return str;
        }
    }

    /**
     * ???????????????????????????start???end???????????????
     *
     * @param key
     * @param start ?????????????????????(0?????????????????????)
     * @param end   ?????????????????????????????????-1????????????????????????-2???-3???????????????
     * @return ???????????????
     */
    public String ltrim(String key, int start, int end) {
        return ltrim(SafeEncoder.encode(key), start, end);
    }
}
