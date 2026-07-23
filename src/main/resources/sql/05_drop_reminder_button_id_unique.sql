-- 05_drop_reminder_button_id_unique.sql
--
-- 배경:
-- reminder.button_id에 UNIQUE 인덱스가 걸려 있었으나, 알림 삭제(DELETE) 기능이
-- 소프트 삭제(deleted_at) 방식으로 추가되면서 이 제약과 충돌이 발생함.
-- (삭제된 리마인더가 있는 버튼에 새 리마인더를 다시 만들려고 하면
--  DB의 UNIQUE 제약 때문에 Duplicate entry 에러 발생)
--
-- 애플리케이션 레벨(ReminderRepository.findByButtonIdAndDeletedAtIsNull)에서
-- "삭제되지 않은 리마인더는 버튼당 하나"를 보장하고 있으므로,
-- DB 레벨의 강제 UNIQUE 제약은 제거한다.

ALTER TABLE reminder DROP INDEX UK4jkymns5282ev9v8amtsx7fsq;
