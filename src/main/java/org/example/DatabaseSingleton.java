package org.example;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatabaseSingleton {
    private static MongoDatabase _instance = null;

    private static void init() {
        Properties properties = new Properties();

        try (var input = Files.newInputStream(Path.of("application.properties"))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        String connectionString = properties.getProperty("connection_string");
        String databaseName = properties.getProperty("database_name");

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        _instance = MongoClients.create(connectionString).getDatabase(databaseName).withCodecRegistry(codecRegistry);
    }

    public static MongoDatabase getInstance() {
        if (_instance == null) {
            init();
        }
        return _instance;
    }
}
