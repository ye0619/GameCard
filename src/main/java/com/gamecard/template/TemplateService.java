package com.gamecard.template;

import java.util.List;

/**
 * 模板服务接口 - 对外暴露的模板操作 API
 *
 * @author GameCard Team
 */
public interface TemplateService {

    /**
     * 获取所有模板列表
     *
     * @return 模板配置列表
     */
    List<TemplateConfig> listTemplates();

    /**
     * 获取模板详情
     *
     * @param templateId 模板 ID
     * @return 模板配置
     */
    TemplateConfig getTemplate(String templateId);

}
