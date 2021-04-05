package com.kjl.flink.development.state;

import java.util.EnumSet;

/**
 * @description: 工单状态流转接口
 * @Date : 2020/7/19 上午9:34
 * @Author : 石冬冬-Seig Heil
 */
public interface AttentionEvent extends EnumDesc {
    /**
     * 前置状态
     *
     * @return
     */
    EnumValue[] preventStatus();

    /**
     * 后置状态
     *
     * @return
     */
    EnumValue[] nextStatus();

    /**
     * 是否终态
     *
     * @return
     */
    boolean isEnd();

    /**
     * 校验状态
     *
     * @param currentStatus 当前状态
     * @return
     */
    boolean checkCurrentStatus(int currentStatus);

    /**
     * 校验状态
     *
     * @param expectStatus 期望状态
     * @return
     */
    boolean checkExpectStatus(int expectStatus);
}

