package de.borstelmann.doorbell.server.test.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import de.borstelmann.doorbell.server.test.SpringIntegrationTest;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureWireMock(port = 0)
@ActiveProfiles({"wiremock","default"})
@Import(OAuthIntegrationTest.TestWireMockConfiguration.class)
public class OAuthIntegrationTest extends SpringIntegrationTest {

    private static final String JWKS_PATH = "/.well-known/jwks.json";
    private static final String CONFIGURATION_PATH = "/.well-known/openid-configuration";
    private static final String JWKS_URI = "jwks_uri";
    private static final String ISSUER = "issuer";

    private final JWSBuilder jwsBuilder = new JWSBuilder();

    @Value("${wiremock.server.baseUrl}")
    private String wireMockServerBaseUrl;

    @Value("${auth0.audience}")
    private String audience;

    @Autowired
    private RsaJsonWebKey rsaJsonWebKey;

    @BeforeEach
    void setUpWireMock() throws JsonProcessingException {
        jwsBuilder
                .rsaJsonWebKey(rsaJsonWebKey)
                .issuer(wireMockServerBaseUrl)
                .audience(audience);

        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo(JWKS_PATH))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                        .withBody(new JsonWebKeySet(rsaJsonWebKey).toJson())
                        )
        );

        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo(CONFIGURATION_PATH))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                        .withBody(createConfigurationPayload())
                        )
        );
    }

    private String createConfigurationPayload() throws JsonProcessingException {
        Map<String, String> configuration = new HashMap<>();
        configuration.put(JWKS_URI, wireMockServerBaseUrl + JWKS_PATH);
        configuration.put(ISSUER, wireMockServerBaseUrl);
        return objectMapper.writeValueAsString(configuration);
    }

    protected String obtainToken() {
        return obtainToken(SAMPLE_OAUTH_ID);
    }

    protected String obtainToken(String subject) {
        try {
            return jwsBuilder.subject(subject).build().getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    protected JWSBuilder getJwsBuilder() {
        return jwsBuilder;
    }

    @TestConfiguration
    static class TestWireMockConfiguration {
        @Bean
        public RsaJsonWebKey rsaJsonWebKey() throws JoseException {
            RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
            rsaJsonWebKey.setKeyId("k1");
            rsaJsonWebKey.setAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
            rsaJsonWebKey.setUse("sig");
            return rsaJsonWebKey;
        }
    }
}
