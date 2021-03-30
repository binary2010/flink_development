package com.kjl.flink.development.debug;

import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.entity.MessageProcessInfo;
import com.kjl.flink.development.sink.RedisSinkMapper;
import com.kjl.flink.development.source.KafkaConsumer;
import com.kjl.flink.development.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.kjl.flink.development.util.MessageDecodeUtil.transforMessage;

/**
 * @author KJL
 * @version 1.0
 * @description 说明
 * @date 2021-03-29 10:25
 */
@Slf4j
public class Kafka {
    static JedisUtil resultJedis = new JedisUtil();
    static JedisUtil cacheJedis = new JedisUtil();
    static JedisPool resultPool = new JedisPool(new JedisPoolConfig(),
            "10.2.200.5", 16379, 1000, "20211223fdfsdfsdfdf@$%ssfpoooiSEEWWEE", 1);
    static JedisPool cachePool = new JedisPool(new JedisPoolConfig(),
            "10.2.200.5", 16379, 1000, "20211223fdfsdfsdfdf@$%ssfpoooiSEEWWEE", 2);
    public static void main(String[] args) {
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094");
            properties.setProperty("group.id", "flink-development");
            //设置false 则不更新offset 监控无法查看进度
            properties.put("enable.auto.commit", "true");
            properties.put("auto.commit.interval.ms", "1000");
//            FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<>("his_hl7", new SimpleStringSchema(), properties);
//            //myConsumer.setStartFromEarliest();     // start from the earliest record possible
//            myConsumer.setStartFromLatest();       // start from the latest record
//            //myConsumer.setStartFromTimestamp(...); // start from specified epoch timestamp (milliseconds)
//            //myConsumer.setStartFromGroupOffsets(); // the default behaviour




            PojoTypeInfo<MessageBaseInfo> pojoType = (PojoTypeInfo<MessageBaseInfo>) TypeExtractor.createTypeInfo(MessageBaseInfo.class);

            String host = "10.2.200.5";
            int port = 16379;
            String password = "20211223fdfsdfsdfdf@$%ssfpoooiSEEWWEE";

//        JedisUtil resultJedis = new JedisUtil(host, port, 1000, null, 1);
//        resultJedis.flushAll();
//        JedisUtil cacheJedis = new JedisUtil(host, port, 1000, null, 2);
//        cacheJedis.flushAll();


            resultJedis.setJedisPool(resultPool);
            Jedis result=resultJedis.getJedisPool().getResource();
            resultJedis.set("abc","123");
            resultJedis.flushDB();
            resultJedis.set("abc","123");



            cacheJedis.setJedisPool(cachePool);
            Jedis cache=cacheJedis.getJedisPool().getResource();
            cacheJedis.set("efg","456");
            cacheJedis.flushDB();
            cacheJedis.set("efg","456");
            Long aa=cacheJedis.zadd("msgabc",11,"kdkdkdkdk");
            Long a=cacheJedis.zadd("msgabc",12,"k1212212dkdkdkdk");
            //VALUE
            Set<String> bb=cacheJedis.zrange("msgabc",0,-1);



            FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder()
                    .setHost(host)
                    .setPort(port)
                    .setPassword(password)
                    .setDatabase(0)
                    .build();
            RedisSink redisSink = new RedisSink<Tuple2<String, String>>(conf, new RedisSinkMapper());

            FlinkKafkaConsumer<MessageBaseInfo> myConsumer = KafkaConsumer.buildConsumer("his_hl7",
                    "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094");

            env.addSource(myConsumer)
                    .flatMap(new FlatMapFunction<MessageBaseInfo, MessageProcessInfo>() {
                        @Override
                        public void flatMap(MessageBaseInfo messageInfo, Collector<MessageProcessInfo> out) throws Exception {
                            log.info("flink处理消息,id:{},type:{},时间:{}", messageInfo.getMsgid(), messageInfo.getMsgtype(),
                                    DateFormatUtils.format(messageInfo.getDatecreated(), "yyyy-MM-dd HH:mm:ss"));
                            List<MessageProcessInfo> list = transforMessage(messageInfo);
                            for (MessageProcessInfo info : list) {
                                //log.info("cache message begin");
                                resultJedis.rpush("messageZSet:" + info.getUpid() + ":" + info.getClinicNo() + ":" + info.getApplyNo(),
                                        info.getMessageType() + ":" + info.getMessageId() + ":" + info.getState());
                                //log.info("cache message end");
                                out.collect(info);
                            }
                        }
                    })
                    .addSink(new PrintSinkFunction<>());
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
