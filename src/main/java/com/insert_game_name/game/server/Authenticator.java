package com.insert_game_name.game.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Stores user credentials and authenticates users against the local user database. */
public final class Authenticator {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final Path database;
    private final Path tempDatabase;
    private final ObjectMapper MAPPER;
    private ArrayNode playerData;
    private final StringKeyGenerator TOKEN_GEN = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 32);

    /**
     * Creates an authenticator backed by {@code users.json} in the supplied directory.
     *
     * @param dir directory containing the user database
     * @param mapper mapper used to read and write the JSON database
     */
    public Authenticator(Path dir, ObjectMapper mapper) {
        database = dir.resolve("users.json");
        tempDatabase = dir.resolve("users.json.tmp");
        MAPPER = mapper;
        try {
            if (Files.notExists(database)) {
                MAPPER.writeValue(database.toFile(), MAPPER.createArrayNode());
            }

            if (Files.notExists(tempDatabase)) {
                MAPPER.writeValue(tempDatabase.toFile(), MAPPER.createArrayNode());
            }

            fetchStored();
        } catch (IOException e) {
            playerData = MAPPER.createArrayNode();
        }
    }

    private void fetchStored() throws IOException {
        JsonNode stored = MAPPER.readTree(database.toFile());
        playerData = stored != null && stored.isArray() ? (ArrayNode) stored : MAPPER.createArrayNode();
    }

    /**
     * Registers a user and persists the password as a BCrypt hash.
     *
     * @param user username to register
     * @param password plaintext password to hash
     * @return {@code true} when the user was stored, or {@code false} when the
     *         username already exists or persistence fails
     */
    public boolean register(String user, String password) {
        if (playerData != null && playerData.isArray()) {
            for (JsonNode player : playerData) {
                if (user.equals(player.path("username").asText())) {
                    return false;
                }
            }

            ObjectNode newPlayer = MAPPER.createObjectNode();
            newPlayer.put("username", user);
            newPlayer.put("password", encoder.encode(password));
            playerData.add(newPlayer);
            
            try {
                MAPPER.writeValue(tempDatabase.toFile(), playerData);

                try {
                    Files.move(tempDatabase, database, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                    Files.move(tempDatabase, database, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifies credentials and creates a session token on success.
     *
     * @param user username to authenticate
     * @param password plaintext password to verify
     * @return a newly generated token when authentication succeeds; otherwise
     *         {@code null}
     */
    public String login(String user, String password) {
        if (playerData != null && playerData.isArray()) {
            for (JsonNode player : playerData) {
                if (user.equals(player.path("username").asText())) {
                    if (encoder.matches(password, player.path("password").asText())) return TOKEN_GEN.generateKey();
                }
            }
        }
        
        return null;
    }
}