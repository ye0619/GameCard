package com.gamecard.template.manager;

import com.gamecard.template.model.Template;

import java.util.List;

/**
 * 模板管理器接口
 * <p>
 * 负责模板的注册、发现与生命周期管理。
 * 是模板系统的核心门面，屏蔽底层加载细节。
 *
 * @author GameCard Team
 */
public interface TemplateManager {

    /**
     * 初始化：加载所有可用模板
     */
    void init();

    /**
     * 获取所有可用模板
     *
     * @return 模板列表
     */
    List<Template> getAllTemplates();

    /**
     * 根据 ID 获取模板详情
     *
     * @param templateId 模板 ID
     * @return 模板对象，未找到返回 null
     */
    Template getTemplate(String templateId);

    /**
     * 注册新模板
     *
     * @param template 模板对象
     */
    void registerTemplate(Template template);

    /**
     * 注销模板
     *
     * @param templateId 模板 ID
     */
    void unregisterTemplate(String templateId);

    /**
     * 刷新所有模板
     */
    void refresh();

}
