package com.kjl.flink.development.source;

import com.kjl.flink.development.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class KafkaJdbcProducer {
    public static void main(String[] args) {
        String serializer = StringSerializer.class.getName();
        String deserializer = StringDeserializer.class.getName();
        String topic = "kafka-topic-flink-jdbc-2";


/*
        // kafka-topic-flink-jdbc-1 506926
        // 2 495438
        // kafka-topic-flink-jdbc-2 jdbc-message 495438
        Properties produceProp = new Properties();
        produceProp.put("bootstrap.servers", "10.2.84.129:9092");
        produceProp.put("acks", "all");
        produceProp.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        produceProp.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");


        Producer<String, String> producer = new KafkaProducer<>(produceProp);

        Date currentDate;
        Date beginDate;
        try {
            beginDate = DateUtils.parseDate("2019-12-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            currentDate = DateUtils.parseDate("2020-01-09 12:30:00", "yyyy-MM-dd HH:mm:ss");

            //kafka-topic-flink-jdbc-1
            //currentDate = DateUtils.parseDate("2019-12-02 12:30:00", "yyyy-MM-dd HH:mm:ss");
            long diffHours = TimeUnit.HOURS.convert(currentDate.getTime() - beginDate.getTime(), TimeUnit.MILLISECONDS);
            for (int i = 0; i < diffHours; i++) {

                try (Connection connection = MySQLUtil.getConnection()) {
                    String sql = "select a.id id,a.msg_id msg_id,a.msg_type msg_type,a.date_created date_created,a.raw_data raw_data from hl7_message a where a.date_created > to_date(?,'yyyy-MM-dd hh24:mi:ss') and a.date_created<=to_date(?,'yyyy-MM-dd hh24:mi:ss') and a.type='Source' and (a.msg_type='OML^O21'or a.msg_type='ORL^O22' or a.msg_type='OMG^O19' or a.msg_type='ORG^O20') order by a.date_created,a.msg_id";
                    PreparedStatement ps = connection.prepareStatement(sql);

                    Date begin = DateUtils.addHours(beginDate, i);
                    Date end = DateUtils.addHours(beginDate, i + 1);

                    ps.setString(1, DateFormatUtils.format(begin, "yyyy-MM-dd HH:mm:ss"));
                    ps.setString(2, DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss"));
                    ps.setFetchSize(2000);
                    ResultSet resultSet = ps.executeQuery();
                    int count = 0;
                    while (resultSet.next()) {
                        MessageBaseInfo baseInfo = new MessageBaseInfo(
                                resultSet.getString("id"),
                                resultSet.getString("msg_id"),
                                resultSet.getString("msg_type"),
                                resultSet.getTimestamp("date_created"),
                                converClobToString(resultSet.getClob("raw_data"))
                        );
                        count++;
                        producer.send(new ProducerRecord<String, String>(topic, "jdbc-message", GsonUtil.toJson(baseInfo)));
                    }
                    log.info("发送数量：{}/{},{}",i,diffHours,count);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        producer.close();
*/


        Properties consumeProp = new Properties();
        consumeProp.put("bootstrap.servers", "10.2.84.129:9092");
        consumeProp.put("group.id", "flink-consumer");
        //设置false 则不更新offset
        consumeProp.put("enable.auto.commit", "false");
        consumeProp.put("auto.offset.reset", "earliest");
        consumeProp.put("auto.commit.interval.ms", "1000");
        consumeProp.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumeProp.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumeProp);

        //从所有分区的所有偏移量开始消费
        consumer.subscribe(Collections.singletonList(topic));
        consumer.poll(Duration.ofSeconds(1));
        int offset = 10;
        for (TopicPartition partition : consumer.assignment()) {
            consumer.seek(partition, offset);
        }

        //consumer.subscribe(Collections.singletonList(topic));

        RedisUtil redisUtil = new RedisUtil();
        Jedis jedis = redisUtil.getJedis();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            //if(records.isEmpty()){
            //    break;
            //}
            for (ConsumerRecord<String, String> record : records) {
//                System.out.printf("%s [%d] offset=%d, key=%s, value=\"%s\"\n",
//                        record.topic(), record.partition(),
//                        record.offset(), record.key(), record.value());
                jedis.sadd("messageSet:" + UUID.randomUUID().toString(), record.value());
            }
        }
    }
}
