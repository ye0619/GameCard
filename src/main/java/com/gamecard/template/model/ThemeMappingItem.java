package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Theme mapping entry.
 * <p>
 * Maps a field value (e.g. "fire") to a visual theme.
 * The template's themeMapping drives all styling -- never hardcode
 * conditions like if(type==="fire") in components.
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeMappingItem {

    /** Background identifier (key into assets.backgrounds) */
    private String background;

    /** Primary color hex (e.g. "#EF4444") */
    private String color;

    /** Icon identifier (key into assets.icons) */
    private String icon;

    /** Secondary/accent color */
    private String secondaryColor;

    /** Extended style variables (passed through as CSS custom properties) */
    private Map<String, String> styles;

}
