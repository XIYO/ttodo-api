ALTER USER sa SET PASSWORD 'root';

-- 테이블 생성
CREATE TABLE todo (
                      id SERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      done BOOLEAN NOT NULL DEFAULT FALSE
);

-- 컬럼별 주석 추가
COMMENT ON COLUMN todo.id IS 'To-Do 항목의 고유 식별자';
COMMENT ON COLUMN todo.title IS 'To-Do 항목의 제목';
COMMENT ON COLUMN todo.description IS 'To-Do 항목의 상세 설명';
COMMENT ON COLUMN todo.done IS 'To-Do 항목 완료 여부';