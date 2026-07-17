package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Core template model for game cards.
 * <p>
 * Each game style (Pokemon, Genshin, Minecraft, etc.) has its own template.
 * A template defines card layout, colors, field mappings, and style info.
 * <p>
 * Design principles:
 * <ul>
 *   <li>Decoupled: components read template config, no hardcoded business logic</li>
 *   <li>Attribute-driven: themeMapping maps field values to visual themes</li>
 *   <li>Extensible: new templates = new template.json + assets, no Renderer changes</li>
 * </ul>
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    /** Template ID (e.g. "pokemon", "genshin") */
    private String id;

    /** Template display name */
    private String name;

    /** Template description */
    private String description;

    /** Template author */
    private String author;

    /** Template version */
    private String version;

    /** Preview image path */
    private String previewImage;

    /** Template tags for filtering */
    private List<String> tags;

    /** Card dimensions */
    private TemplateSize size;

    /** Field definitions */
    private List<TemplateField> fields;

    /**
     * Theme mapping table (field value -> theme config)
     * <p>
     * Core of attribute-driven theming:
     * the frontend resolves themes by looking up card data values
     * in this map -- no hardcoded if/else chains.
     */
    private Map<String, ThemeMappingItem> themeMapping;

    /** Field name used as the theme lookup key.
     * e.g. "nature" means cardData["nature"] drives the theme.
     */
    private String themeField;

    /**
     * List of field keys that represent numerical stats for radar/bar display.
     * e.g. ["wisdom", "constitution", "perception"]
     */
    private List<String> statFields;

    /** Template asset definitions */
    private TemplateAssets assets;

    /** Template metadata */
    private TemplateMetadata metadata;

    /** Preset skills for quick selection (loaded from template.json) */
    private List<PresetSkill> presetSkills;

    /** Preset character introductions for quick selection (loaded from template.json) */
    private List<PresetIntroduction> presetIntroductions;

}
