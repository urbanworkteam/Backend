-- =====================================================================
--  야간 push 배치 부하 실험 — 더미 유저 알림 활성화
-- =====================================================================
--  ⚠️  경고: 이 스크립트는 운영(prod) 데이터를 직접 변경한다.
--      실행 전 반드시 prod RDS 스냅샷을 생성하라. 스냅샷 없이 실행 금지.
--
--  대상: kakao_id 가 'dummy_%' 인 더미 유저.
--  목적: notification_settings.push_enabled / diary_reminder_enabled 를 켜서
--        UserReminderQueries.findUsersWithoutDiaryOn 의 타깃에 더미 유저가
--        잡히도록 한다(실측 부하 ≈50만 명 확보).
--  주의: 이 스크립트는 db/migration 이 아닌 loadtest/ 에 둔다(Flyway 실행 대상 아님).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) 더미 유저 알림 설정 ON
-- ---------------------------------------------------------------------
UPDATE notification_settings
   SET push_enabled = true,
       diary_reminder_enabled = true
 WHERE user_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dummy\_%');

-- ---------------------------------------------------------------------
-- 2) 검증 쿼리
-- ---------------------------------------------------------------------
-- 2-a) 아직 온보딩되지 않은(onboarded_at IS NULL) 더미 유저 수 확인.
--      (온보딩 안 된 유저는 타깃 쿼리에서 제외되므로, 이 값이 크면 타깃이 줄어든다.)
SELECT count(*) AS dummy_not_onboarded
  FROM users
 WHERE kakao_id LIKE 'dummy\_%'
   AND onboarded_at IS NULL;

-- 2-b) UserReminderQueries.findUsersWithoutDiaryOn 과 동일한 조건으로
--      최종 타깃 수가 ≈50만 나오는지 확인.
--      :today 자리에 실험 당일 날짜(예: '2026-06-11')를 바인딩한다.
SELECT count(*) AS final_target_count
  FROM users u
  LEFT JOIN notification_settings n ON n.user_id = u.id
 WHERE u.deleted_at IS NULL
   AND u.onboarded_at IS NOT NULL
   AND coalesce(n.diary_reminder_enabled, true) = true
   AND coalesce(n.push_enabled, true) = true
   AND NOT EXISTS (
     SELECT 1 FROM farm_diaries d
      WHERE d.user_id = u.id
        AND d.diary_date = :today
        AND d.deleted_at IS NULL
   );

-- ---------------------------------------------------------------------
-- 3) 롤백 — 실험 종료 후 더미 유저 알림 설정 OFF 로 되돌린다.
-- ---------------------------------------------------------------------
-- UPDATE notification_settings
--    SET push_enabled = false,
--        diary_reminder_enabled = false
--  WHERE user_id IN (SELECT id FROM users WHERE kakao_id LIKE 'dummy\_%');
