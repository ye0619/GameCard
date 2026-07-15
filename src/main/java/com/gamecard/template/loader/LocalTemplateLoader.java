package com.gamecard.template.loader;

import com.gamecard.template.model.Template;
import com.gamecard.template.model.TemplateField;
import com.gamecard.template.model.TemplateMetadata;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地模板加载器
 * <p>
 * 从本地资源目录加载模板数据。
 * 当前阶段使用 Mock 数据，后续可扩展为从 JSON 文件或数据库加载。
 *
 * @author GameCard Team
 */
@Slf4j
@Component
public class LocalTemplateLoader implements TemplateLoader {

    /** 模板缓存（id -> Template） */
    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadAll();
        log.info("本地模板加载完成，共 {} 个模板", templateCache.size());
    }

    @Override
    public List<Template> loadAll() {
        templateCache.clear();
        registerMockTemplates();
        return new ArrayList<>(templateCache.values());
    }

    @Override
    public Template loadById(String templateId) {
        return templateCache.get(templateId);
    }

    @Override
    public void refresh() {
        loadAll();
        log.info("模板缓存已刷新，共 {} 个模板", templateCache.size());
    }

    // ==================== Mock 数据 ====================

    private void registerMockTemplates() {
        registerPokemonTemplate();
    }

    /**
     * Pokémon 模板 - Mock 数据
     */
    private void registerPokemonTemplate() {
        List<TemplateField> fields = new ArrayList<>();

        fields.add(TemplateField.builder()
                .key("name")
                .label("宝可梦名称")
                .type("TEXT")
                .required(true)
                .placeholder("如：皮卡丘")
                .build());

        fields.add(TemplateField.builder()
                .key("element")
                .label("属性")
                .type("SELECT")
                .required(true)
                .defaultValue("电")
                .placeholder("选择属性")
                .options("[\"火\",\"水\",\"草\",\"电\",\"超能力\",\"格斗\",\"岩石\",\"地面\",\"飞行\",\"虫\",\"毒\",\"一般\",\"幽灵\",\"冰\",\"龙\",\"恶\",\"钢\",\"妖精\"]")
                .build());

        fields.add(TemplateField.builder()
                .key("rarity")
                .label("稀有度")
                .type("SELECT")
                .required(true)
                .defaultValue("普通")
                .placeholder("选择稀有度")
                .options("[\"普通\",\"稀有\",\"史诗\",\"传说\"]")
                .build());

        fields.add(TemplateField.builder()
                .key("skill")
                .label("技能")
                .type("TEXT")
                .required(false)
                .placeholder("如：十万伏特")
                .build());

        fields.add(TemplateField.builder()
                .key("description")
                .label("描述")
                .type("TEXTAREA")
                .required(false)
                .placeholder("输入宝可梦描述...")
                .build());

        fields.add(TemplateField.builder()
                .key("image")
                .label("宝可梦图片")
                .type("IMAGE")
                .required(true)
                .build());

        Template pokemon = Template.builder()
                .id("pokemon")
                .name("宝可梦")
                .description("经典 Pokémon 风格游戏卡片，展示宝可梦的名称、属性与技能")
                .author("GameCard Team")
                .version("1.0.0")
                .previewImage("/images/preview/pokemon.png")
                .tags(List.of("经典", "日式", "RPG"))
                .fields(fields)
                .metadata(TemplateMetadata.builder()
                        .createTime(LocalDateTime.of(2026, 7, 1, 0, 0))
                        .updateTime(LocalDateTime.now())
                        .templateVersion("1.0.0")
                        .minAppVersion("0.1.0")
                        .build())
                .build();

        templateCache.put(pokemon.getId(), pokemon);
    }
}
