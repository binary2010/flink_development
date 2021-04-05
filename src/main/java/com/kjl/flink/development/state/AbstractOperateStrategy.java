package com.kjl.flink.development.state;

import com.google.common.collect.Lists;

import java.util.List;

@Slf4j
public abstract class AbstractOperateStrategy implements OperateStrategy {
    /**
     * 事件
     */
    final static ThreadLocal<AttentionEvent> ATTENTION_EVENT_CONTEXT = new NamedThreadLocal<>("ATTENTION_EVENT_CONTEXT");

    @Autowired
    RedisService redisService;

    @Autowired
    Validator validator;

    /**
     * 执行
     * @param context
     */
    public void execute(OperateContext context){
        // 1、上下文依赖初始化
        prepare(context);
        // 2. 参数校验
        if(!paramCheck(context)) {
            return;
        }
        String workCode = context.getOperateParam().getWorkCode();
        log.info("ATTENTION_EVENT_CONTEXT={}",ATTENTION_EVENT_CONTEXT.toString());
        if(null == ATTENTION_EVENT_CONTEXT.get()){
            ATTENTION_EVENT_CONTEXT.set(context.getAttentionEvent());
        }
        String redisKey = redisService.getKey(MessageFormat.format(CarthageConst.AuditMemberKey.OPERATE_LOCK,workCode));
        try {
            // 3. 获取锁
            String lock = redisService.get(redisKey,String.class);
            if(!Objects.isNull(lock)){
                context.buildExecuteResultWithFailure("亲：工单[{" + workCode + "}]正在处理中，请稍后操作！");
                return;
            }
            if(!redisService.lock(redisKey)) {
                context.buildExecuteResultWithFailure("亲：工单[{" + workCode + "}]正在处理中，请稍后操作！");
                return;
            }
            // 4. 操作校验
            if(!operationCheck(context)) {
                return;
            }
            // 5. 本地操作
            ((AbstractOperateStrategy) AopContext.currentProxy()).operation(context);
            log.info("operate success attentionEvent={}, workCode={}", ATTENTION_EVENT_CONTEXT.get(), workCode);
        } catch (Exception e) {
            log.error("operate error attentionEvent={}, context={}", ATTENTION_EVENT_CONTEXT.get(), JSON.toJSONString(context), e);
            context.buildExecuteResultWithFailure("系统异常，操作失败！");
        } finally {
            ATTENTION_EVENT_CONTEXT.remove();
            redisService.unlock(redisKey);
        }
    }

    /**
     * 执行操作（扩展部分），常用语redis处理
     * @param context
     */
    abstract void operationExtend(OperateContext context);

    @Override
    public boolean paramCheck(OperateContext context) {
        String message;
        List<String> messages = validateInOval(context);
        if(CollectionsTools.isNotEmpty(messages)){
            message = MessageFormat.format("非法[workOrder={0}],[attentionEvent={1}],context实例对象参数校验不通过:{2}"
                    ,context.getOperateParam().getWorkCode(),ATTENTION_EVENT_CONTEXT.get(),messages.get(0));
            context.buildExecuteResultWithFailure(message);
        }
        return true;
    }

    @Override
    public boolean operationCheck(OperateContext context) {
        AttentionEvent attentionEvent = ATTENTION_EVENT_CONTEXT.get();
        WorkOrder workOrder = context.getWorkOrder();
        String message;
        if(Const.isYes(workOrder.getIsFinished())){
            message = MessageFormat.format("工单[workOrder={0}]已处理完结，禁止操作！[attentionEvent={1}]"
                    ,context.getOperateParam().getWorkCode(),attentionEvent);
            context.buildExecuteResultWithFailure(message);
            return false;
        }
        if(Objects.isNull(workOrder)){
            message = MessageFormat.format("非法[workOrder={0}]不存在，禁止操作！[attentionEvent={1}]"
                    ,context.getOperateParam().getWorkCode(),attentionEvent);
            context.buildExecuteResultWithFailure(message);
            return false;
        }
        boolean passCheck = context.getAttentionEvent().checkCurrentStatus(workOrder.getSubStatus());
        if(!passCheck){
            message = MessageFormat.format("[workCode={0}]工单状态={2}不符合操作场景！[attentionEvent={1}]"
                    ,workOrder.getWorkCode(),workOrder.getSubStatus(),attentionEvent);
            context.buildExecuteResultWithFailure(message);
        }
        return passCheck;
    }

    /**
     * 基于Oval校验实体对象
     * @param object
     * @return
     */
    List<String> validateInOval(Object object){
        List<String> messages = Lists.newArrayList();
        List<ConstraintViolation> violationList = validator.validate(object);
        if(CollectionsTools.isNotEmpty(violationList)){
            violationList.forEach(each -> messages.add(each.getMessage()));
        }
        return messages;
    }
}
