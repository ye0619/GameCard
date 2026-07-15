package com.gamecard.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * ID 工具类
 *
 * @author GameCard Team
 */
@Slf4j
public final class IdUtil {

    private IdUtil() {
    }

    /**
     * 生成 UUID（去除横线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成雪花算法 ID（后续接入 MyBatis-Plus 后使用）
     */
    public static long snowflakeId() {
        // TODO: 接入 MyBatis-Plus 雪花算法 ID 生成器
        throw new UnsupportedOperationException("暂未实现");
    }

}
