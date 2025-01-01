-- 테이블 생성
CREATE TABLE TODO (
                      ID BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'TO-DO 항목의 고유 식별자',
                      TITLE VARCHAR(255) NOT NULL COMMENT 'TO-DO 항목의 제목',
                      DESCRIPTION TEXT COMMENT 'TO-DO 항목의 상세 설명',
                      DONE BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'TO-DO 항목 완료 여부'
) COMMENT 'TO-DO 항목';