package com.kjl.flink.development.debug;

import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.entity.MessageProcessInfo;
import com.kjl.flink.development.sink.RedisSinkMapper;
import com.kjl.flink.development.source.KafkaConsumer;
import com.kjl.flink.development.util.GsonUtil;
import com.kjl.flink.development.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple1;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    static String host = "10.2.84.129";
    static int port = 6379;
    static String password = "2018@HkuszhRedis!!!";
    static String kafkaServers = "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094";
    static String toptic = "his_hl7";

    static JedisUtil resultJedis = new JedisUtil();
    static JedisUtil cacheJedis = new JedisUtil();
    static JedisPool resultPool = new JedisPool(new JedisPoolConfig(),
            host, port, 1000, password, 1);
    static JedisPool cachePool = new JedisPool(new JedisPoolConfig(),
            host, port, 1000, password, 2);
    public static void main(String[] args) {
        try {
            cacheJedis.setJedisPool(cachePool);
            cacheJedis.flushDB();
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(8);
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094");
            properties.setProperty("group.id", "flink-development");
            //设置false 则不更新offset 监控无法查看进度
            properties.put("enable.auto.commit", "true");
            properties.put("auto.commit.interval.ms", "1000");
            FlinkKafkaConsumer<MessageBaseInfo> myConsumer = new FlinkKafkaConsumer<MessageBaseInfo>("his_hl7",
                    new DeserializationSchema<MessageBaseInfo>() {
                @Override
                public MessageBaseInfo deserialize(byte[] bytes) throws IOException {
                    return GsonUtil.fromJson(new String(bytes, StandardCharsets.UTF_8), MessageBaseInfo.class);
                }

                @Override
                public boolean isEndOfStream(MessageBaseInfo messageBaseInfo) {
                    return false;
                }

                @Override
                public TypeInformation<MessageBaseInfo> getProducedType() {
                    return TypeExtractor.createTypeInfo(MessageBaseInfo.class);
                }
            }, properties);
            myConsumer.setStartFromEarliest();     // start from the earliest record possible
            //myConsumer.setStartFromLatest();       // start from the latest record
            //myConsumer.setStartFromTimestamp(...); // start from specified epoch timestamp (milliseconds)
            //myConsumer.setStartFromGroupOffsets(); // the default behaviour

//            FlinkKafkaConsumer<MessageBaseInfo> myConsumer = KafkaConsumer.buildConsumer("his_hl7",
//                    "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094");

            env.addSource(myConsumer)
                    .flatMap(new FlatMapFunction<MessageBaseInfo, Tuple1<Integer>>() {
                        @Override
                        public void flatMap(MessageBaseInfo msg, Collector<Tuple1<Integer>> collector) throws Exception {
                            if(msg.getMsgid()!=null) {
                                //重复过滤
//                                if (cacheJedis.exists("message:" + msg.getMsgid())) {
//                                    return;
//                                }
                                //缓存3600*100秒
                                cacheJedis.setEx("message:" + msg.getMsgid(), 360000, "");
                                //log.info("msg info:{}",msg.getMsg());
//                                if(cacheJedis.setnx("message:" + msg.getMsgid(), msg.getMsg())==0){
//                                    //log.info("message id:{} 重复",msg.getMsgid());
//                                    cacheJedis.zincrby("repeat",1,msg.getMsgid());
//                                }
                                collector.collect(new Tuple1<Integer>(1));
                            }
//                            collector.collect(new Tuple1<Integer>(1));
//                            cacheJedis.lpush("cache:noid","no msg id");
                        }
                    })
                    .keyBy(0)
                    .sum(0)
                    //.addSink(new PrintSinkFunction<>());
            .print()
            ;
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
