package de.borstelmann.doorbell.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class GoogleCredentialsConfig {

    public static final String KEY_PATH = "classpath:smart-doorbell.json";

    @Bean
    @ConditionalOnResource(resources = KEY_PATH)
    public GoogleCredentials googleCredentials(@Value(KEY_PATH) Resource resourceFile) throws IOException {
        return GoogleCredentials.fromStream(resourceFile.getInputStream());
    }

}
