package com.kjl.flink.development.state;

/**
 * @description: 工单操作策略接口
 * 调用顺序：{@link OperateStrategy#prepare(OperateContext)} -> {@link OperateStrategy#paramCheck(OperateContext)}
 *  -> {@link OperateStrategy#operationCheck(OperateContext)} -> {@link OperateStrategy#operation(OperateContext)}
 * @Date : 2020/7/15 下午4:11
 * @Author : 石冬冬-Seig Heil
 */
public interface OperateStrategy {
    /**
     * 上下文相关初始化
     * 子类可以完成对context相关成员变量的城市化，以及子类的成员变量
     * @param context
     */
    void prepare(OperateContext context);
    /**
     * context成员变量参数校验
     * @param context
     * @return
     */
    boolean paramCheck(OperateContext context);
    /**
     * 操作校验，是否可以执行操作
     * @param context
     * @return
     */
    boolean operationCheck(OperateContext context);
    /**
     * 执行操作，包括入库更新操作若干业务处理
     * @param context
     */
    void operation(OperateContext context);
}
