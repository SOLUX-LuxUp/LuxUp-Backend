-- icon_color를 HEX 코드에서 색상 토큰 문자열로 되돌리는 마이그레이션
--
-- 신규 DB 세팅 시에는 이 파일을 실행할 필요 없습니다.
-- 01, 02번 시드 스크립트만 실행하면 이미 토큰 문자열로 들어갑니다.
--
-- 이 파일은 과거에 icon_color를 HEX 코드로 잘못 넣어둔 로컬 DB를 가진
-- 팀원만 실행하면 됩니다. (백엔드는 토큰 문자열만 전달하는 것이 최종 방침입니다.)

-- 컬럼 길이는 이미 VARCHAR(20)으로 확장되어 있어야 합니다.
-- 아직 확장 안 하셨다면 아래 두 줄 먼저 실행하세요.
-- ALTER TABLE template_button MODIFY COLUMN icon_color VARCHAR(20) NOT NULL;
-- ALTER TABLE button MODIFY COLUMN icon_color VARCHAR(20) NOT NULL;

UPDATE template_button SET icon_name = 'clean',     icon_color = 'indigo'   WHERE preset_id = 1;  -- 침구류 세탁하기
UPDATE template_button SET icon_name = 'clothes',   icon_color = 'blue'     WHERE preset_id = 2;  -- 잠옷 세탁하기
UPDATE template_button SET icon_name = 'clean',     icon_color = 'blue'     WHERE preset_id = 3;  -- 냉장고 정리하기
UPDATE template_button SET icon_name = 'clean',     icon_color = 'darkgrey' WHERE preset_id = 4;  -- 쓰레기 버리기
UPDATE template_button SET icon_name = 'clean',     icon_color = 'grey'     WHERE preset_id = 5;  -- 설거지하기
UPDATE template_button SET icon_name = 'clean',     icon_color = 'black'    WHERE preset_id = 6;  -- 집 청소하기
UPDATE template_button SET icon_name = 'plant',     icon_color = 'green'    WHERE preset_id = 7;  -- 식물에 물 주기
UPDATE template_button SET icon_name = 'dog',       icon_color = 'orange'   WHERE preset_id = 8;  -- 반려동물 산책하기
UPDATE template_button SET icon_name = 'pay',       icon_color = 'indigo'   WHERE preset_id = 9;  -- 카드값 납부
UPDATE template_button SET icon_name = 'pay',       icon_color = 'purple'   WHERE preset_id = 10; -- 보험료 납부
UPDATE template_button SET icon_name = 'pay',       icon_color = 'red'      WHERE preset_id = 11; -- 구독 프로그램 갱신
UPDATE template_button SET icon_name = 'pay',       icon_color = 'blue'     WHERE preset_id = 12; -- 적금 이체
UPDATE template_button SET icon_name = 'shopping1', icon_color = 'orange'   WHERE preset_id = 13; -- 택배 반품
UPDATE template_button SET icon_name = 'calendar',  icon_color = 'red'      WHERE preset_id = 14; -- 일정 정리
UPDATE template_button SET icon_name = 'pay',       icon_color = 'green'    WHERE preset_id = 15; -- 정산 완료
UPDATE template_button SET icon_name = 'fire',      icon_color = 'red'      WHERE preset_id = 16; -- 가스불 확인
UPDATE template_button SET icon_name = 'lightning', icon_color = 'yellow'   WHERE preset_id = 17; -- 콘센트 확인
UPDATE template_button SET icon_name = 'lightning', icon_color = 'cyan'     WHERE preset_id = 18; -- 냉난방기 확인
UPDATE template_button SET icon_name = 'lightning', icon_color = 'orange'   WHERE preset_id = 19; -- 전등 확인
UPDATE template_button SET icon_name = 'lock',      icon_color = 'black'    WHERE preset_id = 20; -- 창문 확인
UPDATE template_button SET icon_name = 'door',      icon_color = 'black'    WHERE preset_id = 21; -- 문 잠금 확인
UPDATE template_button SET icon_name = 'medicine',  icon_color = 'red'      WHERE preset_id = 22; -- 약 먹기
UPDATE template_button SET icon_name = 'medicine',  icon_color = 'blue'     WHERE preset_id = 23; -- 영양제 먹기
UPDATE template_button SET icon_name = 'gym',       icon_color = 'cyan'     WHERE preset_id = 24; -- 스트레칭 하기
UPDATE template_button SET icon_name = 'gym',       icon_color = 'green'    WHERE preset_id = 25; -- 운동하기
UPDATE template_button SET icon_name = 'medicine',  icon_color = 'red'      WHERE preset_id = 26; -- 혈당 체크
UPDATE template_button SET icon_name = 'health',    icon_color = 'red'      WHERE preset_id = 27; -- 병원 방문
UPDATE template_button SET icon_name = 'health',    icon_color = 'red'      WHERE preset_id = 28; -- 치과 방문
UPDATE template_button SET icon_name = 'gym',       icon_color = 'green'    WHERE preset_id = 29; -- 운동하기
UPDATE template_button SET icon_name = 'gym',       icon_color = 'cyan'     WHERE preset_id = 30; -- 스트레칭하기
UPDATE template_button SET icon_name = 'health',    icon_color = 'grey'     WHERE preset_id = 31; -- 체중 체크
UPDATE template_button SET icon_name = 'medicine',  icon_color = 'red'      WHERE preset_id = 32; -- 혈당 체크
UPDATE template_button SET icon_name = 'selfcare',  icon_color = 'purple'   WHERE preset_id = 33; -- 피부 관리
UPDATE template_button SET icon_name = 'food',      icon_color = 'orange'   WHERE preset_id = 34; -- 식단 관리
UPDATE template_button SET icon_name = 'health',    icon_color = 'green'    WHERE preset_id = 35; -- 명상
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'yellow'   WHERE preset_id = 36; -- 일기 쓰기
UPDATE template_button SET icon_name = 'health',    icon_color = 'yellow'   WHERE preset_id = 37; -- 디지털 디톡스
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'pink'     WHERE preset_id = 38; -- 감정 기록하기
UPDATE template_button SET icon_name = 'camera',    icon_color = 'purple'   WHERE preset_id = 39; -- 취미 생활하기
UPDATE template_button SET icon_name = 'sleep',     icon_color = 'blue'     WHERE preset_id = 40; -- 휴식
UPDATE template_button SET icon_name = 'sleep',     icon_color = 'purple'   WHERE preset_id = 41; -- 일찍 자기
UPDATE template_button SET icon_name = 'medicine',  icon_color = 'blue'     WHERE preset_id = 42; -- 영양제 먹기
UPDATE template_button SET icon_name = 'health',    icon_color = 'darkgrey' WHERE preset_id = 43; -- 카페인 줄이기
UPDATE template_button SET icon_name = 'sun',       icon_color = 'yellow'   WHERE preset_id = 44; -- 햇빛 보기
UPDATE template_button SET icon_name = 'liquid',    icon_color = 'cyan'     WHERE preset_id = 45; -- 물 마시기
UPDATE template_button SET icon_name = 'calendar',  icon_color = 'orange'   WHERE preset_id = 46; -- 계획 세우기
UPDATE template_button SET icon_name = 'calendar',  icon_color = 'red'      WHERE preset_id = 47; -- 일정 정리
UPDATE template_button SET icon_name = 'chat',      icon_color = 'grey'     WHERE preset_id = 48; -- 메일 정리
UPDATE template_button SET icon_name = 'call',      icon_color = 'black'    WHERE preset_id = 49; -- 연락 돌리기
UPDATE template_button SET icon_name = 'note',      icon_color = 'green'    WHERE preset_id = 50; -- 자료 정리
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'yellow'   WHERE preset_id = 51; -- 언어 공부
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'indigo'   WHERE preset_id = 52; -- 코딩 공부
UPDATE template_button SET icon_name = 'book',      icon_color = 'yellow'   WHERE preset_id = 53; -- 책 읽기
UPDATE template_button SET icon_name = 'book',      icon_color = 'orange'   WHERE preset_id = 54; -- 강의 듣기
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'red'      WHERE preset_id = 55; -- 자격증 공부
UPDATE template_button SET icon_name = 'book',      icon_color = 'blue'     WHERE preset_id = 56; -- 뉴스 읽기
UPDATE template_button SET icon_name = 'book',      icon_color = 'indigo'   WHERE preset_id = 57; -- 논문 읽기
UPDATE template_button SET icon_name = 'note',      icon_color = 'pink'     WHERE preset_id = 58; -- 트렌드 서치
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'yellow'   WHERE preset_id = 59; -- 일기 쓰기
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'pink'     WHERE preset_id = 60; -- 아이디어 정리
UPDATE template_button SET icon_name = 'pencil',    icon_color = 'purple'   WHERE preset_id = 61; -- 포트폴리오 작업

-- 검증
SELECT preset_id, button_name, icon_name, icon_color
FROM template_button
ORDER BY preset_id;
