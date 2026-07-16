package com.gamecard.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Template field definition.
 * <p>
 * Describes each input field's metadata for dynamic form rendering.
 *
 * @author GameCard Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateField {

    /** Unique field key (e.g. "name", "type") */
    private String key;

    /** Display label */
    private String label;

    /**
     * Field type
     * <ul>
     *   <li>TEXT - single-line text</li>
     *   <li>TEXTAREA - multi-line text</li>
     *   <li>IMAGE - image upload</li>
     *   <li>NUMBER - numeric input</li>
     *   <li>SELECT - dropdown select</li>
     *   <li>ENUM - tag group selector</li>
     *   <li>ARRAY - dynamic list (e.g. skills)</li>
     *   <li>COLOR - color picker</li>
     * </ul>
     */
    private String type;

    /** Whether the field is required */
    private boolean required;

    /** Default value */
    private String defaultValue;

    /** Input placeholder text */
    private String placeholder;

    /** Options for SELECT/ENUM types (JSON array string) */
    private String options;

    /** Sub-field definitions for ARRAY type */
    private List<TemplateField> fields;

}
