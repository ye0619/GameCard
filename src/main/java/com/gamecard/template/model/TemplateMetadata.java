package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Template metadata (versioning, timestamps as ISO strings).
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMetadata {

    /** Creation time (ISO-8601 string, e.g. "2026-07-01T00:00:00") */
    private String createTime;

    /** Last update time (ISO-8601 string) */
    private String updateTime;

    /** Template internal version */
    private String templateVersion;

    /** Minimum compatible GameCard version */
    private String minAppVersion;

}
