package com.kjl.flink.development.source;

import com.kjl.flink.streaming.entity.MessageBaseInfo;
import com.kjl.flink.streaming.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.List;
import java.util.Set;

@Slf4j
public class MessageBaseInfoFromRedis extends RichSourceFunction<MessageBaseInfo> {
    /**
     * Starts the source. Implementations can use the {@link SourceContext} emit
     * elements.
     *
     * <p>Sources that implement {@link CheckpointedFunction}
     * must lock on the checkpoint lock (using a synchronized block) before updating internal
     * state and emitting elements, to make both an atomic operation:
     *
     * <pre>{@code
     *  public class ExampleCountSource implements SourceFunction<Long>, CheckpointedFunction {
     *      private long count = 0L;
     *      private volatile boolean isRunning = true;
     *
     *      private transient ListState<Long> checkpointedCount;
     *
     *      public void run(SourceContext<T> ctx) {
     *          while (isRunning && count < 1000) {
     *              // this synchronized block ensures that state checkpointing,
     *              // internal state updates and emission of elements are an atomic operation
     *              synchronized (ctx.getCheckpointLock()) {
     *                  ctx.collect(count);
     *                  count++;
     *              }
     *          }
     *      }
     *
     *      public void cancel() {
     *          isRunning = false;
     *      }
     *
     *      public void initializeState(FunctionInitializationContext context) {
     *          this.checkpointedCount = context
     *              .getOperatorStateStore()
     *              .getListState(new ListStateDescriptor<>("count", Long.class));
     *
     *          if (context.isRestored()) {
     *              for (Long count : this.checkpointedCount.get()) {
     *                  this.count = count;
     *              }
     *          }
     *      }
     *
     *      public void snapshotState(FunctionSnapshotContext context) {
     *          this.checkpointedCount.clear();
     *          this.checkpointedCount.add(count);
     *      }
     * }
     * }</pre>
     *
     * @param ctx The context to emit elements to and for accessing locks.
     */
    @Override
    public void run(SourceContext<MessageBaseInfo> ctx) throws Exception {
        RedisUtil redisUtil = new RedisUtil();
        String key = "set:messageZSet:*";
        ScanParams scanParams = new ScanParams().match(key).count(10000);
        String cur = ScanParams.SCAN_POINTER_START;
        boolean cycleIsFinished = false;
        int count = 0;

        key = "set:*";
        scanParams = new ScanParams().match(key).count(10000);
        cur = ScanParams.SCAN_POINTER_START;
        cycleIsFinished = false;
        Jedis jedis = redisUtil.getJedis();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        while (!cycleIsFinished) {
            ScanResult<String> scanResult = jedis.scan(cur, scanParams);
            List<String> result = scanResult.getResult();
            for (String listKey : result) {
                Set<String> listValue = jedis.smembers(listKey);
            }
            count += result.size();
            cur = scanResult.getStringCursor();
            if ("0".equals(cur)) {
                cycleIsFinished = true;
            }
            log.info("count:{},cur:{}", count, cur);
        }
        stopWatch.stop();
        log.info("set count:{},time:{}", count, stopWatch.getTime());
        count = 0;
        stopWatch.reset();
    }

    /**
     * Cancels the source. Most sources will have a while loop inside the
     * {@link #run(SourceContext)} method. The implementation needs to ensure that the
     * source will break out of that loop after this method is called.
     *
     * <p>A typical pattern is to have an {@code "volatile boolean isRunning"} flag that is set to
     * {@code false} in this method. That flag is checked in the loop condition.
     *
     * <p>When a source is canceled, the executing thread will also be interrupted
     * (via {@link Thread#interrupt()}). The interruption happens strictly after this
     * method has been called, so any interruption handler can rely on the fact that
     * this method has completed. It is good practice to make any flags altered by
     * this method "volatile", in order to guarantee the visibility of the effects of
     * this method to any interruption handler.
     */
    @Override
    public void cancel() {

    }
}
