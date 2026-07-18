package com.gamecard.controller;

/**
 * AI 图像处理请求 DTO
 *
 * <p>
 * Jackson 3.x 通过 getter 方法名自动推断 JSON 字段名，
 * 例如 getImageUrl() → "imageUrl"，无需 @JsonProperty 注解。
 * </p>
 *
 * @author GameCard Team
 */
public class AiProcessRequest {

    private String imageUrl;
    private String templateId;
    private String operation;
    /** 用户自定义提示词（可选，为空则使用模板默认） */
    private String prompt;
    private AiConfig aiConfig;

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public AiConfig getAiConfig() { return aiConfig; }
    public void setAiConfig(AiConfig aiConfig) { this.aiConfig = aiConfig; }

    public static class AiConfig {
        private String endpoint;
        private String apiKey;
        private String model;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
