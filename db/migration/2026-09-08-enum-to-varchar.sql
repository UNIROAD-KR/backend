-- 운영/기존 DB에 한 번 실행하는 스크립트입니다. (이 프로젝트는 Flyway 없이 ddl-auto=update로 운영합니다.)
--
-- 왜 필요한가
--   Hibernate는 MySQL에서 @Enumerated(EnumType.STRING) 필드를 네이티브 ENUM('A','B',...) 컬럼으로 만듭니다.
--   ddl-auto=update는 이미 만들어진 ENUM 컬럼의 허용 값을 넓혀주지 않기 때문에,
--   상수를 새로 추가하면 그 값을 넣는 INSERT가 "Data truncated for column" 오류로 실패합니다.
--   실제로 ChatReferenceType.COMPANION / NotificationType.COMMENT 를 넣자 INSERT가 죽는 것을 확인했습니다.
--
-- 무엇을 하는가
--   두 컬럼을 varchar(30)으로 바꿔, 앞으로 상수를 추가할 때 DB를 건드리지 않아도 되게 합니다.
--   엔티티도 columnDefinition = "varchar(30)" 으로 맞춰두었으므로 새 환경은 처음부터 varchar로 생성됩니다.
--
-- 배포 순서
--   이 스크립트를 먼저 실행한 뒤 새 버전을 배포하세요. (반대로 하면 그 사이 댓글 알림과
--   동행 채팅방 생성이 500으로 실패합니다.)

ALTER TABLE chat_room   MODIFY COLUMN reference_type varchar(30) NULL;
ALTER TABLE notification MODIFY COLUMN type          varchar(30) NOT NULL;
