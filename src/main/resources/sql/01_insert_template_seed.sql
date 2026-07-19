-- template 원본 시드 데이터 (온보딩 템플릿 3종)
-- 신규 DB 셋업 시 template_button 시드보다 먼저 실행해야 합니다 (FK 의존성).

INSERT INTO template (template_name, template_type, description, is_active, created_at, updated_at, deleted_at) VALUES
                                                                                                                    ('잊지 않기 (Keep up)', 'memory', '바쁜 일상 속 놓치기 쉬운 것들을 기억해요', 1, NOW(), NOW(), NULL),
                                                                                                                    ('나 챙기기 (Self care)', 'self_care', '소중한 내 몸과 마음의 리듬을 기억해요', 1, NOW(), NOW(), NULL),
                                                                                                                    ('성장하기', 'productivity', '배우고 나아가는 순간들을 기억해요', 1, NOW(), NOW(), NULL);