package kr.farmily.api.auth.scheduler;

import kr.farmily.api.auth.domain.DeletionRequest;
import kr.farmily.api.auth.repository.DeletionRequestRepository;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurgeDeletedUsersScheduler {

    private final DeletionRequestRepository deletionRepository;
    private final UserRepository userRepository;

    /** 매일 03:00 (Asia/Seoul). 30일 경과 사용자 hard delete. CASCADE 로 관련 데이터 정리. */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void purge() {
        List<DeletionRequest> due = deletionRepository.findDueForPurge(OffsetDateTime.now());
        if (due.isEmpty()) return;
        log.info("Purging {} expired deletion requests", due.size());
        for (DeletionRequest req : due) {
            userRepository.findById(req.getUserId()).ifPresent(userRepository::delete);
            deletionRepository.delete(req);
        }
    }
}
