package com.gamecard.template;

import java.util.List;

/**
 * 模板管理器 - 模板的生命周期管理
 *
 * @author GameCard Team
 */
public interface TemplateManager {

    /**
     * 初始化所有模板
     */
    void init();

    /**
     * 获取所有可用模板
     *
     * @return 模板配置列表
     */
    List<TemplateConfig> getAvailableTemplates();

    /**
     * 注册新模板
     *
     * @param config 模板配置
     */
    void registerTemplate(TemplateConfig config);

    /**
     * 注销模板
     *
     * @param templateId 模板 ID
     */
    void unregisterTemplate(String templateId);

}
