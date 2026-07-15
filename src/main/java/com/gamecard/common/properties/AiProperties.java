package com.gamecard.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置属性（预留）
 *
 * @author GameCard Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用 AI 功能 */
    private boolean enabled = false;

    /** AI 服务提供商 */
    private String provider = "openai-compatible";

    /** API 基础地址 */
    private String baseUrl;

    /** API 密钥 */
    private String apiKey;

    /** 模型名称 */
    private String model;

}
