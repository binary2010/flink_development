package com.kjl.flink.development.source;

import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.util.GsonUtil;
import com.kjl.flink.development.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

@Slf4j
public class KafkaConsumer implements Serializable {
    public static FlinkKafkaConsumer<MessageBaseInfo> buildConsumer(String topic, String url) {
        Properties consumeProp = new Properties();
        consumeProp.put("bootstrap.servers", url);
        consumeProp.put("group.id", "flink-development");

        //设置false 则不更新offset 监控无法查看进度
        consumeProp.put("enable.auto.commit", "false");

        consumeProp.put("auto.offset.reset", "earliest");
        //consumeProp.put("auto.offset.reset", "latest");
        consumeProp.put("auto.commit.interval.ms", "1000");
        //consumeProp.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //consumeProp.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        FlinkKafkaConsumer<MessageBaseInfo> myConsumer = new FlinkKafkaConsumer<MessageBaseInfo>(topic, new DeserializationSchema<MessageBaseInfo>() {
            @Override
            public MessageBaseInfo deserialize(byte[] message) throws IOException {
                //log.info(new String(message, StandardCharsets.UTF_8));
                //                log.info("处理kafka消息，id:{},type:{},时间:{}",info.getMsgid(),info.getMsgtype(),
//                        DateFormatUtils.format(info.getDatecreated(),"yyyy-MM-dd HH:mm:ss"));
                return GsonUtil.fromJson(new String(message, StandardCharsets.UTF_8), MessageBaseInfo.class);
            }

            @Override
            public boolean isEndOfStream(MessageBaseInfo nextElement) {
                return false;
            }

            @Override
            public TypeInformation<MessageBaseInfo> getProducedType() {
                return TypeExtractor.createTypeInfo(MessageBaseInfo.class);
            }
        }, consumeProp);
        myConsumer.setStartFromEarliest();     // start from the earliest record possible
        //myConsumer.setStartFromLatest();       // start from the latest record
        //myConsumer.setStartFromTimestamp(...); // start from specified epoch timestamp (milliseconds)
        //myConsumer.setStartFromGroupOffsets(); // the default behaviour

        //指定位置
        //Map<KafkaTopicPartition, Long> specificStartOffsets = new HashMap<>();
        //specificStartOffsets.put(new KafkaTopicPartition("myTopic", 0), 23L);
        //myConsumer.setStartFromSpecificOffsets(specificStartOffsets);


//        myConsumer.assignTimestampsAndWatermarks(
//                //WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(20))
//                WatermarkStrategy.forMonotonousTimestamps()
//        );

        myConsumer.setCommitOffsetsOnCheckpoints(true);
        myConsumer.setStartFromGroupOffsets();
        return myConsumer;
    }
}
