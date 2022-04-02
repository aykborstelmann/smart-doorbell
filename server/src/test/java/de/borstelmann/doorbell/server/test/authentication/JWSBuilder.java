package de.borstelmann.doorbell.server.test.authentication;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.UUID;

public class JWSBuilder {
    private RsaJsonWebKey rsaJsonWebKey;
    private String claimsIssuer;
    private String claimsSubject;
    private String audience;

    public JWSBuilder rsaJsonWebKey(RsaJsonWebKey rsaJsonWebKey) {
        this.rsaJsonWebKey = rsaJsonWebKey;
        return this;
    }

    public JWSBuilder issuer(String claimsIssuer) {
        this.claimsIssuer = claimsIssuer;
        return this;
    }

    public JWSBuilder subject(String claimsSubject) {
        this.claimsSubject = claimsSubject;
        return this;
    }

    public JWSBuilder audience(String audience) {
        this.audience = audience;
        return this;
    }

    public JsonWebSignature build() {
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setJwtId(UUID.randomUUID().toString());
        jwtClaims.setIssuer(claimsIssuer);
        jwtClaims.setSubject(claimsSubject);
        jwtClaims.setAudience(audience);
        jwtClaims.setExpirationTimeMinutesInTheFuture(10F);
        jwtClaims.setIssuedAtToNow();
        jwtClaims.setClaim("azp", "example-client-id");

        JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(jwtClaims.toJson());
        if (rsaJsonWebKey != null) {
            jsonWebSignature.setKey(rsaJsonWebKey.getPrivateKey());
            jsonWebSignature.setAlgorithmHeaderValue(rsaJsonWebKey.getAlgorithm());
            jsonWebSignature.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
            jsonWebSignature.setHeader("typ", "JWT");
        }
        return jsonWebSignature;
    }
}
