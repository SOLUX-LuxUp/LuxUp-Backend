-- button 테이블 icon_color 컬럼 확장 및 기존 HEX 데이터를 토큰으로 변환
-- 신규 DB 셋업 시에는 실행할 필요 없습니다 (button 테이블은 유저 생성 데이터라 시드 자체가 없음).
-- 기존 HEX 값을 가진 로컬/배포 DB에서만 실행하면 됩니다.
--
-- MySQL Workbench에서 실행 시 Safe Update Mode 에러가 나면 아래 먼저 실행:
-- SET SQL_SAFE_UPDATES = 0;

ALTER TABLE button MODIFY COLUMN icon_color VARCHAR(20) NOT NULL;

-- 현재 랜덤 배정 풀 5개
UPDATE button SET icon_color = 'red'    WHERE icon_color = '#FF5733';
UPDATE button SET icon_color = 'cyan'   WHERE icon_color = '#00BFFF';
UPDATE button SET icon_color = 'green'  WHERE icon_color = '#A8D8A8';
UPDATE button SET icon_color = 'orange' WHERE icon_color = '#FFC107';
UPDATE button SET icon_color = 'black'  WHERE icon_color = '#1A1A1A';

-- 개발 초기 테스트 데이터에 남아있던 구버전 색상값
UPDATE button SET icon_color = 'cyan'   WHERE icon_color = '#A8D8EA';
UPDATE button SET icon_color = 'orange' WHERE icon_color = '#FFD3B6';

-- 검증: 결과가 전부 토큰 문자열이어야 정상 (# 으로 시작하는 값이 남아있으면 안 됨)
SELECT DISTINCT icon_color FROM button;