-- 발급된 Access Token을 서버가 즉시 무효화할 수 있게 하는 컬럼.
--
-- 로그아웃과 비밀번호 변경은 지금까지 refresh_token 행만 지웠기 때문에,
-- 이미 발급된 Access Token은 남은 수명(30분) 동안 그대로 통했다.
-- 토큰에 발급 시점의 token_version을 실어 두고 인증할 때 회원 행의 값과 대조하면,
-- 값을 1 올리는 것만으로 그 회원에게 발급된 토큰이 전부 무효가 된다.
--
-- 인증 필터는 이미 요청마다 회원을 조회하므로 대조에 드는 추가 쿼리는 없다.
-- 별도 저장소(Redis 등)를 두지 않기 위한 선택이다.
--
-- 컬럼이 아직 없을 때만 ALTER를 만든다. 새 환경에서는 Flyway가 Hibernate보다 먼저 돌아
-- member 테이블 자체가 없을 수 있고, 이미 손으로 추가한 DB에서는 아무 일도 하지 않아야 한다.
SET @sql = (
    SELECT IF(COUNT(*) = 1 AND SUM(COLUMN_NAME = 'token_version') = 0,
              'ALTER TABLE member ADD COLUMN token_version INT NOT NULL DEFAULT 0',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'member'
      AND COLUMN_NAME IN ('id', 'token_version')
);
PREPARE add_token_version FROM @sql;
EXECUTE add_token_version;
DEALLOCATE PREPARE add_token_version;
