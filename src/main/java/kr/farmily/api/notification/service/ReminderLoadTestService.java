package kr.farmily.api.notification.service;

import kr.farmily.api.notification.scheduler.UserReminderQueries;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 야간 push 배치 부하 실험용 서비스. farmily.experiment.enabled=true 일 때만
 * AdminReminderController 를 통해 호출된다. 기존 DiaryReminderScheduler.remind() 는
 * 건드리지 않으며, notification_logs 는 읽거나 쓰지 않아 자유로운 재실행이 가능하다.
 * PushService.send 를 그대로 재사용하므로(@Transactional(readOnly=true) 별도 빈)
 * parallel 모드에서 각 호출이 HikariCP 커넥션을 점유해 풀 경합을 재현한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderLoadTestService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String TITLE = "오늘 영농일지를 작성하지 않으셨어요!";
    private static final String BODY = "영농일지를 기반으로 AI 콘텐츠를 작성해보세요.";
    private static final String LINK = "farmily://diary/write";

    private final UserReminderQueries reminderQueries;
    private final PushService pushService;

    public RunResult run(String mode, int threads, int limit, long target) {
        LocalDate today = LocalDate.now(KST);
        List<Long> targets = reminderQueries.findUsersWithoutDiaryOn(today);
        List<Long> sample = (limit > 0 && limit < targets.size())
                ? targets.subList(0, limit)
                : targets;

        long startNanos = System.nanoTime();
        if ("parallel".equals(mode)) {
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            try {
                for (Long uid : sample) {
                    pool.submit(() -> pushService.send(uid, TITLE, BODY, LINK));
                }
            } finally {
                pool.shutdown();
                try {
                    pool.awaitTermination(2, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            for (Long uid : sample) {
                pushService.send(uid, TITLE, BODY, LINK);
            }
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

        int processed = sample.size();
        double msPerItem = processed == 0 ? 0 : (double) elapsedMs / processed;
        double ratePerSec = elapsedMs == 0 ? 0 : processed * 1000.0 / elapsedMs;
        double extrapolatedSeconds = msPerItem * target / 1000.0;
        double extrapolatedHours = extrapolatedSeconds / 3600.0;

        RunResult result = new RunResult(
                "parallel".equals(mode) ? "parallel" : "single",
                "parallel".equals(mode) ? threads : 1,
                processed, elapsedMs, msPerItem, ratePerSec,
                target, extrapolatedSeconds, extrapolatedHours);

        log.info("ReminderLoadTest result: mode={}, threads={}, processed={}, elapsedMs={}, "
                        + "msPerItem={}, ratePerSec={}, target={}, extrapolatedHours={}",
                result.mode(), result.threads(), result.processed(), result.elapsedMs(),
                result.msPerItem(), result.ratePerSec(), result.target(), result.extrapolatedHours());

        return result;
    }

    public record RunResult(String mode, int threads, int processed, long elapsedMs,
                            double msPerItem, double ratePerSec, long target,
                            double extrapolatedSeconds, double extrapolatedHours) {
    }
}
