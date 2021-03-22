package com.kjl.flink.development.util;

import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * blog：http://www.54tianzhisheng.cn/
 * 微信公众号：zhisheng
 */
public class GsonUtil {
    private final static Gson gson = new Gson();

    public static <T> T fromJson(String value, Class<T> type) {
        return gson.fromJson(value, type);
    }

    public static String toJson(Object value) {
        return gson.toJson(value);
    }

    public static byte[] toJSONBytes(Object value) {
        return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    }
}
