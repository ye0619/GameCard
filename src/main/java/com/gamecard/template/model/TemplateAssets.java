package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Template asset definitions.
 * <p>
 * Each field is a map from identifier to resource path.
 * The Renderer resolves theme mapping icons/backgrounds
 * through these asset tables.
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateAssets {

    /** Background images (backgroundId -> path) */
    private Map<String, String> backgrounds;

    /** Icons (iconId -> path) */
    private Map<String, String> icons;

    /** Frames/borders (frameId -> path) */
    private Map<String, String> frames;

    /** Font files (fontFamily -> path) */
    private Map<String, String> fonts;

}
