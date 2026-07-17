package com.gamecard.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 自动抠图服务配置属性
 *
 * @author GameCard Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "bg-remover")
public class BackgroundRemovalProperties {

    /** 是否启用抠图功能 */
    private boolean enabled = true;

    /** Python 抠图服务地址 */
    private String serviceUrl = "http://localhost:8001";

    /** 请求超时时间（毫秒） */
    private long timeoutMs = 120000;

}
