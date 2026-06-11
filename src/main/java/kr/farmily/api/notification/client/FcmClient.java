package kr.farmily.api.notification.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    @Value("${farmily.fcm.service-account-json:}")
    private String serviceAccountJson;

    @Value("${farmily.fcm.dry-run:false}")
    private boolean dryRun;

    private FirebaseMessaging messaging;

    @PostConstruct
    void init() {
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            log.warn("FCM service account 미설정. 푸시 발송 비활성화");
            return;
        }
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options);
            messaging = FirebaseMessaging.getInstance();
        } catch (Exception e) {
            log.error("Firebase 초기화 실패", e);
        }
    }

    public void sendMulticast(List<String> tokens, String title, String body, String deepLink) {
        if (messaging == null || tokens == null || tokens.isEmpty()) return;
        try {
            MulticastMessage msg = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(deepLink != null ? Map.of("deepLink", deepLink) : Map.of())
                    .build();
            var res = messaging.sendEachForMulticast(msg, dryRun);
            log.info("FCM 발송: success={}, fail={}", res.getSuccessCount(), res.getFailureCount());
        } catch (Exception e) {
            log.warn("FCM 발송 실패: {}", e.getMessage());
        }
    }
}
