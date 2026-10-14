-- 동행 구하기 글은 더 이상 오픈채팅 링크를 받지도 저장하지도 않는다.
--
-- 엔티티에서 chat_link를 뺐지만 ddl-auto=update는 이미 있는 컬럼을 건드리지 않는다.
-- NOT NULL인 채로 두면 새 글 INSERT가 값을 주지 않아 실패하므로 NULL을 허용하게 푼다.
-- 이미 저장된 링크는 지우지 않고 남겨 둔다. 컬럼 삭제는 필요할 때 다음 버전에서 한다.
--
-- 컬럼이 있을 때만 ALTER를 만든다. 새 환경에서는 Flyway가 Hibernate보다 먼저 돌아
-- companion_post 테이블 자체가 없을 수 있다.
SET @sql = (
    SELECT IF(COUNT(*) = 1,
              'ALTER TABLE companion_post MODIFY COLUMN chat_link VARCHAR(255) NULL',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'companion_post'
      AND COLUMN_NAME = 'chat_link'
);
PREPARE relax_chat_link FROM @sql;
EXECUTE relax_chat_link;
DEALLOCATE PREPARE relax_chat_link;
