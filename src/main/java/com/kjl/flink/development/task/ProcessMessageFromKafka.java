package com.kjl.flink.development.task;

import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.entity.MessageProcessInfo;
import com.kjl.flink.development.sink.RedisSinkMapper;
import com.kjl.flink.development.util.GsonUtil;
import com.kjl.flink.development.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.util.Collector;
import org.apache.flink.walkthrough.common.entity.Alert;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.kjl.flink.development.util.MessageDecodeUtil.transforMessage;

@Slf4j
public class ProcessMessageFromKafka implements Serializable {
    static String host = "10.2.84.129";
    static int port = 6379;
    static String password = "2018@HkuszhRedis!!!";
    static String kafkaServers = "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094";
    static String toptic = "his_hl7";

    static JedisUtil resultJedis = new JedisUtil();
    static JedisUtil cacheJedis = new JedisUtil();
    static JedisPool resultPool = new JedisPool(new JedisPoolConfig(),
            host, port, 10000, password, 1);
    static JedisPool cachePool = new JedisPool(new JedisPoolConfig(),
            host, port, 10000, password, 2);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        host="10.2.200.5";
        port=6379;
        password="20211223fdfsdfsdfdf@$%ssfpoooiSEEWWEE";

        JedisPool resultPool = new JedisPool(new JedisPoolConfig(),
                host, port, 10000, password, 1);
        JedisPool cachePool = new JedisPool(new JedisPoolConfig(),
                host, port, 10000, password, 2);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        env.setParallelism(1);
        PojoTypeInfo<MessageBaseInfo> pojoType = (PojoTypeInfo<MessageBaseInfo>) TypeExtractor.createTypeInfo(MessageBaseInfo.class);

        resultJedis.setJedisPool(resultPool);
        resultJedis.flushDB();
        resultJedis.flushAll();

        cacheJedis.setJedisPool(cachePool);
        cacheJedis.flushDB();

        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder()
                .setHost(host)
                .setPort(port)
                .setPassword(password)
                .setDatabase(0)
                .build();
        RedisSink redisSink = new RedisSink<Tuple2<String, String>>(conf, new RedisSinkMapper());
//        FlinkKafkaConsumer<MessageBaseInfo> myConsumer = KafkaConsumer.buildConsumer(toptic,
//                kafkaServers);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "10.2.200.69:9092,10.2.200.69:9093,10.2.200.69:9094");
        properties.setProperty("group.id", "flink-development");
        properties.put("enable.auto.commit", "true");
        properties.put("auto.commit.interval.ms", "1000");
        FlinkKafkaConsumer<MessageBaseInfo> myConsumer = new FlinkKafkaConsumer<MessageBaseInfo>("his_hl7_development",
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
        myConsumer.setStartFromEarliest();
        //myConsumer.setStartFromLatest();


        env
                .addSource(myConsumer)
                .flatMap(new FlatMapFunction<MessageBaseInfo, MessageProcessInfo>() {
                    @Override
                    public void flatMap(MessageBaseInfo messageInfo, Collector<MessageProcessInfo> out) throws Exception {
                        if(messageInfo.getMsgid()!=null) {
//                            //????????????
//                            if (cacheJedis.exists("message:"+messageInfo.getMsgid())) {
//                                return;
//                            }
//                            //??????3600*100???
//                            cacheJedis.setEx("message:"+messageInfo.getMsgid(), 360000, "");

                            if(cacheJedis.setnx("message:"+messageInfo.getMsgid(), "")==1) {

                                List<MessageProcessInfo> list = transforMessage(messageInfo);
                                for (MessageProcessInfo info : list) {
                                    if (info.getApplyNo() != null) {
                                        resultJedis.zadd(
//                                        info.getMessageType()
//                                        + ":" +
                                                "messageZSetCache"
                                                        + ":" + info.getUpid()
                                                        + ":" + info.getClinicNo()
                                                        + ":" + info.getApplyNo()
                                                , info.getDateCreated().getTime(),
                                                info.getMessageType()
                                                        + ":" + info.getMessageId()
                                                        + ":" + info.getState()
                                                        + ":" + info.getMsgsender()
                                                        + ":" + info.getMsgreceiver()
                                                        + ":" + DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss")
                                        );
                                        out.collect(info);
                                    }
                                }
                            }
                        }
                    }
                })
                .setParallelism(1)
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<MessageProcessInfo>forBoundedOutOfOrderness(Duration.ofSeconds(60))
                        .withTimestampAssigner(new SerializableTimestampAssigner<MessageProcessInfo>() {
                            @Override
                            public long extractTimestamp(MessageProcessInfo event, long timestamp) {
                                return event.getDateCreated().getTime();
                            }
                        })
                        .withIdleness(Duration.ofSeconds(30))

                ).setParallelism(1)
                .keyBy(MessageProcessInfo::getApplyNo)
//                .process(new KeyedProcessFunction<String, MessageProcessInfo, Tuple2<String, String>>() {
//                    private transient ValueState<Boolean> flagState;
//                    private transient ValueState<Long> timerState;
//
//
//                    @Override
//                    public void open(Configuration parameters){
//
//                        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
//                                "flag",
//                                Types.BOOLEAN);
//                        flagState = getRuntimeContext().getState(flagDescriptor);
//
//                        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
//                                "timer-state",
//                                Types.LONG);
//                        timerState = getRuntimeContext().getState(timerDescriptor);
//                    }
//
//                    @Override
//                    public void processElement(MessageProcessInfo info, Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
//                        StringBuilder resultSb = new StringBuilder();
//                        resultSb.delete(0, resultSb.length());
//
//                        Set<String> cache = resultJedis.zrange(
////                                    info.getMessageType()
////                                            + ":" +
//                                "messageZSetCache"
//                                        + ":" + info.getUpid()
//                                        + ":" + info.getClinicNo()
//                                        + ":" + info.getApplyNo(),
//                                0, -1);
//
//                        String cacheState;
//                        String[] cacheInfo;
//                        cacheState = cache.iterator().next();
//                        cacheInfo = cacheState.split(":");
//
//                        long cacheMsgId = 0;
//                        long currentMsgId = 0;
//                        try {
//                            cacheMsgId = Long.parseLong(cacheInfo[1]);
//                            currentMsgId = Long.parseLong(info.getMessageId());
//                        } catch (Exception e) {
//
//                        }
//                        switch (info.getMessageType()) {
//                            case "OML_O21":
//                            case "OMG_O19":
//
//                                //?????????????????????
//                                //?????????????????????
//
//                                //OML_O21:273049144:NW:CIS:EAI:2021-03-30
//                                if ((!cacheInfo[0].equals(info.getMessageType())
//                                        || ("RU".equals(cacheInfo[2]) && "NW".equals(info.getState())))
//                                        && cacheMsgId < currentMsgId
//                                        && cacheInfo[4] != info.getMsgreceiver()
//                                ) {
//                                    log.info("??????????????????,???????????????{},???????????????{},???????????????{},{},{},???????????????{},{},{}",
//                                            DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
//                                            info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
//                                            cacheInfo[0], cacheInfo[1], cacheInfo[2]
//                                    );
//                                    resultSb = buildResult(resultSb, info.getMessageType() + "??????????????????", info, cacheInfo);
//
//                                    Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType()
//                                            + "??????????????????:"
//                                            //+ new Timestamp(window.getEnd())
//                                            + ":" + info.getUpid(), resultSb.toString());
//                                    out.collect(saveDate);
//                                }
//                                break;
//                            case "ORL_O22":
//                            case "ORG_O20":
//
//                                if (cacheInfo[0].equals(info.getMessageType()) && cacheInfo[4].equals(info.getMsgreceiver())) {
//                                    log.info("??????????????????,???????????????{},???????????????{},???????????????{},{},{},???????????????{},{},{}",
//                                            DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
//                                            info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
//                                            cacheInfo[0], cacheInfo[1], cacheInfo[2]
//                                    );
//                                    resultSb = buildResult(resultSb, info.getMessageType() + "??????????????????", info, cacheInfo);
//
//                                    Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType()
//                                            + "??????????????????:"
//                                            //+ new Timestamp(window.getEnd())
//                                            + ":" + info.getUpid(), resultSb.toString());
//                                    out.collect(saveDate);
//                                } else if ((("OML_O21".equals(cacheInfo[0]) && !"NW".equals(cacheInfo[2]))
//                                        || ("OMG_O19".equals(cacheInfo[0]) && !"NW".equals(cacheInfo[2])))
//                                        && (cacheInfo[4].equals(info.getMsgreceiver()))) {
//                                    log.info("??????NW????????????,???????????????{},???????????????{},???????????????{},{},{},???????????????{},{},{}",
//                                            DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
//                                            info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
//                                            cacheInfo[0], cacheInfo[1], cacheInfo[2]
//                                    );
//                                    resultSb = buildResult(resultSb, info.getMessageType() + "??????NW????????????", info, cacheInfo);
//
//                                    Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType()
//                                            + "??????NW????????????:"
//                                            //+ new Timestamp(window.getEnd())
//                                            + ":" + info.getUpid(), resultSb.toString());
//                                    out.collect(saveDate);
//                                }
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//
//
//                    @Override
//                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, String>> out) {
//                        // remove flag after 1 minute
//                        timerState.clear();
//                        flagState.clear();
//                    }
//
//                    private void cleanUp(Context ctx) throws Exception {
//                        // delete timer
//                        Long timer = timerState.value();
//                        ctx.timerService().deleteProcessingTimeTimer(timer);
//
//                        // clean up all state
//                        timerState.clear();
//                        flagState.clear();
//                    }
//
//                })
                .timeWindowAll(Time.hours(8))
                .apply(new AllWindowFunction<MessageProcessInfo, Tuple2<String, String>, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window, Iterable<MessageProcessInfo> input, Collector<Tuple2<String, String>> out) throws Exception {
                        // ?????????????????????
                        log.info("????????????:{}",new Timestamp(window.getEnd()));
                        StopWatch sw=new StopWatch();
                        sw.start();

                        int i=0;
                        StringBuilder resultSb = new StringBuilder();
                        resultSb.delete(0, resultSb.length());
                        for (MessageProcessInfo info : input) {
                            i++;
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
                            cacheState = cache.iterator().next();
                            cacheInfo = cacheState.split(":");
                            String cacheMsgType=cacheInfo[0];

                            String cacheMsgState=cacheInfo[2];
                            String cacheMsgSender=cacheInfo[3];
                            String cacheMsgReceiver=cacheInfo[4];
                            String cacheMsgDate=cacheInfo[5];

                            long cacheMsgId = 0;
                            long currentMsgId = 0;
                            try {
                                cacheMsgId = Long.parseLong(cacheInfo[1]);
                                currentMsgId = Long.parseLong(info.getMessageId());
                            } catch (Exception e) {

                            }



                            // ??????
                            // ??????
                            // ?????? CIS EAI OML^O21 NW???RU
                            // ?????? HIS EAI ORL^O22 OK
                            // ?????? HIS EAI ORL^O22 CR
                            // LIS?????? LIS HIS ORL^O22 OR
                            // LIS?????? LIS HIS ORL^O22 OC
                            // ??????
                            // ?????? CIS EAI OML^O21 NW???RU
                            // ?????? NIS EAI ORL^O22 OK
                            // LIS?????? LIS HIS ORL^O22 OR
                            // LIS?????? LIS HIS ORL^O22 OC



                            // ??????
                            // ??????
                            // ?????? CIS EAI OMG^O19 NW???RU
                            // ??????
                            // ?????? HIS EAI ORG^O20 OK
                            // ?????? HIS EAI ORG^O20 CR
                            // ?????? LWUS HIS ORG^O20 OR
                            // ???????????? LWUS HIS ORG^O20 MA
                            // ??????
                            // ?????? CIS EAI OMG^O19 NW???RU
                            // ?????? CIS EAI OMG^O19 CA
                            // ?????? NIS EAI ORG^O20 OK
                            // ?????? NIS EAI ORG^O20 CR
                            // ?????? LWUS HIS ORG^O20 OR
                            // ???????????? LWUS HIS ORG^O20 MA



                            //1???CIS?????????EAI
                            //1.1 ?????? NW
                            //1.2 NW ???RU ????????????
                            //1.3 ?????? ??????
                            //1.4 ????????????????????????
                            //2??????????????????CIS???HIS
                            //2.1 ????????????
                            //2.2 ????????????

                            switch (info.getMessageType()) {
                                case "OML_O21":
                                case "OMG_O19":

                                    //????????????????????? ???????????????????????????
                                    //?????????????????????

                                    //OML_O21:273049144:NW:CIS:EAI:2021-03-30
                                    if ((
                                            !cacheMsgType.equals(info.getMessageType())
                                            || ("RU".equals(cacheMsgState) && "NW".equals(info.getState())))
                                            && cacheMsgId < currentMsgId
                                            && cacheMsgReceiver != info.getMsgreceiver()
                                    ) {
                                        log.debug("??????????????????,???????????????{},???????????????{},???????????????{},{},{},???????????????{},{},{}",
                                                DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
                                                info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
                                                cacheMsgType, cacheMsgId, cacheMsgState
                                        );
                                        resultSb = buildResult(resultSb, info.getMessageType() + "??????????????????", info, cacheInfo);

                                        Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType() + "??????????????????:" + new Timestamp(window.getEnd()) + ":" + info.getUpid(), resultSb.toString());
                                        out.collect(saveDate);
                                    }
                                    break;
                                case "ORL_O22":
                                case "ORG_O20":

                                    if (
                                            cacheMsgType.equals(info.getMessageType())
                                                    && cacheMsgReceiver.equals(info.getMsgreceiver())
                                    ) {
                                        log.debug("??????????????????,???????????????{},???????????????{},???????????????{},{},{},???????????????{},{},{}",
                                                DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
                                                info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
                                                cacheMsgType, cacheMsgId, cacheMsgState
                                        );
                                        resultSb = buildResult(resultSb, info.getMessageType() + "??????????????????", info, cacheInfo);

                                        Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType() + "??????????????????:" + new Timestamp(window.getEnd()) + ":" + info.getUpid(), resultSb.toString());
                                        out.collect(saveDate);
                                    } else if ((("OML_O21".equals(cacheMsgType) && !"NW".equals(cacheMsgState))
                                            || ("OMG_O19".equals(cacheMsgType) && !"NW".equals(cacheMsgState)))
                                            && (cacheMsgReceiver.equals(info.getMsgreceiver()))) {
                                        log.debug("??????NW????????????,???????????????{},???????????????{},???????????????{},{},{},???????????????{},{},{}",
                                                DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"),
                                                info.getApplyNo(), info.getMessageType(), info.getMessageId(), info.getState(),
                                                cacheMsgType, cacheMsgId, cacheMsgState
                                        );
                                        resultSb = buildResult(resultSb, info.getMessageType() + "??????NW????????????", info, cacheInfo);

                                        Tuple2<String, String> saveDate = new Tuple2<>(info.getMessageType() + "??????NW????????????:" + new Timestamp(window.getEnd()) + ":" + info.getUpid(), resultSb.toString());
                                        out.collect(saveDate);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                        sw.stop();
                        log.info("????????????:{} ????????????:{},??????:{}",new Timestamp(window.getEnd()),i,sw.getTime());
                    }
                }).setParallelism(1)
                .addSink(redisSink).name("redis_sink");
        JobExecutionResult result = env.execute("My Flink Job");
        log.info("job execute time:{}", result.getNetRuntime(TimeUnit.SECONDS));
    }

    private static StringBuilder buildResult(StringBuilder result, String errType, MessageProcessInfo info, String[] cacheInfo) {
        return result.append(errType)
                .append(":???????????? ")
                .append(DateFormatUtils.format(info.getDateCreated(), "yyyy-MM-dd HH:mm:ss"))
                .append(" ???????????? ")
                .append(info.getApplyNo())

                .append(" --???????????? ")
                .append(info.getMessageType())
                .append(" ")
                .append(info.getMessageId())
                .append(" ")
                .append(info.getState())

                .append(" --???????????? ")
                .append(cacheInfo[0])
                .append(" ")
                .append(cacheInfo[1])
                .append(" ")
                .append(cacheInfo[2])

                .append("\n");
    }

}
