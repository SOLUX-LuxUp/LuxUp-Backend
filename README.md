# 👆 TapTap - 백엔드 레포지토리

터치 한 번으로 기록하는 일상, TapTap의 백엔드 레포지토리입니다.

<br>

## 👥 팀원 소개

| 심세희 | 백은서 | 김정민 |
|---|---|---|
| BE Lead | BE | BE |
| [@sehui516](https://github.com/sehui516) | [@eunseocandoit](https://github.com/eunseocandoit) | [@min1i](https://github.com/min1i) |
| 기록 · 인사이트 · 설정 <br> DevOps (배포 · CI/CD) | 인증 · 온보딩 · 버튼 코어 | 팀 스페이스 |

<br>

## 🛠 기술 스택
 
**언어 & 프레임워크**
 
![Java](https://img.shields.io/badge/JAVA-17-4E4E4E?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/SPRING%20BOOT-3.5.15-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
 
**보안**
 
![Spring Security](https://img.shields.io/badge/SPRING%20SECURITY-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-JSON%20WEB%20TOKEN-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
 
**데이터베이스**
 
![JPA](https://img.shields.io/badge/JPA-HIBERNATE-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MYSQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
 
**인프라 & 자동화 툴**
 
![AWS](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Swagger](https://img.shields.io/badge/SWAGGER-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![GitHub Actions](https://img.shields.io/badge/GITHUB%20ACTIONS-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
 
**AI**
 
![Gemini](https://img.shields.io/badge/GEMINI-3.5%20FLASH-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white)

<br>

## ✨ 주요 기능

- **인증**: 이메일 가입/로그인
- **버튼 & 기록**: 버튼 생성/수정/삭제, 탭 한 번으로 기록, 타임라인 메모·이모지
- **알림**: 버튼별 리마인더 (요일/시각/간격 설정)
- **인사이트**: 일간·주간·월간 리포트, AI 기반 라이프스타일 분석 및 버튼 추천
- **팀 스페이스**: 팀 생성/초대, 공유 버튼, 팀원 활동 통계
- **설정**: 프로필 관리, 알림 마스터 설정, 로그아웃/탈퇴

<br>

## 🏗 시스템 아키텍처

<img src=""/>

<br>

## 📁 디렉터리 구조

```
LuxUp-Backend
├── .github/workflows
│   └── deploy.yml
├── gradle/wrapper
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src
│   ├── main
│   │   ├── java/com/taptap/backend
│   │   │   ├── button/        # 버튼 코어·카테고리
│   │   │   ├── config/        # 공통 설정 (Security, Swagger 등)
│   │   │   ├── insight/       # 인사이트
│   │   │   ├── record/        # 버튼 기록
│   │   │   ├── reminder/      # 알림
│   │   │   ├── setting/       # 설정
│   │   │   ├── team/          # 팀 스페이스
│   │   │   ├── template/      # 온보딩·템플릿
│   │   │   ├── user/          # 인증·계정 관리
│   │   │   └── BackendApplication.java
│   │   └── resources/sql      # 시드·마이그레이션 SQL
│   └── test/java/com/taptap/backend
├── .gitattributes
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

Made by **SOLUX-LuxUp**
