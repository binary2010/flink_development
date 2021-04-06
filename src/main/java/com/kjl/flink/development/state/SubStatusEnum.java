package com.kjl.flink.development.state;

import java.util.stream.Stream;

public enum SubStatusEnum implements EnumValue {

    PENDING(200,"待处理",WorkOrderStatusEnum.PENDING),
    STORED(210,"转存量",WorkOrderStatusEnum.PENDING),

    PROCESSING_1_PHASE(410,"1阶段处理",WorkOrderStatusEnum.PROCESSING),
    PROCESSING_2_PHASE(420,"2阶段处理",WorkOrderStatusEnum.PROCESSING),
    PROCESSING_3_PHASE(430,"3阶段处理",WorkOrderStatusEnum.PROCESSING),

    SUSPENDED_AT_ONCE(610,"1次挂起",WorkOrderStatusEnum.WAITING),
    SUSPENDED_AT_TWICE(620,"2次挂起",WorkOrderStatusEnum.WAITING),

    FINISHED(800,"处理完成",WorkOrderStatusEnum.FINISHED),
    ;


    private int index;
    private String value;
    private EnumValue primaryStatus;

    SubStatusEnum(int index, String value,EnumValue primaryStatus){
        this.value = value;
        this.index = index;
        this.primaryStatus = primaryStatus;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return value;
    }

    public EnumValue getPrimaryStatus() {
        return primaryStatus;
    }

    /**
     * 根据索引获取对象
     * @param index
     * @return
     */
    public static SubStatusEnum getByIndex(int index){
        return Stream.of(SubStatusEnum.values()).filter(each -> each.getIndex() == index).findFirst().get();
    }
    /**
     * 根据索引获取名称
     * @param index
     * @return
     */
    public static String getNameByIndex(int index){
        SubStatusEnum find = Stream.of(SubStatusEnum.values()).filter(each -> each.getIndex() == index).findFirst().get();
        return null == find ? "" : find.getName();
    }
}
