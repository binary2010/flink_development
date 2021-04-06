package com.kjl.flink.development.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SubmitWithStoreOperateStrategy extends AbstractSubmitOperateStrategy{
    /**
     * 转存天数 换算 秒数
     */
    static final int DAY_TO_SECONDS = 24 * 60 * 60;

    @Override
    public void prepare(OperateContext context) {
        ATTENTION_EVENT_CONTEXT.set(AttentionEventEnum.STORE_ORDER);
        context.setAttentionEvent(AttentionEventEnum.STORE_ORDER);
        super.prepare(context);
    }

    @Override
    public boolean paramCheck(OperateContext context) {
        if(Objects.isNull(context.getSurveyResult().getDelayedDays())){
            context.buildExecuteResultWithFailure("[surveyResult.delayedDays]为空！");
        }
        if(context.getSurveyResult().getDelayedDays() == 0){
            context.buildExecuteResultWithFailure("等待天数[delayedDays]必须大于0！");
        }
        return super.paramCheck(context);
    }

    @Override
    WorkOrder buildWorkOrder(OperateContext context){
        WorkOrder workOrder = super.buildWorkOrder(context);
        workOrder.setMainStatus(WorkOrderStatusEnum.PENDING.getIndex());
        workOrder.setSubStatus(WorkOrderStatusEnum.SubStatusEnum.STORED.getIndex());
        workOrder.setIsFinished(Const.NON_INDEX);
        workOrder.setIsStore(Const.YES_INDEX);
        //setSuspendedCount 这里需要重置为0，转存后派单流程状态依赖该字段
        workOrder.setSuspendedCount(0);
        workOrder.setDelayedTime(context.getOperateParam().getDelayedTime());
        return workOrder;
    }

    @Override
    void operationExtend(OperateContext context) {
        long delayedTime = context.getOperateParam().getDelayedTime().getTime();
        int delayedSeconds = context.getOperateParam().getDelayedSeconds();
        WorkOrder workOrder = context.getWorkOrder();
        WorkOrderContext cxt = WorkOrderContext.buildStored(workOrder.getWorkCode(),workOrder.getCasePriority(),delayedTime);
        workOrderQueueManager.leftPush(cxt);

        WorkOrderCacheManager.CacheValue cacheValue = WorkOrderCacheManager.CacheValue.
                buildStored(workOrder.getWorkCode(),workOrder.getCasePriority(),delayedTime,delayedSeconds);
        workOrderCacheManager.setCacheInExpire(cacheValue);

        super.operationExtend(context);
    }

    @Override
    public void setDelayedTime(OperateContext context) {
        int delayedDays = context.getSurveyResult().getDelayedDays();
        Date delayedTime = TimeTools.createNowTime();
        TimeTools.addTimeField(delayedTime, Calendar.DAY_OF_YEAR,delayedDays);
        context.getOperateParam().setDelayedTime(delayedTime);
        context.getOperateParam().setDelayedSeconds(delayedDays * DAY_TO_SECONDS);
    }
}
