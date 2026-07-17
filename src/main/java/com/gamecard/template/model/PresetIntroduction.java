package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preset character introduction for quick selection.
 * <p>
 * Pre-written character descriptions/biographies that users can
 * select and apply to their card, providing inspiration or
 * filling in common archetypes quickly.
 * Loaded as part of the template definition in template.json.
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetIntroduction {

    /** Display title of this preset (e.g. "独行侠客", "远古守护者") */
    private String title;

    /** The actual description/backstory content */
    private String content;

    /** Optional associated nature/theme hint (e.g. "暴躁", "温柔") */
    private String natureHint;
}
