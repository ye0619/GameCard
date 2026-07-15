package com.gamecard.template.service;

import com.gamecard.template.model.Template;

import java.util.List;

/**
 * 模板服务接口
 * <p>
 * 对外暴露的模板操作 API，供 Controller 调用。
 * 负责模板相关的业务逻辑编排。
 *
 * @author GameCard Team
 */
public interface TemplateService {

    /**
     * 获取所有可用模板列表
     *
     * @return 模板列表
     */
    List<Template> listTemplates();

    /**
     * 根据 ID 获取模板详情
     *
     * @param templateId 模板 ID
     * @return 模板对象
     * @throws com.gamecard.exception.BusinessException 模板不存在时抛出
     */
    Template getTemplate(String templateId);

}
