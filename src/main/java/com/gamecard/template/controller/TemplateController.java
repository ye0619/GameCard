package com.gamecard.template.controller;

import com.gamecard.common.result.Result;
import com.gamecard.template.model.Template;
import com.gamecard.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模板管理 API
 *
 * @author GameCard Team
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 获取所有可用模板列表
     * GET /api/templates
     */
    @GetMapping
    public Result<List<Template>> listTemplates() {
        List<Template> templates = templateService.listTemplates();
        return Result.success(templates);
    }

    /**
     * 获取指定模板详情
     * GET /api/templates/{id}
     */
    @GetMapping("/{id}")
    public Result<Template> getTemplate(@PathVariable("id") String id) {
        Template template = templateService.getTemplate(id);
        return Result.success(template);
    }

}
