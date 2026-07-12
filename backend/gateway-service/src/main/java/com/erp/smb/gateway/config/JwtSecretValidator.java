package com.erp.smb.gateway.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Fails fast at startup if APP_JWT_SECRET is missing, blank, too short,
 * or still set to the well-known placeholder value.
 *
 * This guard exists in the gateway because the gateway is the first service
 * that processes JWT tokens from external clients. Auth-service has its own
 * equivalent guard via the same mechanism.
 */
@Component
public class JwtSecretValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtSecretValidator.class);
    private static final int MIN_SECRET_LENGTH = 32;
    private static final String KNOWN_WEAK_SECRET = "CHANGE_ME_generate_with_openssl_rand_hex_32";

    @Value("${app.jwt.secret}")
    private String secret;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is not set. Set it to a cryptographically random string " +
                    "of at least " + MIN_SECRET_LENGTH + " characters. " +
                    "Generate one with: openssl rand -hex 32");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is too short (" + secret.length() + " chars). " +
                    "Minimum length is " + MIN_SECRET_LENGTH + " characters.");
        }
        if (KNOWN_WEAK_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET is set to the placeholder value. " +
                    "Replace it with a real secret before starting the application.");
        }
        log.info("JWT secret validated: length={} chars", secret.length());
    }
}
