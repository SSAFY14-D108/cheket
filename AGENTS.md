# Cheket (체켓) - Frontend Repository

NFT 기반 티켓팅 플랫폼 프론트엔드.

## Repository Structure

동일한 GitLab 원격 저장소(`S14P21D108`)를 브랜치 기반으로 분리 운영:

| 로컬 디렉토리 | 브랜치 | 용도 |
|---|---|---|
| `S14P21D108_frontend/` | `fe/*` 브랜치 | 프론트엔드 개발 |
| `S14P21D108_backend/` | `be/*` 브랜치 | 백엔드 개발 (API 참조용) |

## Backend API Reference

프론트엔드 작업 시 백엔드 코드를 API 스펙 참조용으로 사용:

```
S14P21D108_backend/
├── src/main/java/com/ssafy/cheket/
│   ├── domain/show/
│   │   ├── controller/ShowController.java     # GET /api/v1/shows (페이징, 정렬, 검색)
│   │   ├── dto/ShowDetailResponseDto.java     # 공연 상세 응답
│   │   └── entity/enums/
│   │       ├── ShowSort.java                  # LATEST, POPULARITY, CLOSING_SOON
│   │       └── Region.java                    # SEOUL, GYEONGGI, BUSAN, ...
│   ├── domain/ticket/controller/              # 티켓 API
│   ├── domain/resale/controller/              # 리세일 API
│   └── domain/auth/controller/                # 인증 API
```

### Backend Enum → Frontend 매핑

| Backend Enum | 값 | Frontend 사용 |
|---|---|---|
| `ShowSort` | LATEST, POPULARITY, CLOSING_SOON | sort 쿼리 파라미터 |
| `ShowStatus` | UPCOMING, ON_SALE, SOLD_OUT, COMPLETED | 상태 배지 표시 |
| `Region` | SEOUL, GYEONGGI, BUSAN, DAEGU, INCHEON, GWANGJU, DAEJEON, ULSAN, SEJONG, GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU | 지역 필터 |

## Working Patterns

- 백엔드 API 스펙 확인 → Controller/DTO 코드 직접 참조
- API 응답 형식은 `ApiResponse<T>` 래퍼 (`code`, `message`, `data`)
- 페이징 응답: `PageResponse<T>` (`content`, `page`, `totalPages`, `totalElements`)

## Commit Convention

```
[FE] feat: 새 기능 추가
[FE] fix: 버그 수정
[FE] refactor: 리팩토링
```

## Build

```bash
cd frontend/apps/customer-app/android
./gradlew assembleDebug
```
