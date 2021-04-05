package com.kjl.flink.development.state;

/**
 * @description: 工单挂起或转存，延迟操作
 * @Date : 2020/7/15 下午4:11
 * @Author : 石冬冬-Seig Heil
 */
public interface DelayedOperate {
    /**
     * 为上下文对象设置延迟的时间
     * 场景1：挂起操作，当前系统时间+n个小时
     * 场景2：转存操作，当前系统时间+n天
     * @param context
     * @return
     */
    void setDelayedTime(OperateContext context);
}