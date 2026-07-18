package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Template image processing configuration.
 * <p>
 * Defines AI image generation prompts and processing options
 * for each template. Loaded from the template's imageConfig field
 * in template.json.
 * </p>
 * <p>
 * Design principle: each template carries its own imageConfig,
 * so adding a new template never requires modifying image processing code.
 * </p>
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateImageConfig {

    /** AI prompt for image generation (sent to the user-configured AI API) */
    private String imagePrompt;

    /** Processing options */
    private ProcessingOptions processing;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingOptions {
        /** Whether to remove background before sending to AI API */
        private boolean removeBackground;

        /** Preferred aspect ratio, e.g. "1:1", "16:9" */
        private String preferredRatio;
    }
}
