package com.kjl.flink.development.state;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSubmitOperateStrategy extends AbstractOperateStrategy implements DelayedOperate {

    @Autowired
    DisposeOrderService disposeOrderService;

    @Autowired
    WorkOrderService workOrderService;

    @Autowired
    SurveyResultService surveyResultService;

    @Autowired
    MemberQueueManager memberQueueManager;

    @Autowired
    WorkOrderQueueManager workOrderQueueManager;

    @Autowired
    WorkOrderCacheManager workOrderCacheManager;

    /**
     * 构建工单对象，用于更新工单相关字段
     * @param context
     * @return WorkOrder
     */
    WorkOrder buildWorkOrder(OperateContext context){
        OperateContext.OperateParam operateParam = context.getOperateParam();
        Date currentTime = TimeTools.createNowTime();
        String workCode = context.getWorkOrder().getWorkCode();
        WorkOrder workOrder = WorkOrder.builder()
                .id(context.getSurveyResult().getRelationId())
                .workCode(workCode)
                .handledTime(currentTime)
                .handlerCode(operateParam.getHandlerCode())
                .handlerName(operateParam.getHandlerName())
                .modified(currentTime)
                .build();
        return workOrder;
    }


    @Override
    public void prepare(OperateContext context) {
        WorkOrder workOrder = workOrderService.queryRecord(context.getSurveyResult().getRelationId());
        context.getOperateParam().setWorkCode(workOrder.getWorkCode());
        context.setWorkOrder(workOrder);
        // 初始化延迟时间
        setDelayedTime(context);
    }

    @Override
    public boolean paramCheck(OperateContext context) {
        if(super.paramCheck(context)){
            return true;
        }
        if(Objects.isNull(context.getSurveyResult())){
            context.buildExecuteResultWithFailure("[SurveyResult]为空！");
            return false;
        }
        if(Objects.isNull(context.getAttentionEvent())){
            context.buildExecuteResultWithFailure("[AttentionEvent]为空，上下文未初始化！");
            return false;
        }
        List<String> messages = validateInOval(context);
        if(CollectionsTools.isNotEmpty(messages)){
            context.buildExecuteResultWithFailure("[surveyResult]参数校验不通过:" + messages.get(0));
            return false;
        }
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class,timeout = 5000)
    public void operation(OperateContext context) {
        Date currentTime = TimeTools.createNowTime();
        WorkOrder originWorkOrder = context.getWorkOrder();
        String workCode = originWorkOrder.getWorkCode();

        //1、更新GPS工单相关字段
        WorkOrder workOrder = buildWorkOrder(context);
        boolean passCheck = context.getAttentionEvent().checkExpectStatus(workOrder.getSubStatus());
        if(!passCheck){
            context.buildExecuteResultWithFailure(MessageFormat.format("workCode={0},当前工单状态{1}==>{2}不符合操作！"
                    ,workOrder.getWorkCode(),workOrder.getSubStatus(),originWorkOrder.getSubStatus()));
            return;
        }
        log.info("执行[workOrder]更新操作，workCode={}，workOrder={}",workCode, JSONObject.toJSONString(workOrder));
        workOrderService.updateByPrimaryKeySelective(workOrder);

        SurveyResult surveyResult = context.getSurveyResult();
        String appCode = surveyResult.getAppCode();
        MoveToEnum moveToEnum = MoveToEnum.getByIndex(surveyResult.getMoveTo());

        //选择【处理完结】处理完结操作
        if(moveToEnum == MoveToEnum.FINISHED){
            DisposeOrder disposeOrder = DisposeOrder.builder()
                    .id(surveyResult.getRelationId())
                    .appCode(appCode)
                    .isAllow(Const.NON_INDEX)
                    .isFinished(Const.YES_INDEX)
                    .finishedTime(currentTime)
                    .modified(currentTime)
                    .build();
            log.info("执行[disposeOrder]更新操作，appCode={}，disposeOrder={}",appCode,JSONObject.toJSONString(disposeOrder));
            disposeOrderService.updateByPrimaryKeySelective(disposeOrder);
        }
        //新增调查记录
        log.info("执行[surveyResult]新增，workCode={}，surveyResult={}",workCode, JSONObject.toJSONString(surveyResult));
        surveyResultService.insertRecord(surveyResult);
        // 外部操作
        operationExtend(context);
    }

    @Override
    void operationExtend(OperateContext context) {
        // 审核专员持有单量 -1
        String handlerCode = context.getWorkOrder().getHandlerCode();
        String currentDay = TimeTools.format4YYYYMMDD(TimeTools.createNowTime());
        //1.1、更新 holdingCount 中当前人持有单量
        memberQueueManager.incrementHoldingCountOfThisHandler(handlerCode,-1);
        //1.2、更新人员队列中的持有单量
        memberQueueManager.updateAcceptUserVoToUsersQueue(Boolean.FALSE,currentDay,users -> users.stream()
                .filter(each -> each.getUserId().equals(handlerCode)).forEach(each -> {
                    Integer originCount = Optional.ofNullable(each.getHoldingCount()).orElse(0);
                    each.setHoldingCount(originCount.intValue() == 0 ? 0 : -- originCount );
                }));
    }
}
