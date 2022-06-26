package de.borstelmann.doorbell.server.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleSmartHomeConfiguration {

    public static final String KEY_PATH = "classpath:smart-doorbell.json";

    @Bean
    @ConditionalOnResource(resources = KEY_PATH)
    public HomeGraphService homeGraphService(@Value(KEY_PATH) Resource resourceFile) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(resourceFile.getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/homegraph"));

        HomeGraphService.Builder builder = new HomeGraphService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        );

        return builder
                .setApplicationName("HomeGraphExample/1.0")
                .build();
    }

}
