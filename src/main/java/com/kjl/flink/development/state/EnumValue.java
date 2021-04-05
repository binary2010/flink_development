package com.kjl.flink.development.state;

import java.util.EnumSet;

public interface EnumValue {
    /**
     * 获取枚举索引
     *
     * @return
     */
    int getIndex();

    /**
     * 获取枚举名称
     *
     * @return
     */
    String getName();

    /**
     * 获取枚举类指定index对应的枚举成员
     *
     * @param index index
     * @param clazz 枚举类
     * @param <E>   枚举类
     * @return 对的
     */
    static <E extends Enum<E> & EnumValue> E getByIndex(Integer index, Class<E> clazz) {
        return EnumSet.allOf(clazz).stream().filter(e -> e.getIndex() == index).findFirst().orElse(null);
    }

    /**
     * 获取枚举类指定index对应的枚举成员名称
     *
     * @param index
     * @param clazz
     * @return
     */
    static <E extends Enum<E> & EnumValue> String getNameByIndex(Integer index, Class<E> clazz) {
        EnumValue e = getByIndex(index, clazz);
        return null == e ? "" : e.getName();
    }
}
