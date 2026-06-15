package com.uniroad.backend.domain.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseConfig {

    @Value("${firebase.service-account-base64}")
    private String serviceAccountBase64;

    @Value("${firebase.project-id:}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        if (!StringUtils.hasText(serviceAccountBase64)) {
            throw new IllegalStateException("firebase.service-account-base64 must be set when firebase.enabled=true");
        }

        byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));

        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                .setCredentials(credentials);

        if (StringUtils.hasText(projectId)) {
            optionsBuilder.setProjectId(projectId);
        }

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(optionsBuilder.build());
        }

        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
