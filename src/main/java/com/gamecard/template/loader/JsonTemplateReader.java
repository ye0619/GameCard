package com.gamecard.template.loader;

import com.gamecard.template.model.Template;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads Template objects from JSON using Jackson 3.x (tools.jackson).
 * <p>
 * Spring Boot 4.x provides tools.jackson.core:jackson-databind transitively.
 * Date/time fields in template.json are read as ISO strings (String type in model).
 */
public class JsonTemplateReader {

    private final ObjectMapper objectMapper;

    public JsonTemplateReader() {
        this.objectMapper = new ObjectMapper()
                .rebuild()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    /**
     * Parse a Template from a JSON input stream.
     */
    public Template read(InputStream is) throws IOException {
        return objectMapper.readValue(is, Template.class);
    }
}
