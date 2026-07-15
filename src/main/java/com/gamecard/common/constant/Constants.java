package com.gamecard.common.constant;

/**
 * 全局常量
 *
 * @author GameCard Team
 */
public final class Constants {

    private Constants() {
    }

    /** 应用名称 */
    public static final String APP_NAME = "GameCard";

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 文件上传最大大小（10MB） */
    public static final long MAX_UPLOAD_SIZE = 10 * 1024 * 1024;

    /** 允许的图片格式 */
    public static final String[] ALLOWED_IMAGE_TYPES = {"png", "jpg", "jpeg", "webp"};

}
