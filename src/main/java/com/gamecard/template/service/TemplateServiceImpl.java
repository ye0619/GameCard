package com.gamecard.template.service;

import com.gamecard.exception.BusinessException;
import com.gamecard.template.manager.TemplateManager;
import com.gamecard.template.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模板服务实现
 *
 * @author GameCard Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateManager templateManager;

    @Override
    public List<Template> listTemplates() {
        return templateManager.getAllTemplates();
    }

    @Override
    public Template getTemplate(String templateId) {
        Template template = templateManager.getTemplate(templateId);
        if (template == null) {
            throw new BusinessException("模板不存在: " + templateId);
        }
        return template;
    }

}
