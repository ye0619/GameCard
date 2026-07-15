package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板字段定义
 * <p>
 * 描述模板中每个输入字段的元信息，用于前端动态渲染表单。
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateField {

    /** 字段标识（唯一，如 "name", "element"） */
    private String key;

    /** 字段显示标签（如 "角色名称"） */
    private String label;

    /**
     * 字段类型
     * <ul>
     *   <li>TEXT - 文本输入</li>
     *   <li>TEXTAREA - 多行文本</li>
     *   <li>IMAGE - 图片上传</li>
     *   <li>NUMBER - 数字</li>
     *   <li>SELECT - 下拉选择</li>
     *   <li>COLOR - 颜色选择</li>
     * </ul>
     */
    private String type;

    /** 是否必填 */
    private boolean required;

    /** 默认值 */
    private String defaultValue;

    /** 输入占位符提示 */
    private String placeholder;

    /** SELECT 类型时的选项（JSON 数组字符串） */
    private String options;

}
