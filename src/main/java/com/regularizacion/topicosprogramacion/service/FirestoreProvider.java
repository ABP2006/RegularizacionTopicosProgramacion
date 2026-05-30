package com.regularizacion.topicosprogramacion.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FirestoreProvider {
    private static Firestore firestore;

    private FirestoreProvider() {
    }

    public static synchronized Firestore getDb() {
        if (firestore == null) {
            initialize();
        }
        return firestore;
    }

    private static void initialize() {
        try (InputStream credentials = openCredentials()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            firestore = FirestoreClient.getFirestore();
        } catch (Exception e) {
            throw new IllegalStateException("""
                    No se pudo conectar con Firebase.
                    Coloca serviceAccountKey.json en src/main/resources/firebase/
                    o define FIREBASE_CREDENTIALS con la ruta completa del archivo JSON.
                    Detalle: %s
                    """.formatted(e.getMessage()), e);
        }
    }

    private static InputStream openCredentials() throws Exception {
        String envPath = System.getenv("FIREBASE_CREDENTIALS");
        if (envPath != null && !envPath.isBlank() && Files.exists(Path.of(envPath))) {
            return new FileInputStream(envPath);
        }

        String propertyPath = System.getProperty("firebase.credentials");
        if (propertyPath != null && !propertyPath.isBlank() && Files.exists(Path.of(propertyPath))) {
            return new FileInputStream(propertyPath);
        }

        File projectFile = Path.of("src", "main", "resources", "firebase", "serviceAccountKey.json").toFile();
        if (projectFile.exists()) {
            return new FileInputStream(projectFile);
        }

        InputStream resource = FirestoreProvider.class.getResourceAsStream("/firebase/serviceAccountKey.json");
        if (resource != null) {
            return resource;
        }

        throw new IllegalArgumentException("No se encontro serviceAccountKey.json");
    }
}

