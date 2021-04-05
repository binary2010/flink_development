package com.kjl.flink.development.state;

import java.util.stream.Stream;

public enum WorkOrderStatusEnum implements EnumValue {

    PENDING(200,"待处理"),
    PROCESSING(400,"处理中"),
    WAITING(600,"等待中"),
    FINISHED(800,"处理完结"),
    ;


    private int index;
    private String value;

    WorkOrderStatusEnum(int index, String value ){
        this.value = value;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return value;
    }

    /**
     * 子状态枚举
     * @Date : 2020/7/10 下午4:04
     * @Author : 石冬冬-Seig Heil
     * 子状态(2XX-待处理[200-待处理;210-转存量];4XX-处理中[410-1阶段处理;420-2阶段处理;430-3阶段处理];6XX-等待中[610-1次挂起;620-2次挂起];8XX-处理完结[800-处理完成])
     */


    /**
     * 根据索引获取对象
     * @param index
     * @return
     */
    public static WorkOrderStatusEnum getByIndex(int index){
        return Stream.of(WorkOrderStatusEnum.values()).filter(each -> each.getIndex() == index).findFirst().get();
    }
    /**
     * 根据索引获取名称
     * @param index
     * @return
     */
    public static String getNameByIndex(int index){
        WorkOrderStatusEnum find = Stream.of(WorkOrderStatusEnum.values()).filter(each -> each.getIndex() == index).findFirst().get();
        return null == find ? "" : find.getName();
    }

    /**
     * 根据一级状态获取二级状态
     * @param mainStatus
     * @return
     */
    public static List<EnumValue> getSubStatus(int mainStatus){
        return Stream.of(SubStatusEnum.values()).filter(each -> each.getPrimaryStatus().getIndex() == mainStatus).collect(toList());
    }
}
