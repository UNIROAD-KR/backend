-- 자바 enum 상수를 늘려도 INSERT가 죽지 않도록 ENUM 컬럼을 varchar로 바꾼다.
--
-- Hibernate는 MySQL에서 @Enumerated(EnumType.STRING) 필드를 네이티브 ENUM 컬럼으로 만드는데,
-- ddl-auto=update는 이미 만들어진 ENUM의 허용 값을 넓혀주지 않는다. 그래서 자바 쪽에 상수를
-- 추가해도 DB는 예전 목록을 그대로 들고 있고, 그 값을 넣는 INSERT가
-- "Data truncated for column"으로 실패한다.
-- ChatReferenceType.COMPANION과 NotificationType.COMMENT를 넣다가 실제로 겪은 문제다.
--
-- 컬럼이 아직 ENUM으로 남아 있을 때만 ALTER를 만든다. 두 경우를 함께 감당하기 위해서다.
--   1) 새 환경에는 테이블이 아직 없다. Flyway가 Hibernate보다 먼저 돌기 때문에,
--      빈 스키마에서는 chat_room·notification이 존재하지 않는다.
--   2) 이미 손으로 ALTER를 돌린 DB에서는 아무것도 하지 않아야 한다.
-- 조건이 맞지 않으면 'SELECT 1'을 실행해 아무 일도 일어나지 않는다.
-- MySQL은 최상위에서 IF 문을 쓸 수 없어 변수와 PREPARE로 조건부 실행을 만든다.

SET @sql = (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE chat_room MODIFY COLUMN reference_type varchar(30) NULL',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_room'
      AND COLUMN_NAME = 'reference_type'
      AND DATA_TYPE = 'enum'
);
PREPARE widen_chat_room FROM @sql;
EXECUTE widen_chat_room;
DEALLOCATE PREPARE widen_chat_room;

SET @sql = (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE notification MODIFY COLUMN type varchar(30) NOT NULL',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'notification'
      AND COLUMN_NAME = 'type'
      AND DATA_TYPE = 'enum'
);
PREPARE widen_notification FROM @sql;
EXECUTE widen_notification;
DEALLOCATE PREPARE widen_notification;
