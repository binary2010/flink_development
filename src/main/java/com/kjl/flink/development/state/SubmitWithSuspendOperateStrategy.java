package com.kjl.flink.development.state;

@Slf4j
@Component
public class SubmitWithSuspendOperateStrategy extends AbstractSubmitOperateStrategy{

    static final Map<MoveToEnum,AttentionEventEnum> suspend_to_attention_event_map = new HashMap<>();

    static final Map<MoveToEnum,WorkOrderStatusEnum.SubStatusEnum> suspend_to_sub_status_map = new HashMap<>();

    static final Map<MoveToEnum,Integer> suspend_count_map = new HashMap<>();

    static {

        suspend_to_attention_event_map.put(MoveToEnum.SUSPENDED_AT_ONCE,AttentionEventEnum.SUSPENDED_AT_ONCE);
        suspend_to_attention_event_map.put(MoveToEnum.SUSPENDED_AT_TWICE,AttentionEventEnum.SUSPENDED_AT_TWICE);

        suspend_to_sub_status_map.put(MoveToEnum.SUSPENDED_AT_ONCE,WorkOrderStatusEnum.SubStatusEnum.SUSPENDED_AT_ONCE);
        suspend_to_sub_status_map.put(MoveToEnum.SUSPENDED_AT_TWICE,WorkOrderStatusEnum.SubStatusEnum.SUSPENDED_AT_TWICE);

        suspend_count_map.put(MoveToEnum.SUSPENDED_AT_ONCE,1);
        suspend_count_map.put(MoveToEnum.SUSPENDED_AT_TWICE,2);

        log.info("init... suspend_to_attention_event_map={}",suspend_to_attention_event_map.toString());
        log.info("init... suspend_to_sub_status_map={}",suspend_to_sub_status_map.toString());
        log.info("init... suspend_count_map={}",suspend_count_map.toString());
    }

    @Autowired
    DiamondConfigProxy diamondConfigProxy;

    @Override
    public void prepare(OperateContext context) {
        super.prepare(context);
        SurveyResult surveyResult = context.getSurveyResult();
        MoveToEnum moveToEnum = MoveToEnum.getByIndex(surveyResult.getMoveTo());
        AttentionEvent attentionEvent = suspend_to_attention_event_map.getOrDefault(moveToEnum,null);
        ATTENTION_EVENT_CONTEXT.set(attentionEvent);
        context.setAttentionEvent(attentionEvent);
    }

    @Override
    WorkOrder buildWorkOrder(OperateContext context){
        SurveyResult surveyResult = context.getSurveyResult();
        MoveToEnum moveToEnum = MoveToEnum.getByIndex(surveyResult.getMoveTo());
        WorkOrder workOrder = super.buildWorkOrder(context);
        workOrder.setSuspendedCount(suspend_count_map.getOrDefault(moveToEnum,0).intValue());
        workOrder.setMainStatus(WorkOrderStatusEnum.WAITING.getIndex());
        workOrder.setSubStatus(suspend_to_sub_status_map.get(moveToEnum).getIndex());
        workOrder.setIsFinished(Const.NON_INDEX);
        workOrder.setIsStore(Const.NON_INDEX);
        workOrder.setDelayedTime(context.getOperateParam().getDelayedTime());
        return workOrder;
    }

    @Override
    void operationExtend(OperateContext context) {
        long delayedTime = context.getOperateParam().getDelayedTime().getTime();
        int delayedSeconds = context.getOperateParam().getDelayedSeconds();
        WorkOrder workOrder = context.getWorkOrder();
        WorkOrderContext cxt = WorkOrderContext.buildSuspended(workOrder.getWorkCode(),workOrder.getCasePriority(),delayedTime);
        workOrderQueueManager.leftPush(cxt);

        WorkOrderCacheManager.CacheValue cacheValue = WorkOrderCacheManager.CacheValue.
                buildSuspended(workOrder.getWorkCode(),workOrder.getCasePriority(),delayedTime,delayedSeconds);
        workOrderCacheManager.setCacheInExpire(cacheValue);
        super.operationExtend(context);
    }

    @Override
    public void setDelayedTime(OperateContext context) {
        SurveyResult surveyResult = context.getSurveyResult();
        MoveToEnum moveToEnum = MoveToEnum.getByIndex(surveyResult.getMoveTo());
        DiamondConfig.SuspendOrderConfig suspendOrderConfig = diamondConfigProxy.suspendOrderConfig();
        Date delayedTime = TimeTools.createNowTime();
        int timeUnit = Calendar.HOUR_OF_DAY;
        int delayedSeconds = 0;
        int value = suspendOrderConfig.getConfig().getOrDefault(moveToEnum.name(),0);
        switch (suspendOrderConfig.getTimeUnit()){
            case "DAY":
                timeUnit = Calendar.DAY_OF_YEAR;
                delayedSeconds = value * 24 * 3600;
                break;
            case "HOUR":
                timeUnit = Calendar.HOUR_OF_DAY;
                delayedSeconds = value * 3600;
                break;
            case "MINUTE":
                timeUnit = Calendar.MINUTE;
                delayedSeconds = value * 60;
                break;
            case "SECOND":
                timeUnit = Calendar.SECOND;
                delayedSeconds = value;
                break;
            default:
                break;
        }

        TimeTools.addTimeField(delayedTime, timeUnit,value);
        context.getOperateParam().setDelayedTime(delayedTime);
        context.getOperateParam().setDelayedSeconds(delayedSeconds);
    }
}
