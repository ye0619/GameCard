package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板 - 游戏卡片模板的核心模型
 * <p>
 * 每一种游戏风格（如 Pokémon、原神、Minecraft）对应一个独立模板。
 * 模板定义了卡片的布局、配色、字段映射和样式信息。
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    /** 模板唯一标识（如 "pokemon"、"genshin"） */
    private String id;

    /** 模板名称（如 "宝可梦"） */
    private String name;

    /** 模板描述 */
    private String description;

    /** 模板作者 */
    private String author;

    /** 模板版本 */
    private String version;

    /** 预览图路径 */
    private String previewImage;

    /** 模板标签（用于分类筛选） */
    private List<String> tags;

    /** 模板字段定义列表 */
    private List<TemplateField> fields;

    /** 模板元数据 */
    private TemplateMetadata metadata;

}
