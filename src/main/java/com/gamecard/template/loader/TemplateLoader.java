package com.gamecard.template.loader;

import com.gamecard.template.model.Template;

import java.util.List;

/**
 * 模板加载器接口
 * <p>
 * 负责从不同来源（本地文件、数据库、远程仓库）加载模板数据。
 * 每种加载方式对应一个独立实现。
 *
 * @author GameCard Team
 */
public interface TemplateLoader {

    /**
     * 加载所有可用模板
     *
     * @return 模板列表
     */
    List<Template> loadAll();

    /**
     * 根据 ID 加载指定模板
     *
     * @param templateId 模板 ID
     * @return 模板对象，未找到返回 null
     */
    Template loadById(String templateId);

    /**
     * 刷新/重新加载模板
     */
    void refresh();

}
