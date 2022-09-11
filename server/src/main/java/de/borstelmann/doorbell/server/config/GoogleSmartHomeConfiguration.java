package de.borstelmann.doorbell.server.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.homegraph.v1.HomeGraphService;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleSmartHomeConfiguration {

    public static final List<String> ACCESSIBLE_GOOGLE_APIS = List.of("https://www.googleapis.com/auth/homegraph");
    public static final String KEY_PATH = "classpath:smart-doorbell.json";
    public static final String APPLICATION_NAME = "SmartDoorbell/1.0";

    @Bean
    public HomeGraphService homeGraphService(@Value(KEY_PATH) Resource resourceFile) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = getGoogleCredentials(resourceFile);

        HomeGraphService.Builder builder = new HomeGraphService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        );

        return builder
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static GoogleCredentials getGoogleCredentials(Resource resourceFile) throws IOException {
        if (resourceFile.exists()) {
            return GoogleCredentials
                    .fromStream(resourceFile.getInputStream())
                    .createScoped(ACCESSIBLE_GOOGLE_APIS);
        }

        return GoogleCredentials
                .newBuilder()
                .build();

    }

}
