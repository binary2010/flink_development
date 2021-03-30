package com.kjl.flink.development.task;

import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.entity.MessageProcessInfo;
import com.kjl.flink.development.sink.RedisSinkMapper;
import com.kjl.flink.development.source.KafkaConsumer;
import com.kjl.flink.development.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.runtime.operators.util.AssignerWithPeriodicWatermarksAdapter;
import org.apache.flink.util.Collector;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.kjl.flink.development.util.MessageDecodeUtil.transforMessage;

@Slf4j
public class ProcessMessageFromKafka implements Serializable {
    static String host = "10.2.200.132";
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

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.setParallelism(1);
        PojoTypeInfo<MessageBaseInfo> pojoType = (PojoTypeInfo<MessageBaseInfo>) TypeExtractor.createTypeInfo(MessageBaseInfo.class);

        resultJedis.setJedisPool(resultPool);
        resultJedis.flushDB();

        cacheJedis.setJedisPool(cachePool);
        cacheJedis.flushDB();

        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder()
                .setHost(host)
                .setPort(port)
                .setPassword(password)
                .setDatabase(0)
                .build();
        RedisSink redisSink = new RedisSink<Tuple2<String, String>>(conf, new RedisSinkMapper());
        FlinkKafkaConsumer<MessageBaseInfo> myConsumer = KafkaConsumer.buildConsumer(toptic,
                kafkaServers);

        env
                .addSource(myConsumer)
                .flatMap(new FlatMapFunction<MessageBaseInfo, MessageProcessInfo>() {
                    @Override
                    public void flatMap(MessageBaseInfo messageInfo, Collector<MessageProcessInfo> out) throws Exception {
                        if(messageInfo.getMsgid()!=null) {
                            //重复过滤
                            if (cacheJedis.exists("message:"+messageInfo.getMsgid())) {
                                return;
                            }
                            //缓存3600*100秒
                            cacheJedis.setEx("message:"+messageInfo.getMsgid(), 360000, "");

                            log.info("flink处理消息,id:{},type:{},时间:{}", messageInfo.getMsgid(), messageInfo.getMsgtype(),
                                    DateFormatUtils.format(messageInfo.getDatecreated(), "yyyy-MM-dd HH:mm:ss"));
                            List<MessageProcessInfo> list = transforMessage(messageInfo);
                            for (MessageProcessInfo info : list) {
//                            log.info("缓存消息,id:{},type:{},时间:{}",info.getMessageId(),info.getMessageType(),
//                                    DateFormatUtils.format(info.getDateCreated(),"yyyy-MM-dd HH:mm:ss"));
                                //分组缓存
                                //病人号：流水号：申请单号：状态
//                                resultJedis.rpush(info.getMessageType()
//                                                + ":" + "messageZSetCache"
//                                                + ":" + info.getUpid()
//                                                + ":" + info.getClinicNo()
//                                                + ":" + info.getApplyNo(),
//                                        info.getMessageType() + ":" + info.getMessageId() + ":" + info.getState());

                                //考虑处理分发乱序
                                resultJedis.zadd(
//                                        info.getMessageType()
//                                        + ":" +
                                                "messageZSetCache"
                                        + ":" + info.getUpid()
                                        + ":" + info.getClinicNo()
                                        + ":" + info.getApplyNo(),info.getDateCreated().getTime(),
                                        info.getMessageType()
                                                + ":" + info.getMessageId()
                                                + ":" + info.getState());
                                out.collect(info);
                            }
                        }
                    }
                }).setParallelism(1)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<MessageProcessInfo>() {
                    @Override
                    public long extractAscendingTimestamp(MessageProcessInfo info) {
//                        log.info("抽取时间戳,id:{},type:{},时间:{}",info.getMessageId(),info.getMessageType(),
//                                DateFormatUtils.format(info.getDateCreated(),"yyyy-MM-dd HH:mm:ss"));
                        return info.getDateCreated().getTime();
                    }
                })
                .setParallelism(1)
                //窗口周期 10小时
                .timeWindowAll(Time.hours(10))
                .apply(new AllWindowFunction<MessageProcessInfo, Tuple2<String, String>, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<MessageProcessInfo> input, Collector<Tuple2<String, String>> out) throws Exception {
                        StringBuilder resultSb = new StringBuilder();
                        resultSb.delete(0, resultSb.length());
                        for (MessageProcessInfo info : input) {
                            resultSb.delete(0, resultSb.length());

                            Set<String> cache = resultJedis.zrange(
//                                    info.getMessageType()
//                                            + ":" +
                                            "messageZSetCache"
                                            + ":" + info.getUpid()
                                            + ":" + info.getClinicNo()
                                            + ":" + info.getApplyNo(),
                                    0, -1);

                            String cacheState;
                            String[] cacheInfo;
                            switch (info.getMessageType()) {
                                case "OML_O21":
                                case "OMG_O19":
                                    cacheState = cache.iterator().next();
                                    cacheInfo = cacheState.split(":");

                                    //类型顺序不一致
                                    //状态顺序不一致
                                    if (!cacheInfo[0].equals(info.getMessageType())
                                            || ("RU".equals(cacheInfo[2]) && "NW".equals(info.getState()))
                                    ) {
                                        log.info("消息顺序错误,创建时间：{},申请单号：{},当前信息：{},{},{},历史信息：{},{},{}",
                                                DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
                                                info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
                                                cacheInfo[0], cacheInfo[1], cacheInfo[2]
                                        );
                                        resultSb = buildResult(resultSb, info.getMessageType() + "消息顺序错误", info, cacheInfo);

                                        Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType() + "消息顺序错误:" + new Timestamp(window.getEnd()) + ":" + info.getUpid(), resultSb.toString());
                                        out.collect(saveDate);
                                    }
                                    break;
                                case "ORL_O22":
                                case "ORG_O20":
                                    cacheState = cache.iterator().next();
                                    cacheInfo = cacheState.split(":");
                                    if (cacheInfo[0].equals(info.getMessageType())) {
                                        log.info("没有申请消息,创建时间：{},申请单号：{},当前信息：{},{},{},历史信息：{},{},{}",
                                                DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
                                                info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
                                                cacheInfo[0], cacheInfo[1], cacheInfo[2]
                                        );
                                        resultSb = buildResult(resultSb, info.getMessageType() + "没有申请消息", info, cacheInfo);

                                        Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType() + "没有申请消息:" + new Timestamp(window.getEnd()) + ":" + info.getUpid(), resultSb.toString());
                                        out.collect(saveDate);
                                    } else if (("OML_O21".equals(cacheInfo[0]) && !"NW".equals(cacheInfo[2]))
                                            || ("OMG_O19".equals(cacheInfo[0]) && !"NW".equals(cacheInfo[2]))) {
                                        log.info("没有NW申请消息,创建时间：{},申请单号：{},当前信息：{},{},{},历史信息：{},{},{}",
                                                DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
                                                info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
                                                cacheInfo[0], cacheInfo[1], cacheInfo[2]
                                        );
                                        resultSb = buildResult(resultSb, info.getMessageType() + "没有NW申请消息", info, cacheInfo);

                                        Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType() + "没有NW申请消息:" + new Timestamp(window.getEnd()) + ":" + info.getUpid(), resultSb.toString());
                                        out.collect(saveDate);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }

                    }
                })
                .addSink(redisSink).name("redis_sink");
        JobExecutionResult result = env.execute("My Flink Job");
        log.info("job execute time:{}", result.getNetRuntime(TimeUnit.SECONDS));
    }

    private static StringBuilder buildResult(StringBuilder result, String errType, MessageProcessInfo info, String[] cacheInfo) {
        return result.append(errType)
                .append(":创建时间 ")
                .append(DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"))
                .append(" 申请单号 ")
                .append(info.getApplyNo())

                .append(" --当前信息 ")
                .append(info.getMessageType())
                .append(" ")
                .append(info.getMessageId())
                .append(" ")
                .append(info.getState())

                .append(" --历史信息 ")
                .append(cacheInfo[0])
                .append(" ")
                .append(cacheInfo[1])
                .append(" ")
                .append(cacheInfo[2])

                .append("\n");
    }

}
