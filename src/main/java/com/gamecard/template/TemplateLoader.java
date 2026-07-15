package com.gamecard.template;

import java.util.List;

/**
 * 模板加载器 - 负责从文件系统/数据库加载模板资源
 *
 * @author GameCard Team
 */
public interface TemplateLoader {

    /**
     * 加载所有可用模板
     *
     * @return 模板配置列表
     */
    List<TemplateConfig> loadTemplates();

    /**
     * 根据 ID 加载指定模板
     *
     * @param templateId 模板 ID
     * @return 模板配置，未找到返回 null
     */
    TemplateConfig loadTemplate(String templateId);

    /**
     * 刷新模板缓存
     */
    void refresh();

}
