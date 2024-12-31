-- 테이블 생성
CREATE TABLE todo (
                      id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'To-Do 항목의 고유 식별자',
                      title VARCHAR(255) NOT NULL COMMENT 'To-Do 항목의 제목',
                      description TEXT COMMENT 'To-Do 항목의 상세 설명',
                      done BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'To-Do 항목 완료 여부'
);