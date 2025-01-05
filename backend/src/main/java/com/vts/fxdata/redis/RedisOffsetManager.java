package com.vts.fxdata.redis;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisOffsetManager {
    private static final Logger log = LoggerFactory.getLogger(RedisOffsetManager.class);
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    @Value("${fxdata.redis.offsets}")
    private String filePath;

    @PostConstruct
    public void init() {
        load();
    }

    public void setOffset(String key, String value) {
        map.put(key, value);
        save();
    }

    public String getOffset(String key) {
        return map.get(key);
    }

    private void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            log.error("Error loading properties file: {}", e.getMessage());
        }
    }

    private void save() {
        try {
            Path path = Paths.get(filePath).toAbsolutePath();
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (var entry : map.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving properties file: " + e.getMessage());
        }
    }
}