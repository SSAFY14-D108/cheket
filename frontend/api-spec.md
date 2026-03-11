# Cheket API 명세서 (Customer App)

> 이 문서는 Notion에서 내보낸 API 명세를 기반으로, Android 프로젝트의 화면과 매핑 가능성을 표기한 것입니다.
> 백엔드 서버와 실제 연결되어 있으며, DTO가 백엔드 응답에 맞춰 수정되었습니다.
> 공통 응답 형식: 성공 `{ httpStatusCode, responseMessage, data? }` / 실패 `{ httpStatusCode, errorMessage }`

### 상태 범례
| 표기 | 의미 |
|------|------|
| ✅ | backend에 존재 |
| ⚠️ | frontend에서 연결 대상 못찾음 (backend에는 있지만 Customer App에서 사용 안 함) |
| ❌ | backend에 아직 미구현 되어 있음 |

---

## 1. Auth (인증)

### 1-1. 로그인 ✅
- **POST** `/api/v1/auth/login`
- Auth: 불필요
- Body: `{ email: String, password: String }`
- Response: `{ accessToken, refreshToken }`
- 📱 **화면**: `LoginScreen.kt` — 이메일/비밀번호 입력 후 로그인 처리

### 1-2. 로그아웃 ✅
- **POST** `/api/v1/auth/logout`
- Auth: Bearer (Authorization 헤더)
- Body: 없음
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `SettingsScreen.kt` — 로그아웃 버튼

### 1-3. 토큰 재발급 ✅
- **POST** `/api/v1/auth/reissue`
- Auth: 불필요
- Body: `{ refreshToken: String }`
- Response: `{ accessToken, refreshToken }`
- 📱 **화면**: 없음 (AuthAuthenticator에서 자동 처리)

### 1-4. 회원가입 ✅
- **POST** `/api/v1/users`
- Auth: 불필요
- Body: `{ username, phoneNumber, email, password }`
- Response: 201 Created
- 📱 **화면**: `SignupScreen.kt` — 회원가입 폼

### 1-5. SMS 인증번호 발송 ✅
- **POST** `/api/v1/auth/sms/send`
- Auth: 불필요
- Body: `{ phoneNumber: String }`
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `SignupScreen.kt`, `FindAccountScreen.kt`, `PasswordResetScreen.kt` — SMS 인증 필요 화면들

### 1-6. SMS 인증번호 확인 ✅
- **POST** `/api/v1/auth/sms/verify`
- Auth: 불필요
- Body: `{ phoneNumber, code }`
- Response: `{ verified: true }`
- 📱 **화면**: `SignupScreen.kt`, `FindAccountScreen.kt`, `PasswordResetScreen.kt`

### 1-7. 비밀번호 찾기 (인증코드 발송) ✅
- **POST** `/api/v1/auth/password`
- Auth: 불필요
- Body: `{ email: String }`
- Response: 인증코드 발송
- 📱 **화면**: `PasswordResetScreen.kt`

### 1-8. 비밀번호 재설정 ✅
- **PATCH** `/api/v1/auth/reset-password`
- Auth: 불필요
- Body: `{ phoneNumber, code, newPassword }`
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `PasswordResetScreen.kt` — 새 비밀번호 입력
- ⚠️ 기존 명세: `/api/v1/auth/password` → 실제 backend: `/api/v1/auth/reset-password`

### 1-9. 이메일 찾기 ✅
- **POST** `/api/v1/users/email`
- Auth: Bearer (@SecurityRequirement)
- Body: `{ username, phoneNumber }`
- Response: `{ email: String }`
- 📱 **화면**: `FindAccountScreen.kt` — 이름+전화번호로 이메일 조회
- ⚠️ 기존 명세: `POST /api/v1/auth/email` (Auth 불필요) → 실제 backend: `POST /api/v1/users/email` (Auth 필요)

### 1-10. 유저 검색 (양도용) ✅
- **GET** `/api/v1/auth/search`
- Auth: Bearer (@AuthenticationPrincipal)
- Query: `userType, number`
- Response: `{ id: Long, name: String, number: String }`
- 📱 **화면**: `TransferScreen.kt` — 양도 대상자 전화번호 검색
- ⚠️ 기존 명세: Auth 불필요 → 실제 backend: Auth 필요. Response 필드명: `id`/`name`/`number` (기존 명세의 `userId`/`phone`과 다름)

### 1-11. 이메일 중복 확인 ✅ ⚠️
- **POST** `/api/v1/auth/duplicate`
- Auth: 불필요
- Body: `{ email: String }`
- Response: `{ httpStatusCode: 200 }` (중복 시 에러)
- 📱 **화면**: 기존 frontend에서 연결 대상 못찾음 (회원가입 시 활용 가능)

### 1-12. 비밀번호 변경 (로그인 상태) ✅ ⚠️
- **PATCH** `/api/v1/auth/change-password`
- Auth: Bearer (@AuthenticationPrincipal)
- Body: `{ currentPassword, newPassword }`
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `PasswordChangeScreen.kt` — 기존 비밀번호 + 새 비밀번호 입력
- ⚠️ 기존 api-spec에 누락되어 있었음. frontend `PasswordChangeScreen`에서 활용 가능

---

## 2. User (사용자)

### 2-1. 내 정보 조회 ❌
- **GET** `/api/v1/users`
- Auth: Bearer
- Response: `{ userId, username, phoneNumber, email }`
- 📱 **화면**: `MyPageScreen.kt` — 프로필 정보 표시
- ❌ backend에 아직 미구현 (UserController에 해당 GET 엔드포인트 없음)

### 2-2. 회원 탈퇴 ❌
- **DELETE** `/api/v1/users`
- Auth: Bearer
- Body: 없음
- 📱 **화면**: `SettingsScreen.kt` — 회원 탈퇴 처리
- ❌ backend에 아직 미구현

### 2-3. 찜한 공연 목록 조회 ❌
- **GET** `/api/v1/users/likes`
- Auth: Bearer
- Response: `[{ showId, title, posterUrl, venue, showDate, status }]`
- 📱 **화면**: `WishlistScreen.kt` — 찜 목록
- ❌ backend에 아직 미구현

### 2-4. 알림 설정 변경 ❌
- **PUT** `/api/v1/users/notifications`
- Auth: Bearer
- Body: `{ notificationEnable: Boolean }`
- 📱 **화면**: `SettingsScreen.kt` — 알림 ON/OFF 토글
- ❌ backend에 아직 미구현

---

## 3. Show (공연)

### 3-1. 공연 목록 조회 ✅
- **GET** `/api/v1/shows`
- Auth: 불필요
- Query: `region(enum), sort(POPULAR/LATEST/DEADLINE), keyword, page, size`
- Response (paginated):
```json
{
  "shows": [
    {
      "showId": 11,
      "title": "2026 서울 뮤직 페스티벌",
      "posterUrl": "https://example.com/poster1.jpg",
      "venue": "올림픽공원",
      "purchaseLimit": 4,
      "region": "SEOUL",
      "show": {
        "showStartDate": "2026-06-15",
        "showEndDate": "2026-06-17"
      },
      "reservation": {
        "startDate": "2026-06-01T10:00:00",
        "endDate": "2026-06-14T23:59:59"
      },
      "status": "ON_SALE"
    }
  ],
  "page": 0, "size": 20, "totalElements": 135, "totalPages": 7
}
```
- 📱 **화면**: `ConcertsScreen.kt` — 공연 목록 (검색, 지역 필터, 정렬), `HomeScreen.kt` — 홈 인기/최신 공연

### 3-2. 공연 상세 조회 ✅
- **GET** `/api/v1/shows/{showId}`
- Auth: Bearer (@SecurityRequirement)
- Response:
```json
{
  "showId": 11,
  "title": "2026 서울 뮤직 페스티벌",
  "posterUrl": "...",
  "description": "공연 설명",
  "artist": "아티스트명",
  "venue": "올림픽공원",
  "show": {
    "showStartDate": "2026-06-15",
    "showEndDate": "2026-06-17"
  },
  "reservation": {
    "startDate": "2026-06-01T10:00:00",
    "endDate": "2026-06-14T23:59:59"
  },
  "region": "SEOUL",
  "status": "ON_SALE",
  "isLiked": true,
  "likeCount": 1234,
  "grade": [
    { "gradeId": 1, "gradeName": "VIP", "price": 150000 },
    { "gradeId": 2, "gradeName": "R", "price": 120000 }
  ],
  "refundPolicy": [
    { "daysRemaining": 7, "refundRate": 100 },
    { "daysRemaining": 3, "refundRate": 50 }
  ]
}
```
- 📱 **화면**: `EventDetailScreen.kt` — 공연 상세 정보

### 3-3. 공연 회차 목록 조회 ✅
- **GET** `/api/v1/shows/{showId}/sessions`
- Auth: Bearer (@SecurityRequirement)
- Response:
```json
[
  {
    "sessionId": 1,
    "sessionDate": "2026-06-15",
    "sessionStartTime": "19:00:00",
    "remainingSeats": 150,
    "totalSeats": 500
  }
]
```
- 📱 **화면**: `EventDateSelectionScreen.kt` — 회차 선택

### 3-4. 좌석 조회 ✅
- **GET** `/api/v1/shows/{showId}/sessions/{sessionId}/seats`
- Auth: Bearer (@SecurityRequirement)
- Response:
```json
{
  "sections": [
    {
      "sectionId": 1,
      "sectionName": "가",
      "seats": [
        {
          "seatId": 1,
          "seatNo": "A-1",
          "grade": "VIP",
          "price": 150000,
          "status": "AVAILABLE"
        }
      ]
    }
  ]
}
```
- status: `AVAILABLE` | `HELD` | `PENDING_TX` | `SOLD`
- 📱 **화면**: `SeatSelectionScreen.kt` — 좌석 선택 (등급->좌석 2단계, 최대 4석)

### 3-5. 공연 찜하기 ✅
- **POST** `/api/v1/shows/{showId}/likes`
- Auth: Bearer (@AuthenticationPrincipal)
- Response: 201 Created
- 📱 **화면**: `EventDetailScreen.kt` — 찜 버튼

### 3-6. 공연 찜 취소 ✅
- **DELETE** `/api/v1/shows/{showId}/likes`
- Auth: Bearer (@AuthenticationPrincipal)
- 📱 **화면**: `EventDetailScreen.kt`, `WishlistScreen.kt` — 찜 해제

### 3-7. 공연장 목록 조회 ✅ ⚠️
- **GET** `/api/v1/shows/venue`
- Auth: 불필요
- Response: `[{ venueId, name, capacity }]`
- 📱 **화면**: 해당 없음 (주최측 기능이나 필터용으로 활용 가능)
- ⚠️ frontend에서 연결 대상 못찾음 (ShowService에 해당 엔드포인트 없음)

### 3-8. 환불 정책 조회 ✅
- **GET** `/api/v1/shows/{showId}/refund`
- Auth: 불필요
- Response: `{ refundPolicy: [{ daysRemaining, refundRate }], showStartDate }`
- 📱 **화면**: `EventDetailScreen.kt` — 환불 정책 표시, `TicketDetailScreen.kt` — 환불 전 정책 확인

### 3-9. 오픈 예정 공연 조회 ✅
- **GET** `/api/v1/shows/upcoming`
- Auth: 불필요
- Response:
```json
{
  "shows": [
    {
      "showId": 11,
      "title": "2026 서울 뮤직 페스티벌",
      "posterUrl": "https://example.com/poster1.jpg",
      "venue": "올림픽공원",
      "purchaseLimit": 4,
      "region": "SEOUL",
      "show": {
        "showStartDate": "2026-06-15",
        "showEndDate": "2026-06-17"
      },
      "reservation": {
        "startDate": "2026-06-01T10:00:00",
        "endDate": "2026-06-14T23:59:59"
      },
      "status": "UPCOMING"
    }
  ]
}
```
- 공연 목록(3-1)과 동일한 `ShowItem` 구조이나, 페이지네이션 없이 `{ shows: [...] }` 래퍼로 반환
- 📱 **화면**: `HomeScreen.kt` — 오픈 스케줄 섹션

---

## 4. Ticket (티켓)

### 4-1. 티켓 구매 (블록체인 TX) ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/purchase`
- Auth: Bearer
- Body: `{ sessionSeatId: List<Long> }`
- Response: `{ txId: Long }`
- 📱 **화면**: `PaymentScreen.kt` — 결제 처리 (REVIEW->APPROVED->SUCCESS/FAILURE)
- TX 상태는 `GET /wallets/transactions/{txId}`로 폴링 (1-2초 간격)
- ❌ backend에 아직 미구현

### 4-2. 티켓 컬렉션 조회 ❌
- **GET** `/api/v1/tickets/collection`
- Auth: Bearer
- Response: 티켓 배열 (공연 정보, 좌석 정보, 등급 포함)
- 📱 **화면**: `CollectionScreen.kt`, `ArchiveScreen.kt` — 소장 티켓 목록
- ❌ backend에 아직 미구현

### 4-3. 좌석 선점 (Redis SETNX) ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/seats`
- Auth: Bearer
- Body: `{ seatId: List<String> }`
- 5분 TTL 좌석 잠금
- 📱 **화면**: `SeatSelectionScreen.kt` — 좌석 선택 확정 시 호출
- ❌ backend에 아직 미구현

### 4-4. 티켓 양도 ❌
- **POST** `/api/v1/tickets/{ticketId}/transfer`
- Auth: Bearer
- Body: `{ phoneNumber: String }`
- 📱 **화면**: `TransferScreen.kt` — 전화번호 입력 -> 수신자 확인 -> 블록체인 전송
- ❌ backend에 아직 미구현

### 4-5. QR 체크인 코드 생성 ❌
- **POST** `/api/v1/tickets/{ticketId}/qr`
- Auth: Bearer
- Response: `{ qrData: "ticketId:OTP", expiresAt, ticketId, title, sectionName, seatNo }`
- 30초 OTP (Redis SET EX 30)
- 📱 **화면**: `QrCheckinScreen.kt` — QR 코드 표시
- ❌ backend에 아직 미구현

### 4-6. 내 예매 티켓 목록 조회 ❌
- **GET** `/api/v1/tickets/upcoming`
- Auth: Bearer
- Response:
```json
[
  {
    "ticketId": 1,
    "numbering": 1,
    "posterUrl": "...",
    "show": { "showId": 1, "name": "공연명", "date": "2026-06-15", "venue": "장소" },
    "price": 150000,
    "seatId": 1,
    "sectionName": "가",
    "seatNo": "A-1",
    "grade": "VIP",
    "status": "UPCOMING"
  }
]
```
- status: `UPCOMING` | `ON-SALE`
- 📱 **화면**: `MyTicketsScreen.kt` — 내 티켓 목록
- ❌ backend에 아직 미구현

### 4-7. 티켓 환불 ✅
- **POST** `/api/v1/tickets/{ticketId}/refund`
- Auth: Bearer (@SecurityRequirement)
- 📱 **화면**: `TicketDetailScreen.kt` — 환불 버튼

---

## 5. Queue (대기열)

### 5-1. 대기열 진입 ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/queue`
- Auth: Bearer
- Response: `{ sessionId, position, totalWaiting, estimatedWaitSec }`
- Redis ZADD (Sorted Set) + heartbeat SET EX 30
- 📱 **화면**: `WaitingQueueScreen.kt` — 대기열 진입 시 호출
- ❌ backend에 아직 미구현 (QueueController 없음)

### 5-2. 대기 상태 조회 (폴링) ❌
- **GET** `/api/v1/shows/{showId}/sessions/{sessionId}/queue/status`
- Auth: Bearer
- Response:
```json
{
  "position": 158,
  "totalWaiting": 4830,
  "estimatedWaitSec": 55,
  "status": "WAITING"
}
```
- status: `WAITING` -> `YOUR_TURN` (enterDeadlineSec: 300)
- 클라이언트 3-5초 간격 폴링, heartbeat TTL 자동 갱신
- 📱 **화면**: `WaitingQueueScreen.kt` — 대기 순번 표시, YOUR_TURN 시 좌석 선택 화면 이동
- ❌ backend에 아직 미구현

### 5-3. 대기열 이탈 ❌
- **DELETE** `/api/v1/shows/{showId}/sessions/{sessionId}/queue`
- Auth: Bearer
- Redis ZREM + DEL heartbeat + DEL active
- 📱 **화면**: `WaitingQueueScreen.kt` — 대기 취소 버튼
- ❌ backend에 아직 미구현

### 5-4. 좌석 선택 진입 ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/queue/enter`
- Auth: Bearer
- Response: `{ sessionToken, remainingSec: 300, purchaseLimit }`
- YOUR_TURN 상태에서 5분 이내 호출 필요
- 403: 아직 차례 아님, 410: 만료됨
- 📱 **화면**: `WaitingQueueScreen.kt` -> `SeatSelectionScreen.kt` 전환 시 호출
- ❌ backend에 아직 미구현

---

## 6. Wallet (지갑)

### 6-1. 잔액 조회 ❌
- **GET** `/api/v1/wallets/balance`
- Auth: Bearer
- Response: `{ balance: Int, walletAddress: String }`
- ERC-20 balanceOf 호출
- 📱 **화면**: `WalletScreen.kt` — 잔액 표시, `PaymentScreen.kt` — 결제 전 잔액 확인
- ❌ backend에 아직 미구현 (WalletController 없음)

### 6-2. 거래 내역 조회 ❌
- **GET** `/api/v1/wallets/transactions`
- Auth: Bearer
- Response:
```json
[
  {
    "transactionId": 1,
    "type": "PURCHASE",
    "amount": -150000,
    "description": "티켓 구매",
    "createdAt": "2026-06-15T19:00:00"
  }
]
```
- type: `CHARGE` | `PURCHASE` | `RESALE_LIST` | `RESALE_BUY` | `REFUND` | `TRANSFER` | `CHECK_IN` | `SETTLE`
- 📱 **화면**: `WalletHistoryScreen.kt`, `TxHistoryScreen.kt` — 거래 내역 목록
- ❌ backend에 아직 미구현

### 6-3. TX 상태 폴링 ❌
- **GET** `/api/v1/wallets/transactions/{txId}`
- Auth: Bearer
- Response: `{ txId, status, type, txHash, blockNumber, createdAt, updatedAt }`
- status: `PENDING` -> `SUBMITTED` -> `CONFIRMED` | `FAILED`
- 1-2초 간격 폴링
- 📱 **화면**: `PaymentScreen.kt` — 결제 진행 상태 표시
- ❌ backend에 아직 미구현

---

## 7. Resale (2차거래)

### 7-1. 2차거래 티켓 등록 ❌
- **POST** `/api/v1/resales/{ticketId}`
- Auth: Bearer
- Body: `{ resalePrice: Int }`
- 스마트 컨트랙트: 원가의 100% 이하만 가능
- 📱 **화면**: `ResaleCreateScreen.kt` — 판매 가격 입력
- ❌ backend에 아직 미구현

### 7-2. 2차거래 공연 목록 조회 ✅
- **GET** `/api/v1/resales`
- Auth: 불필요
- Query: `region, sort, keyword, page, size`
- Response (paginated):
```json
{
  "shows": [
    {
      "showId": 11,
      "title": "2026 서울 뮤직 페스티벌",
      "showStartDate": "2026-06-15",
      "showEndDate": "2026-06-17",
      "venue": "올림픽공원",
      "region": "SEOUL",
      "posterUrl": "...",
      "ticketCount": 5
    }
  ],
  "page": 0, "size": 20, "totalElements": 50, "totalPages": 3
}
```
- ⚠️ `ResaleShowItem`은 `ShowItem`과 달리 날짜가 flat 구조 (`showStartDate`/`showEndDate`)이며, `purchaseLimit`, `reservation`, `status` 필드 없음
- 📱 **화면**: `ResaleScreen.kt`, `ResaleListScreen.kt` — 2차거래 공연 목록

### 7-3. 특정 공연의 2차거래 티켓 목록 조회 ❌
- **GET** `/api/v1/resales/{showId}`
- Auth: 불필요
- Query: `sort(enum), sessionId, page, size`
- Response:
```json
{
  "show": { "showId": 11, "title": "...", "posterUrl": "...", "venue": "...", "region": "SEOUL" },
  "tickets": [
    {
      "ticketId": 1,
      "session": { "sessionId": 1, "sessionDate": "2026-06-15", "sessionStartTime": "21:00:00" },
      "sectionName": "가",
      "seatId": 1,
      "seatNo": "A-12",
      "grade": "VIP",
      "originalPrice": 1000000,
      "discountedPrice": 10000,
      "discountRate": 30.4
    }
  ],
  "page": 0, "size": 20, "totalElements": 135, "totalPages": 7
}
```
- 📱 **화면**: `ResaleTicketsScreen.kt` — 특정 공연의 2차거래 티켓 목록
- ❌ backend에 아직 미구현

### 7-4. 2차거래 티켓 구매 ❌
- **POST** `/api/v1/resales/{ticketId}` (구매)
- Auth: Bearer
- Response: "2차 거래 티켓 구매 완료"
- 📱 **화면**: `ResaleDetailScreen.kt` -> `ResalePurchaseCompleteScreen.kt`
- ❌ backend에 아직 미구현

### 7-5. 2차거래 등록 취소 ❌
- **DELETE** `/api/v1/resales/{ticketId}`
- Auth: Bearer
- Response: "2차 거래 티켓 등록 취소 완료"
- 📱 **화면**: `ResaleDetailScreen.kt` — 등록 취소 버튼 (판매자 본인일 경우)
- ❌ backend에 아직 미구현

---

## 구현 현황 요약

| 섹션 | 전체 | ✅ 구현 | ❌ 미구현 | ⚠️ FE 미연결 |
|------|------|---------|----------|-------------|
| 1. Auth | 12 | 12 | 0 | 2 (duplicate, change-password) |
| 2. User | 4 | 0 | 4 | - |
| 3. Show | 9 | 9 | 0 | 1 (venue) |
| 4. Ticket | 7 | 1 | 6 | - |
| 5. Queue | 4 | 0 | 4 | - |
| 6. Wallet | 3 | 0 | 3 | - |
| 7. Resale | 5 | 1 | 4 | - |
| **합계** | **44** | **23** | **21** | **3** |

---

## 화면-API 매핑 요약

| 화면 | 연결 가능 API | 매핑 확신도 |
|------|-------------|-----------|
| `LoginScreen` | 1-1 로그인 | ⭐⭐⭐ 높음 |
| `SignupScreen` | 1-4 회원가입, 1-5 SMS발송, 1-6 SMS확인, 1-11 이메일중복확인 | ⭐⭐⭐ 높음 |
| `FindAccountScreen` | 1-9 이메일찾기, 1-5 SMS발송, 1-6 SMS확인 | ⭐⭐⭐ 높음 |
| `PasswordResetScreen` | 1-7 비밀번호찾기, 1-8 비밀번호재설정, 1-5 SMS발송, 1-6 SMS확인 | ⭐⭐⭐ 높음 |
| `HomeScreen` | 3-1 공연목록(인기/최신), 3-9 오픈예정 | ⭐⭐ 중간 |
| `ConcertsScreen` | 3-1 공연목록 | ⭐⭐⭐ 높음 |
| `EventDetailScreen` | 3-2 공연상세, 3-5 찜하기, 3-6 찜취소, 3-8 환불정책 | ⭐⭐⭐ 높음 |
| `EventDateSelectionScreen` | 3-3 회차목록 | ⭐⭐⭐ 높음 |
| `WaitingQueueScreen` | 5-1 대기열진입, 5-2 대기상태폴링, 5-3 대기열이탈, 5-4 좌석선택진입 | ⭐⭐⭐ 높음 |
| `SeatSelectionScreen` | 3-4 좌석조회, 4-3 좌석선점 | ⭐⭐⭐ 높음 |
| `PaymentScreen` | 4-1 티켓구매, 6-1 잔액조회, 6-3 TX폴링 | ⭐⭐⭐ 높음 |
| `PurchaseFailedScreen` | (결제 실패 시 표시, API 직접 호출 없음) | — |
| `MyTicketsScreen` | 4-6 내티켓목록 | ⭐⭐⭐ 높음 |
| `TicketDetailScreen` | 4-7 환불, 3-8 환불정책 | ⭐⭐ 중간 |
| `QrCheckinScreen` | 4-5 QR생성 | ⭐⭐⭐ 높음 |
| `TransferScreen` | 1-10 유저검색, 4-4 양도 | ⭐⭐⭐ 높음 |
| `CollectionScreen` | 4-2 컬렉션조회 | ⭐⭐⭐ 높음 |
| `ArchiveScreen` | 4-2 컬렉션조회 | ⭐⭐ 중간 |
| `CollectibleTicketDetailScreen` | (컬렉션 상세, 추가 API 필요 가능) | ⭐ 낮음 |
| `ResaleScreen` | 7-2 2차거래공연목록 | ⭐⭐⭐ 높음 |
| `ResaleListScreen` | 7-2 2차거래공연목록 | ⭐⭐ 중간 |
| `ResaleTicketsScreen` | 7-3 특정공연2차거래티켓 | ⭐⭐⭐ 높음 |
| `ResaleDetailScreen` | 7-4 2차거래구매, 7-5 등록취소 | ⭐⭐ 중간 |
| `ResaleCreateScreen` | 7-1 2차거래등록 | ⭐⭐⭐ 높음 |
| `ResalePurchaseCompleteScreen` | (구매 완료 표시, API 직접 호출 없음) | — |
| `WalletScreen` | 6-1 잔액조회 | ⭐⭐⭐ 높음 |
| `WalletHistoryScreen` | 6-2 거래내역 | ⭐⭐⭐ 높음 |
| `TxHistoryScreen` | 6-2 거래내역 | ⭐⭐ 중간 |
| `WithdrawScreen` | (출금 API 미정의, 추후 추가 필요) | ⭐ 낮음 |
| `MyPageScreen` | 2-1 내정보조회 | ⭐⭐⭐ 높음 |
| `SettingsScreen` | 1-2 로그아웃, 2-2 회원탈퇴, 2-4 알림설정 | ⭐⭐⭐ 높음 |
| `PasswordChangeScreen` | 1-12 비밀번호변경 | ⭐⭐⭐ 높음 |
| `WishlistScreen` | 2-3 찜목록, 3-6 찜취소 | ⭐⭐⭐ 높음 |

### 매핑 확신도 기준
- ⭐⭐⭐ **높음**: 화면 UI와 API 요청/응답이 명확히 대응됨. 바로 연결 가능.
- ⭐⭐ **중간**: 화면 기능과 API가 관련 있으나 데이터 모델 차이가 있을 수 있음. DTO 조정 필요.
- ⭐ **낮음**: API가 아직 정의되지 않았거나, 화면 구조와 API 간 상당한 차이 존재.
- **—**: API 직접 호출 없는 화면 (결과 표시, 네비게이션 등).

---

## 참고: 주최측(Host) API (Customer App 미사용)

아래 API들은 주최측 전용이며, Customer App에서는 사용하지 않습니다:
- `POST /api/v1/hosts` — 주최자 회원가입
- `POST /api/v1/hosts/auth/login` — 주최자 로그인
- `POST /api/v1/hosts/business-no/duplicate` — 사업자번호 중복 확인
- `GET /api/v1/hosts` — 주최자 정보 조회
- `PATCH /api/v1/hosts` — 주최자 정보 수정
- 기타 `/api/v1/hosts/` 경로의 API들
