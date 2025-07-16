-- Test users with specific UUIDs
INSERT INTO member (id, email, password, nickname, created_at, updated_at) 
VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'anon@ttodo.dev', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '익명사용자', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO member (id, email, password, nickname, created_at, updated_at) 
VALUES ('00000000-0000-0000-0000-000000000000', 'root@ttodo.dev', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '루트관리자', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Profiles for test users
INSERT INTO profiles (owner_id, theme, introduction, time_zone, locale, created_at, updated_at) 
VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'PINKY', '테스트 익명 사용자', 'Asia/Seoul', 'ko-KR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (owner_id) DO NOTHING;

INSERT INTO profiles (owner_id, theme, introduction, time_zone, locale, created_at, updated_at) 
VALUES ('00000000-0000-0000-0000-000000000000', 'PINKY', '테스트 루트 관리자', 'Asia/Seoul', 'ko-KR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (owner_id) DO NOTHING;