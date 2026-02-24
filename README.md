# 날씨는_쿠름이 (React + Spring Boot)

성공회대학교 날씨 서비스를 React 프론트엔드 + Spring Boot(H2 DB) 백엔드로 재구현한 프로젝트입니다.

---

## 프로젝트 구조

```
skhuweather-react/
├── backend/          ← Spring Boot (H2 DB)
└── frontend/         ← React (Vite)
```

---

## 백엔드 실행 (Spring Boot + H2)

```bash
cd backend
./mvnw spring-boot:run
# 또는 Maven 설치된 경우:
mvn spring-boot:run
```

- 서버: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:skhuweather`
  - User: `sa` / Password: (빈칸)

### API 목록

| Method | URL               | 설명                        |
|--------|-------------------|-----------------------------|
| GET    | /api/weather      | 외부 날씨 API 프록시         |
| GET    | /api/notices      | 학교 공지사항 (H2 DB)        |
| GET    | /api/reports      | 날씨 제보 현황 (H2 DB)       |
| POST   | /api/reports      | 날씨 제보 등록 (H2 DB)       |

---

## 프론트엔드 실행 (React)

```bash
cd frontend
npm install
npm run dev
```

- 앱: http://localhost:5173
- 백엔드가 먼저 실행되어 있어야 합니다.

---

## H2 DB 테이블

- **school_notice**: 학교 공지사항 (앱 시작 시 8개 샘플 데이터 자동 삽입)
- **weather_report**: 날씨 제보 (비/흐림/화창/공기나쁨/바람/눈, 세션ID로 중복 방지, 3시간 단위 초기화)
