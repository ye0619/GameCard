package com.gamecard.template.loader;

import com.gamecard.template.model.Template;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads templates from classpath:templates/{game}/template.json files.
 * Uses Spring's Resource API with Jackson for JSON parsing.
 * <p>
 * Jackson is provided transitively by spring-boot-starter-web.
 * The JsonTemplateReader handles both Jackson 2.x and 3.x.
 */
@Slf4j
@Component
public class LocalTemplateLoader implements TemplateLoader {

    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

    @Value("${gamecard.template.scan-path:templates/*/template.json}")
    private String scanPath;

    private final JsonTemplateReader reader;

    public LocalTemplateLoader() {
        this.reader = new JsonTemplateReader();
    }

    @PostConstruct
    public void init() {
        loadAll();
        log.info("Local template loader initialized, loaded {} templates", templateCache.size());
    }

    @Override
    public List<Template> loadAll() {
        templateCache.clear();
        List<Template> loaded = scanAndLoad();
        loaded.forEach(t -> templateCache.put(t.getId(), t));
        if (loaded.isEmpty()) {
            log.warn("No template files found (scan: {}), using fallback", scanPath);
            loadFallback();
        }
        return new ArrayList<>(templateCache.values());
    }

    @Override
    public Template loadById(String templateId) {
        return templateCache.get(templateId);
    }

    @Override
    public void refresh() {
        loadAll();
        log.info("Template cache refreshed: {} templates", templateCache.size());
    }

    private List<Template> scanAndLoad() {
        List<Template> templates = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources("classpath:" + scanPath);
            for (Resource resource : resources) {
                if (!resource.exists() || !resource.isReadable()) {
                    continue;
                }
                try (InputStream is = resource.getInputStream()) {
                    Template template = reader.read(is);
                    if (template.getId() == null || template.getId().isBlank()) {
                        log.warn("Skipping template without id: {}", resource.getFilename());
                        continue;
                    }
                    templates.add(template);
                    log.info("Loaded template: {} (id={})", template.getName(), template.getId());
                } catch (IOException e) {
                    log.error("Failed to load template: {}", resource.getFilename(), e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan templates at: {}", scanPath, e);
        }

        return templates;
    }

    private void loadFallback() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource resource = resolver.getResource("classpath:templates/pokemon/template.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    Template template = reader.read(is);
                    templateCache.put(template.getId(), template);
                    log.info("Fallback template loaded: {}", template.getName());
                }
            }
        } catch (IOException e) {
            log.warn("Fallback template not available", e);
        }
    }
}
