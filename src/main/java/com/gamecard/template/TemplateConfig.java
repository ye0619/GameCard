package com.gamecard.template;

import lombok.Data;

/**
 * 模板配置（单个模板的元数据定义）
 *
 * @author GameCard Team
 */
@Data
public class TemplateConfig {

    /** 模板唯一标识 */
    private String id;

    /** 模板名称 */
    private String name;

    /** 模板描述 */
    private String description;

    /** 模板版本 */
    private String version;

    /** 模板作者 */
    private String author;

    /** 模板缩略图路径 */
    private String thumbnail;

}
