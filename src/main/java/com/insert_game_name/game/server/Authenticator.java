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

public final class Authenticator {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final Path database;
    private final Path tempDatabase;
    private final ObjectMapper MAPPER;
    private ArrayNode playerData;
    private final StringKeyGenerator TOKEN_GEN = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 32);

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