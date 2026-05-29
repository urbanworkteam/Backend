package kr.farmily.api.notification.scheduler;

import kr.farmily.api.notification.domain.NotificationLog;
import kr.farmily.api.notification.repository.NotificationLogRepository;
import kr.farmily.api.notification.service.PushService;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private static final String KIND = "DIARY_REMINDER";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserReminderQueries reminderQueries;
    private final PushService pushService;
    private final NotificationLogRepository logRepo;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    @Transactional
    public void remind() {
        LocalDate today = LocalDate.now(KST);
        List<Long> targets = reminderQueries.findUsersWithoutDiaryOn(today);
        log.info("Diary reminder targets: {}", targets.size());
        for (Long userId : targets) {
            if (logRepo.existsByUserIdAndKindAndSentDate(userId, KIND, today)) continue;
            pushService.send(userId,
                    "오늘 영농일지를 작성하지 않으셨어요!",
                    "영농일지를 기반으로 AI 콘텐츠를 작성해보세요.",
                    "farmily://diary/write");
            logRepo.save(NotificationLog.of(userId, KIND, today));
        }
    }
}
