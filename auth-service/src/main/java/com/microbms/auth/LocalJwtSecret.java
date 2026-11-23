package com.microbms.auth;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/** Loads an explicitly configured key or creates/reuses a per-user local development key. */
@Component
public class LocalJwtSecret {
    private final Environment environment;

    public LocalJwtSecret(Environment environment) {
        this.environment = environment;
    }

    public String get() {
        String configured = environment.getProperty("JWT_SECRET");
        if (configured != null && !configured.isBlank() && !configured.startsWith("CHANGE_ME")) {
            validate(configured);
            return configured;
        }

        Path secretFile = localSecretFile();
        try {
            Files.createDirectories(secretFile.getParent());
            for (int attempt = 0; attempt < 40; attempt++) {
                if (!Files.exists(secretFile)) {
                    byte[] bytes = new byte[32];
                    new java.security.SecureRandom().nextBytes(bytes);
                    try {
                        Files.writeString(secretFile, Base64.getEncoder().encodeToString(bytes), StandardCharsets.US_ASCII,
                                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    } catch (FileAlreadyExistsException ignored) {
                        // Another service created the shared key at the same time.
                    }
                }
                String secret = Files.readString(secretFile, StandardCharsets.US_ASCII).trim();
                try {
                    validate(secret);
                    return secret;
                } catch (IllegalStateException incompleteOrInvalid) {
                    if (attempt == 39) throw incompleteOrInvalid;
                    Thread.sleep(50);
                }
            }
            throw new IllegalStateException("Could not load the local JWT key at " + secretFile);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load or create the local JWT key at " + secretFile, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading the local JWT key at " + secretFile, e);
        }
    }

    private static Path localSecretFile() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = localAppData == null || localAppData.isBlank()
                ? Path.of(System.getProperty("user.home"), ".local", "share")
                : Path.of(localAppData);
        return base.resolve("MicroBms").resolve("jwt-secret.txt");
    }

    private static void validate(String secret) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException notBase64) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be Base64-encoded and contain at least 32 random bytes");
        }
    }
}
