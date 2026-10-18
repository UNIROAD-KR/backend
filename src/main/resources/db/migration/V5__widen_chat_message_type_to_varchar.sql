-- 채팅 사진 전송을 위해 MessageType에 IMAGE를 더한다.
-- chat_message.type은 Hibernate가 MySQL 네이티브 ENUM('TALK','ENTER','QUIT')으로 만들었을 가능성이 높고,
-- ddl-auto=update는 ENUM의 허용 값을 넓혀주지 않아 IMAGE를 넣는 INSERT가 "Data truncated"로 실패한다.
-- V2와 같은 방식으로, 컬럼이 아직 ENUM일 때만 varchar로 바꾼다. 테이블이 없거나 이미 varchar면 아무것도 하지 않는다.

SET @sql = (
    SELECT IF(COUNT(*) > 0,
              'ALTER TABLE chat_message MODIFY COLUMN type varchar(20) NULL',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_message'
      AND COLUMN_NAME = 'type'
      AND DATA_TYPE = 'enum'
);
PREPARE widen_chat_message_type FROM @sql;
EXECUTE widen_chat_message_type;
DEALLOCATE PREPARE widen_chat_message_type;
