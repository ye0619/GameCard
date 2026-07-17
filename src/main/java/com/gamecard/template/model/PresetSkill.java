package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preset skill definition for quick selection.
 * <p>
 * Skills that users can pick from when building a card,
 * rather than typing them out manually each time.
 * Loaded as part of the template definition in template.json.
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetSkill {

    /** Skill name (e.g. "火焰吐息") */
    private String name;

    /** Skill category for grouping (e.g. "攻击", "防御", "辅助") */
    private String category;

    /** Short description of what the skill does */
    private String description;

    /** Power level hint (1-100) */
    private Integer power;
}
