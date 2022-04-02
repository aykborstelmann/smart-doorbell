package de.borstelmann.doorbell.server.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cronn.assertions.validationfile.junit5.JUnit5ValidationFileAssertions;
import de.cronn.assertions.validationfile.normalization.ValidationNormalizer;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

public abstract class BaseTest implements JUnit5ValidationFileAssertions {

    public void assertWithFormattedJsonFile(String responseBody) {
        assertWithJsonFile(formatJson(responseBody));
    }

    public void assertWithFormattedJsonFile(String responseBody, ValidationNormalizer validationNormalizer) {
        assertWithJsonFile(formatJson(responseBody), validationNormalizer);
    }

    public String formatJson(String json) {
        if (json.isBlank()) {
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

    protected void assertWithFormattedJsonFile(MvcResult result) throws UnsupportedEncodingException {
        this.assertWithFormattedJsonFile(result.getResponse().getContentAsString());
    }

    protected void assertWithFormattedJsonFile(MvcResult result, ValidationNormalizer validationNormalizer) throws UnsupportedEncodingException {
        this.assertWithFormattedJsonFile(result.getResponse().getContentAsString(), validationNormalizer);
    }
}
