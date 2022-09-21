package de.borstelmann.doorbell.server.systemtest;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.homegraph.v1.HomeGraphService;
import okhttp3.mockwebserver.*;
import okio.Buffer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

@TestConfiguration
class GoogleHomeMockConfig {

    @Bean
    public MockWebServer mockWebServer() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new MockGoogleHomeGraphDispatcher());
        return mockWebServer;
    }

    @Bean
    @Primary
    public HomeGraphService mockHomeGraphService(MockWebServer mockWebServer) throws GeneralSecurityException, IOException {
        HomeGraphService.Builder builder = new HomeGraphService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new MockGoogleCredential(new MockGoogleCredential.Builder())
        );

        return builder
                .setRootUrl(mockWebServer.url("/").toString())
                .setApplicationName("HomeGraphExample/1.0")
                .build();
    }

}
