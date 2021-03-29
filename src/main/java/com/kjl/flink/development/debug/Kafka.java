package com.kjl.flink.development.debug;

import com.kjl.flink.development.source.KafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * @author KJL
 * @version 1.0
 * @description 说明
 * @date 2021-03-29 10:25
 */
public class Kafka {
    public static void main(String[] args) {
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            Properties properties = new Properties();
            properties.setProperty("bootstrap.servers", "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094");
            properties.setProperty("group.id", "flink-development");
            FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<>("his_hl7", new SimpleStringSchema(), properties);
            //myConsumer.setStartFromEarliest();     // start from the earliest record possible
            myConsumer.setStartFromLatest();       // start from the latest record
            //myConsumer.setStartFromTimestamp(...); // start from specified epoch timestamp (milliseconds)
            //myConsumer.setStartFromGroupOffsets(); // the default behaviour

            DataStream<String> stream = env.addSource(myConsumer);
            stream.addSink(new PrintSinkFunction<>());
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
