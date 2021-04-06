package com.kjl.flink.development.state;

import java.util.Arrays;

public enum AttentionEventEnum implements AttentionEvent {
    INIT_ORDER(1,"初始化","第一次创建工单",
            new EnumValue[]{
            },
            new EnumValue[]{
                    SubStatusEnum.PENDING,
            },
            false),
    ACCEPT_ORDER(2,"领单","审核专员领单",
            new EnumValue[]{
                    SubStatusEnum.PENDING,
                    SubStatusEnum.STORED,
            },
            new EnumValue[]{
                    SubStatusEnum.PROCESSING_1_PHASE,
                    SubStatusEnum.PROCESSING_2_PHASE,
                    SubStatusEnum.PROCESSING_3_PHASE
            },
            false),
    STORE_ORDER(3,"转存","审核专员转存订单",
            new EnumValue[]{
                    SubStatusEnum.PROCESSING_1_PHASE,
                    SubStatusEnum.PROCESSING_2_PHASE,
                    SubStatusEnum.PROCESSING_3_PHASE
            },
            new EnumValue[]{
                    SubStatusEnum.STORED,
            },
            false),
    SUSPENDED_AT_ONCE(4,"一次挂起","审核专员一次挂起订单",
            new EnumValue[]{
                    SubStatusEnum.PROCESSING_1_PHASE
            },
            new EnumValue[]{
                    SubStatusEnum.SUSPENDED_AT_ONCE
            },
            false),
    SUSPENDED_AT_TWICE(5,"二次挂起","审核专员二次挂起订单",
            new EnumValue[]{
                    SubStatusEnum.PROCESSING_2_PHASE
            },
            new EnumValue[]{
                    SubStatusEnum.SUSPENDED_AT_TWICE
            },
            false),
    DELAYED_SCHEDULED(6,"延迟既定时间处理工单","挂起|转存的延迟队列处理",
            new EnumValue[]{
                    SubStatusEnum.SUSPENDED_AT_ONCE,
                    SubStatusEnum.SUSPENDED_AT_TWICE,
                    SubStatusEnum.STORED
            },
            new EnumValue[]{
                    SubStatusEnum.PENDING,
            },
            false),
    ASSIGN_ORDER(7,"指派订单","持有工单的人当前不坐席或者离职，需要主管指派给坐席专员处理",
            new EnumValue[]{
                    SubStatusEnum.PENDING,
                    SubStatusEnum.PROCESSING_1_PHASE,
                    SubStatusEnum.PROCESSING_2_PHASE,
                    SubStatusEnum.PROCESSING_3_PHASE
            },
            new EnumValue[]{
                    SubStatusEnum.PROCESSING_1_PHASE,
                    SubStatusEnum.PROCESSING_2_PHASE,
                    SubStatusEnum.PROCESSING_3_PHASE
            },
            false),
    FINISH_ORDER(8,"处理完结","审核专员处理完结",
            new EnumValue[]{
                    SubStatusEnum.PROCESSING_1_PHASE,
                    SubStatusEnum.PROCESSING_2_PHASE,
                    SubStatusEnum.PROCESSING_3_PHASE
            },
            new EnumValue[]{
                    SubStatusEnum.FINISHED,
            },
            true),
    ;

    /**
     * 索引
     */
    private int index;
    /**
     * 名称
     */
    private String name;
    /**
     * 名称
     */
    private String desc;
    /**
     * 前置状态
     * @return
     */
    private EnumValue[] preventStatus;
    /**
     * 后置状态
     * @return
     */
    private EnumValue[] nextStatus;
    /**
     * 是否终态
     * @return
     */
    private boolean isEnd;


    AttentionEventEnum(int index, String name, String desc,
                       EnumValue[] preventStatus,
                       EnumValue[] nextStatus,
                       boolean isEnd){
        this.index = index;
        this.name = name;
        this.desc = desc;
        this.preventStatus = preventStatus;
        this.nextStatus = nextStatus;
        this.isEnd = isEnd;
    }


    @Override
    public EnumValue[] preventStatus() {
        return preventStatus;
    }

    @Override
    public EnumValue[] nextStatus() {
        return nextStatus;
    }

    @Override
    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean checkCurrentStatus(int currentStatus){
        SubStatusEnum currentStatusEnum = SubStatusEnum.getByIndex(currentStatus);
        return Arrays.asList(this.preventStatus()).contains(currentStatusEnum);
    }

    @Override
    public boolean checkExpectStatus(int expectStatus){
        SubStatusEnum expectStatusEnum = SubStatusEnum.getByIndex(expectStatus);
        return Arrays.asList(this.nextStatus()).contains(expectStatusEnum);
    }

}
