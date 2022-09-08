package de.borstelmann.doorbell.server.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cronn.assertions.validationfile.junit5.JUnit5ValidationFileAssertions;
import de.cronn.assertions.validationfile.normalization.ValidationNormalizer;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

public abstract class BaseTest implements JUnit5ValidationFileAssertions {

    protected void assertWithFormattedJsonFile(String responseBody) {
        assertWithJsonFile(formatJsonString(responseBody));
    }

    public void assertWithFormattedJsonFileWithSuffix(String responseBody, ValidationNormalizer validationNormalizer, String suffix) {
        assertWithJsonFileWithSuffix(formatJsonString(responseBody), validationNormalizer, suffix);
    }

    protected void assertWithFormattedJsonFile(Object object) {
        assertWithJsonFile(formatJson(object));
    }

    protected void assertWithFormattedJsonFile(MvcResult result) throws UnsupportedEncodingException {
        this.assertWithFormattedJsonFile(result.getResponse().getContentAsString());
    }

    protected String formatJson(Object object) {
        try {
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String formatJsonString(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }

        try {
            Object o = getObjectMapper().readValue(json, Object.class);
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
