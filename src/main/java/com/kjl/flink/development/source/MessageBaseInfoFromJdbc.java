package com.kjl.flink.development.source;

import com.google.common.collect.Queues;
import com.kjl.flink.development.entity.MessageBaseInfo;
import com.kjl.flink.development.util.MySQLUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.*;

import static com.kjl.flink.development.util.MySQLUtil.converClobToString;


@Slf4j
public class MessageBaseInfoFromJdbc extends RichParallelSourceFunction<MessageBaseInfo> implements Serializable {

    private static CountDownLatch countDownLatch;
    // Client 线程的默认数量
    private final int DEFAULT_CLIENT_THREAD_NUM = 4;
    // 数据缓冲队列的默认容量
    private final int DEFAULT_QUEUE_CAPACITY = 5000;
    //PreparedStatement ps;
    private Connection connection;
    private volatile boolean isRunning = true;
    private LinkedBlockingQueue<String> bufferQueue;


    //private Semaphore semaphore = new Semaphore(1);
    private CyclicBarrier clientBarrier;
    private final LinkedBlockingQueue<MessageBaseInfo> msgQueue = Queues.newLinkedBlockingQueue();
    private ScheduledExecutorService queryExecutor;


    private ScheduledExecutorService consumeExecutor;

    private boolean taskFinish = false;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        queryExecutor = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("scheduled-pool-query-%d").daemon(true).build());


        consumeExecutor = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("scheduled-pool-consume-%d").daemon(true).build());

    }

    @Override
    public void close() throws Exception {
        log.info("开始关闭");


        super.close();
        if (connection != null) {
            connection.close();
        }
//        if (ps != null) {
//            ps.close();
//        }
    }

    @Override
    public void run(SourceContext<MessageBaseInfo> ctx) throws Exception {
        Date currentDate = new Date();
        Date beginDate = DateUtils.parseDate("2019-12-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
        currentDate = DateUtils.parseDate("2020-01-09 12:30:00", "yyyy-MM-dd HH:mm:ss");
        //currentDate = DateUtils.parseDate("2019-12-02 12:30:00", "yyyy-MM-dd HH:mm:ss");
        long diffHours = TimeUnit.HOURS.convert(currentDate.getTime() - beginDate.getTime(), TimeUnit.MILLISECONDS);

        countDownLatch = new CountDownLatch(Math.toIntExact(diffHours));

        ConsumerMessageThread consumerMessageThread = new ConsumerMessageThread(diffHours, ctx);
        consumeExecutor.submit(consumerMessageThread);

        for (int i = 0; i < diffHours; i++) {
            MultiThreadConsumerClient consumerClient = new MultiThreadConsumerClient(i, beginDate);
            queryExecutor.submit(consumerClient);
        }
        //必须保证线程完成，否则会优先close
        while (!taskFinish) {
            countDownLatch.await();
        }
        queryExecutor.shutdown();
        queryExecutor.awaitTermination(10, TimeUnit.SECONDS);

        consumeExecutor.shutdown();
        consumeExecutor.awaitTermination(10, TimeUnit.SECONDS);


    }

    @Override
    public void cancel() {
        isRunning = false;
    }


    public class ConsumerMessageThread implements Runnable {
        private final SourceContext<MessageBaseInfo> collect;
        private final Long diffHours;

        public ConsumerMessageThread(long diffHours, SourceContext<MessageBaseInfo> ctx) {
            collect = ctx;
            this.diffHours = diffHours;
        }

        @Override
        public void run() {
            try {
                while (countDownLatch.getCount() == diffHours) {
                    continue;
                }
                log.info("开始消费");
                while (!(countDownLatch.getCount() == 0
                        && msgQueue.isEmpty())
                ) {
                    //log.info("消费：{},count:{}", msgQueue.size(),countDownLatch.getCount());
                    collect.collect(msgQueue.poll());
                    //log.info("消费结束：{},count:{}", msgQueue.size(),countDownLatch.getCount());
                }
                log.info("消费完成{},count:{}", msgQueue.size(), countDownLatch.getCount());
                countDownLatch.await();
                taskFinish = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class MultiThreadConsumerClient implements Runnable {
        //private PreparedStatement ps;
        private Date begin;
        private Date end;

        MultiThreadConsumerClient(int i, Date beginDate) {
            try {
                //String sql = "select a.id id,a.msg_id msg_id,a.msg_type msg_type,a.date_created date_created,a.raw_data raw_data from hl7_message a where a.date_created > to_date(?,'yyyy-MM-dd hh24:mi:ss') and a.date_created<=to_date(?,'yyyy-MM-dd hh24:mi:ss') and a.type='Source' and (a.msg_type='OML^O21'or a.msg_type='ORL^O22' or a.msg_type='OMG^O19' or a.msg_type='ORG^O20') order by a.date_created,a.msg_id";
                begin = DateUtils.addHours(beginDate, i);
                end = DateUtils.addHours(beginDate, i + 1);
//                this.ps = MySQLUtil.getConnection().prepareStatement(sql);
//                this.ps.setString(1, DateFormatUtils.format(begin, "yyyy-MM-dd HH:mm:ss"));
//                this.ps.setString(2, DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {

                int count;
                try (Connection connection = MySQLUtil.getConnection()) {
                    String sql = "select a.id id,a.msg_id msg_id,a.msg_type msg_type,a.date_created date_created,a.raw_data raw_data from hl7_message a where a.date_created > to_date(?,'yyyy-MM-dd hh24:mi:ss') and a.date_created<=to_date(?,'yyyy-MM-dd hh24:mi:ss') and a.type='Source' and (a.msg_type='OML^O21'or a.msg_type='ORL^O22' or a.msg_type='OMG^O19' or a.msg_type='ORG^O20') order by a.date_created,a.msg_id";
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, DateFormatUtils.format(begin, "yyyy-MM-dd HH:mm:ss"));
                    ps.setString(2, DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss"));
                    ps.setFetchSize(2000);
                    ResultSet resultSet = ps.executeQuery();
                    count = 0;
                    while (resultSet.next()) {
                        MessageBaseInfo baseInfo = new MessageBaseInfo(
                                resultSet.getString("id"),
                                resultSet.getString("msg_id"),
                                resultSet.getString("msg_type"),
                                resultSet.getTimestamp("date_created"),
                                converClobToString(resultSet.getClob("raw_data"))
                        );
                        msgQueue.put(baseInfo);
                        count++;
                    }
//                    if (ps != null) {
//                        ps.getConnection().close();
//                        ps.close();
//                    }

                }
                log.info("单次查询:{},{},消息数量：{},{}", begin, end, count, countDownLatch.getCount());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }

        }


    }
}
