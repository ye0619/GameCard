package com.gamecard.template.manager;

import com.gamecard.template.loader.TemplateLoader;
import com.gamecard.template.model.Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板管理器实现
 * <p>
 * 通过 TemplateLoader 加载模板数据，并提供缓存与查询能力。
 *
 * @author GameCard Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateManagerImpl implements TemplateManager {

    private final TemplateLoader templateLoader;

    /** 模板缓存（id -> Template） */
    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

    @PostConstruct
    @Override
    public void init() {
        List<Template> templates = templateLoader.loadAll();
        templates.forEach(t -> templateCache.put(t.getId(), t));
        log.info("TemplateManager 初始化完成，共加载 {} 个模板", templateCache.size());
    }

    @Override
    public List<Template> getAllTemplates() {
        List<Template> values = new ArrayList<>(templateCache.values());
        Collections.sort(values, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return values;
    }

    @Override
    public Template getTemplate(String templateId) {
        return templateCache.get(templateId);
    }

    @Override
    public void registerTemplate(Template template) {
        if (template == null || template.getId() == null) {
            log.warn("注册模板失败：模板或模板 ID 为空");
            return;
        }
        templateCache.put(template.getId(), template);
        log.info("模板已注册：{} ({})", template.getName(), template.getId());
    }

    @Override
    public void unregisterTemplate(String templateId) {
        Template removed = templateCache.remove(templateId);
        if (removed != null) {
            log.info("模板已注销：{} ({})", removed.getName(), templateId);
        }
    }

    @Override
    public void refresh() {
        templateLoader.refresh();
        init();
    }

}
