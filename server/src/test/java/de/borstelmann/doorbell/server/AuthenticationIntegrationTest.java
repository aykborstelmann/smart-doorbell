package de.borstelmann.doorbell.server;

import de.borstelmann.doorbell.server.test.RequestUtils;
import de.borstelmann.doorbell.server.test.authentication.OAuthIntegrationTest;
import org.assertj.core.api.Assertions;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.CoreMatchers.not;

public class AuthenticationIntegrationTest extends OAuthIntegrationTest {

    private static final String WRONG_AUDIENCE = "WRONG_AUDIENCE";

    @Test
    void testAuthenticate_wrongAudience() throws Exception {
        String token = getJwsBuilder()
                .audience(WRONG_AUDIENCE)
                .subject(SAMPLE_OAUTH_ID)
                .build()
                .getCompactSerialization();

        mockMvc.perform(RequestUtils.createGetAllUsersRequest(token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(result -> {
                    String header = result.getResponse().getHeader("WWW-Authenticate");
                    Assertions.assertThat(header)
                            .contains("invalid_token")
                            .contains("The required audience is missing");
                });
    }

    @Test
    void testAuthenticate_wrongKey() throws Exception {
        RsaJsonWebKey differentRsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        differentRsaJsonWebKey.setKeyId("k2");
        differentRsaJsonWebKey.setAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
        differentRsaJsonWebKey.setUse("sig");

        String token = getJwsBuilder()
                .rsaJsonWebKey(differentRsaJsonWebKey)
                .subject(SAMPLE_OAUTH_ID)
                .build()
                .getCompactSerialization();

        mockMvc.perform(RequestUtils.createGetAllUsersRequest(token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(result -> {
                    String header = result.getResponse().getHeader("WWW-Authenticate");
                    Assertions.assertThat(header)
                            .contains("invalid_token")
                            .contains("Signed JWT rejected");
                });
    }

    @Test
    void testAuthenticate_createsUser() throws Exception {
        String oAuthId = "OAuthId";
        String token = obtainToken(oAuthId);

        mockMvc.perform(RequestUtils.createGetAllUsersRequest(token))
                .andExpect(MockMvcResultMatchers.status().is(not(HttpStatus.UNAUTHORIZED)));

        Assertions.assertThat(userRepository.findByOAuthId(oAuthId))
                .isPresent();
    }
}
