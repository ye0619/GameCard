package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板元数据
 * <p>
 * 记录模板的版本、创建时间等元信息。
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMetadata {

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;

    /** 模板内部版本号 */
    private String templateVersion;

    /** 兼容的最低 GameCard 版本 */
    private String minAppVersion;

}
