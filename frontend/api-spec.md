# Cheket API 명세서 (Customer App)

> 이 문서는 Notion에서 내보낸 API 명세를 기반으로, Android 프로젝트의 화면과 매핑 가능성을 표기한 것입니다.
> 백엔드 서버와 실제 연결되어 있으며, DTO가 백엔드 응답에 맞춰 수정되었습니다.
> 공통 응답 형식: 성공 `{ httpStatusCode, responseMessage, data? }` / 실패 `{ httpStatusCode, errorMessage }`

### 상태 범례
| 표기 | 의미 |
|------|------|
| ✅ | backend에 존재 |
| 🔗 | backend + frontend 연결 완료 |
| ⚠️ | frontend에서 연결 대상 못찾음 (backend에는 있지만 Customer App에서 사용 안 함) |
| ❌ | backend에 아직 미구현 되어 있음 |

---

## 1. Auth (인증)

### 1-1. 로그인 🔗
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

### 1-3. 토큰 재발급 🔗
- **POST** `/api/v1/auth/reissue`
- Auth: 불필요
- Body: `{ refreshToken: String }`
- Response: `{ accessToken, refreshToken }`
- 📱 **화면**: 없음 (AuthAuthenticator에서 자동 처리)

### 1-4. 회원가입 🔗
- **POST** `/api/v1/users`
- Auth: 불필요
- Body: `{ username, phoneNumber, email, password }`
- Response: 201 Created
- 📱 **화면**: `SignupScreen.kt` — 회원가입 폼

### 1-5. SMS 인증번호 발송 🔗
- **POST** `/api/v1/auth/sms/send`
- Auth: 불필요
- Body: `{ phoneNumber: String }` (하이픈 포함 형식: `010-1234-5678`)
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `SignupScreen.kt` (SignupViewModel에서 호출)

### 1-6. SMS 인증번호 확인 🔗
- **POST** `/api/v1/auth/sms/verify`
- Auth: 불필요
- Body: `{ phoneNumber, code }`
- Response: `{ verified: true }`
- 📱 **화면**: `SignupScreen.kt` (SignupViewModel에서 호출)

### 1-7. 비밀번호 찾기 (인증코드 발송) 🔗
- **POST** `/api/v1/auth/password`
- Auth: 불필요
- Body: `{ email: String }`
- Response: 인증코드 발송
- 📱 **화면**: `PasswordResetScreen.kt` — 이메일 입력 후 "발송" 버튼

### 1-8. 비밀번호 재설정 🔗
- **PATCH** `/api/v1/auth/reset-password`
- Auth: 불필요
- Body: `{ phoneNumber, code, newPassword }`
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `PasswordResetScreen.kt` — 전화번호+인증코드+새 비밀번호 입력 후 "재설정" 버튼

### 1-9. 이메일 찾기 🔗
- **POST** `/api/v1/users/email`
- Auth: Bearer (@SecurityRequirement)
- Body: `{ username, phoneNumber }`
- Response: `{ email: String }`
- 📱 **화면**: `FindAccountScreen.kt` — 이름+전화번호 입력 후 계정 찾기

### 1-10. 유저 검색 (양도용) ✅
- **GET** `/api/v1/auth/search`
- Auth: Bearer (@AuthenticationPrincipal)
- Query: `userType, number`
- Response: `{ id: Long, name: String, number: String }`
- 📱 **화면**: `TransferScreen.kt`

### 1-11. 이메일 중복 확인 🔗
- **POST** `/api/v1/auth/duplicate`
- Auth: 불필요
- Body: `{ email: String }`
- Response: `{ httpStatusCode: 200 }` (중복 시 409 에러)
- 📱 **화면**: `SignupScreen.kt` — "중복확인" 버튼 (SignupViewModel에서 호출)

### 1-12. 비밀번호 변경 (로그인 상태) 🔗
- **PATCH** `/api/v1/auth/change-password`
- Auth: Bearer (@AuthenticationPrincipal)
- Body: `{ oldPassword, newPassword }`
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `PasswordChangeScreen.kt` — 현재/새 비밀번호 입력 후 "변경하기" 버튼

---

## 2. User (사용자)

### 2-1. 내 정보 조회 🔗
- **GET** `/api/v1/users`
- Auth: Bearer
- Response: `{ userId, username, phoneNumber, email }`
- 📱 **화면**: `MyPageScreen.kt` — 프로필 정보 표시 (MyPageViewModel 연결)

### 2-2. 회원 탈퇴 🔗
- **DELETE** `/api/v1/users`
- Auth: Bearer (Authorization 헤더 + 선택적 Refresh-Token 헤더)
- Response: `{ httpStatusCode: 200, responseMessage: "회원 탈퇴 완료" }`
- 📱 **화면**: `MyPageScreen.kt` — "회원 탈퇴" 텍스트 → 2단계 확인 다이얼로그 → API 호출

### 2-3. 찜한 공연 목록 조회 🔗
- **GET** `/api/v1/users/likes`
- Auth: Bearer
- Response: `[{ showId, title, posterUrl, venue, showDate, status }]`
- 📱 **화면**: `WishlistScreen.kt` — 찜 목록 (MyPageViewModel 연결)

### 2-4. 알림 설정 변경 🔗
- **PUT** `/api/v1/users/notifications`
- Auth: Bearer
- Body: `{ notificationEnable: Boolean }`
- Response: `{ httpStatusCode: 200 }`
- 📱 **화면**: `SettingsScreen.kt` — 푸시 알림 받기 토글 (공연 당일/전날 알림)

---

## 3. Show (공연)

### 3-1. 공연 목록 조회 🔗
- **GET** `/api/v1/shows`
- Auth: 불필요
- Query: `region(Int 코드), sort(POPULAR/LATEST/DEADLINE), keyword, page, size`
- **Region 코드 매핑**:

| 코드 | 지역 | 코드 | 지역 | 코드 | 지역 |
|------|------|------|------|------|------|
| 11 | 서울 | 28 | 인천 | 26 | 부산 |
| 27 | 대구 | 29 | 광주 | 30 | 대전 |
| 31 | 울산 | 36 | 세종 | 41 | 경기 |
| 42 | 강원 | 43 | 충북 | 44 | 충남 |
| 45 | 전북 | 46 | 전남 | 47 | 경북 |
| 48 | 경남 | 50 | 제주 | | |

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
      "region": 11,
      "show": { "showStartDate": "2026-06-15", "showEndDate": "2026-06-17" },
      "reservation": { "startDate": "2026-06-01T10:00:00", "endDate": "2026-06-14T23:59:59" },
      "status": "ON_SALE"
    }
  ],
  "page": 0, "size": 20, "totalElements": 135, "totalPages": 7
}
```
- 📱 **화면**: `ShowsScreen.kt` — 공연 목록 (검색, 지역 필터, 정렬), `HomeScreen.kt`

### 3-2. 공연 상세 조회 🔗
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
  "show": { "showStartDate": "2026-06-15", "showEndDate": "2026-06-17" },
  "reservation": { "startDate": "2026-06-01T10:00:00", "endDate": "2026-06-14T23:59:59" },
  "region": 11,
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
- 📱 **화면**: `ShowDetailScreen.kt` — 공연 상세 정보

### 3-3. 공연 회차 목록 조회 🔗
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
- 📱 **화면**: `ShowDateSelectionScreen.kt` — 캘린더 기반 회차 선택 (ShowDateSelectionViewModel 연결)

### 3-4. 좌석 조회 🔗
- **GET** `/api/v1/shows/{showId}/sessions/{sessionId}/seats`
- Auth: Bearer (@SecurityRequirement)
- Response (List 형태, 섹션별):
```json
[
  {
    "sectionId": 1,
    "sectionName": "A구역",
    "gradeName": "VIP",
    "price": 150000,
    "colorCode": "#FF6B6B",
    "seats": [
      {
        "sessionSeatId": 101,
        "seatId": 1,
        "rowNum": 1,
        "colNum": 5,
        "seatNo": "A-1",
        "status": "AVAILABLE"
      }
    ]
  }
]
```
- status: `AVAILABLE` | `HELD` | `PENDING_TX` | `SOLD`
- 📱 **화면**: `SeatMapScreen.kt` — Canvas 기반 줌 가능 좌석 배치도 (SeatMapViewModel 연결)
- 좌석 배치: rowNum/colNum 기반 그리드 레이아웃, 클라이언트에서 section polygon 자동 계산

### 3-5. 공연 찜하기 🔗
- **POST** `/api/v1/shows/{showId}/likes`
- Auth: Bearer (@AuthenticationPrincipal)
- 📱 **화면**: `ShowDetailScreen.kt` — 찜 버튼

### 3-6. 공연 찜 취소 🔗
- **DELETE** `/api/v1/shows/{showId}/likes`
- Auth: Bearer (@AuthenticationPrincipal)
- 📱 **화면**: `ShowDetailScreen.kt`, `WishlistScreen.kt`

### 3-7. 공연장 목록 조회 ✅ ⚠️
- **GET** `/api/v1/shows/venue`
- Auth: 불필요
- Response: `[{ venueId, name, capacity }]`
- ⚠️ frontend에서 미사용

### 3-8. 환불 정책 조회 ✅
- **GET** `/api/v1/shows/{showId}/refund`
- Auth: 불필요
- Response: `{ refundPolicy: [{ daysRemaining, refundRate }], showStartDate }`
- 📱 **화면**: `ShowDetailScreen.kt`, `TicketDetailScreen.kt`

### 3-9. 오픈 예정 공연 조회 🔗
- **GET** `/api/v1/shows/upcoming`
- Auth: 불필요
- Response: `{ shows: [ShowSummaryDto] }` (페이지네이션 없음)
- 📱 **화면**: `HomeScreen.kt` — 오픈 스케줄 섹션

---

## 4. Ticket (티켓)

### 4-1. 티켓 구매 (블록체인 TX) ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/purchase`
- Auth: Bearer
- Body: `{ sessionSeatId: List<Long> }`
- Response: `{ txId: Long }`
- 📱 **화면**: `PaymentScreen.kt`
- ❌ backend에 아직 미구현

### 4-2. 티켓 컬렉션 조회 ❌
- **GET** `/api/v1/tickets/collection`
- Auth: Bearer
- 📱 **화면**: `CollectionScreen.kt`, `ArchiveScreen.kt`
- ❌ backend에 아직 미구현

### 4-3. 좌석 선점 (Redis SETNX) ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/seats`
- Auth: Bearer
- Body: `{ seatId: List<String> }`
- 5분 TTL 좌석 잠금
- ❌ backend에 아직 미구현

### 4-4. 티켓 양도 ❌
- **POST** `/api/v1/tickets/{ticketId}/transfer`
- Auth: Bearer
- Body: `{ phoneNumber: String }`
- 📱 **화면**: `TransferScreen.kt`
- ❌ backend에 아직 미구현

### 4-5. QR 체크인 코드 생성 ❌
- **POST** `/api/v1/tickets/{ticketId}/qr`
- Auth: Bearer
- Response: `{ qrData, expiresAt, ticketId, title, sectionName, seatNo }`
- 30초 OTP
- 📱 **화면**: `QrCheckinScreen.kt`
- ❌ backend에 아직 미구현

### 4-6. 내 예매 티켓 목록 조회 ❌
- **GET** `/api/v1/tickets/upcoming`
- Auth: Bearer
- 📱 **화면**: `MyTicketsScreen.kt`
- ❌ backend에 아직 미구현

### 4-7. 티켓 환불 ✅
- **POST** `/api/v1/tickets/{ticketId}/refund`
- Auth: Bearer (@SecurityRequirement)
- 📱 **화면**: `TicketDetailScreen.kt`

---

## 5. Queue (대기열)

### 5-1. 대기열 진입 ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/queue`
- Auth: Bearer
- ❌ backend에 아직 미구현

### 5-2. 대기 상태 조회 (폴링) ❌
- **GET** `/api/v1/shows/{showId}/sessions/{sessionId}/queue/status`
- Auth: Bearer
- ❌ backend에 아직 미구현

### 5-3. 대기열 이탈 ❌
- **DELETE** `/api/v1/shows/{showId}/sessions/{sessionId}/queue`
- Auth: Bearer
- ❌ backend에 아직 미구현

### 5-4. 좌석 선택 진입 ❌
- **POST** `/api/v1/shows/{showId}/sessions/{sessionId}/queue/enter`
- Auth: Bearer
- ❌ backend에 아직 미구현

---

## 6. Wallet (지갑)

### 6-1. 잔액 조회 🔗
- **GET** `/api/v1/wallets/balance`
- Auth: Bearer
- Response: `{ balance: Int, walletAddress: String }`
- 📱 **화면**: `WalletScreen.kt`, `MyPageScreen.kt` (CTK 잔액 카드)

### 6-2. 잔액 새로고침 (온체인) 🔗
- **GET** `/api/v1/wallets/balance/refresh`
- Auth: Bearer
- Response: `{ balance: Int, walletAddress: String }`
- 📱 **화면**: `MyPageScreen.kt` — 잔액 갱신 시 호출

### 6-3. 거래 내역 조회 🔗
- **GET** `/api/v1/wallets/transactions`
- Auth: Bearer
- Query: `type` (optional, 필터링)
- Response:
```json
[
  {
    "transactionId": 1,
    "type": "PURCHASE",
    "amount": -150000,
    "description": "티켓 구매",
    "sellerId": null,
    "buyerId": 42,
    "createdAt": "2026-06-15T19:00:00"
  }
]
```
- type: `CHARGE` | `PURCHASE` | `RESALE_BUY` | `RESALE_SELL` | `TRANSFER_SEND` | `TRANSFER_RECEIVE` | `REFUND`
- amount: Long (양수=입금, 음수=출금)
- 📱 **화면**: `WalletHistoryScreen.kt` — 거래 내역 목록 (WalletHistoryViewModel 연결, 날짜별 그룹핑)

### 6-4. TX 상태 폴링 ❌
- **GET** `/api/v1/wallets/transactions/{txId}`
- Auth: Bearer
- Response: `{ txId, status, type, txHash, blockNumber, createdAt, updatedAt }`
- ❌ backend에 아직 미구현

---

## 7. Resale (2차거래)

### 7-1. 2차거래 티켓 등록 ✅
- **POST** `/api/v1/resales/{ticketId}`
- Auth: Bearer
- Body: `{ resalePrice: Int }`
- 📱 **화면**: `ResaleCreateScreen.kt`

### 7-2. 2차거래 공연 목록 조회 🔗
- **GET** `/api/v1/resales`
- Auth: 불필요
- Query: `region(Int 코드), sort, keyword, page, size`
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
      "region": 11,
      "posterUrl": "...",
      "ticketCount": 5
    }
  ],
  "page": 0, "size": 20, "totalElements": 50, "totalPages": 3
}
```
- 📱 **화면**: `ResaleScreen.kt` — 2차거래 공연 목록

### 7-3. 특정 공연의 2차거래 티켓 목록 조회 🔗
- **GET** `/api/v1/resales/shows/{showId}`
- Auth: 불필요
- Query: `sort(enum), sessionId, page, size`
- Response:
```json
{
  "tickets": [
    {
      "ticketId": 1,
      "session": { "sessionId": 1, "sessionDate": "2026-06-15", "sessionStartTime": "21:00:00" },
      "sectionName": "가",
      "seatId": 1,
      "seatNo": "A-12",
      "grade": "VIP",
      "originalPrice": 1000000,
      "discountedPrice": 700000,
      "discountRate": 30
    }
  ],
  "page": 0, "size": 20, "totalElements": 135, "totalPages": 7
}
```
- discountRate: 정수 (30 = 30% 할인, `*100` 불필요)
- 📱 **화면**: `ResaleTicketsScreen.kt` (ResaleTicketsViewModel 연결)

### 7-4. 2차거래 티켓 구매 ✅
- **POST** `/api/v1/resales/{ticketId}/purchase`
- Auth: Bearer
- 📱 **화면**: `ResaleDetailScreen.kt` → `ResalePurchaseCompleteScreen.kt`

### 7-5. 2차거래 등록 취소 ✅
- **DELETE** `/api/v1/resales/{ticketId}`
- Auth: Bearer
- 📱 **화면**: `ResaleDetailScreen.kt`

---

## 구현 현황 요약

| 섹션 | 전체 | 🔗 연결 | ✅ BE만 | ❌ 미구현 | ⚠️ FE 미사용 |
|------|------|---------|---------|----------|-------------|
| 1. Auth | 12 | 10 | 2 | 0 | 0 |
| 2. User | 4 | 4 | 0 | 0 | 0 |
| 3. Show | 9 | 7 | 1 | 0 | 1 |
| 4. Ticket | 7 | 0 | 1 | 6 | 0 |
| 5. Queue | 4 | 0 | 0 | 4 | 0 |
| 6. Wallet | 4 | 3 | 0 | 1 | 0 |
| 7. Resale | 5 | 3 | 2 | 0 | 0 |
| **합계** | **45** | **27** | **4** | **11** | **3** |

---

## 화면-API 매핑 요약

| 화면 | 연결 API | 상태 |
|------|---------|------|
| `LoginScreen` | 1-1 로그인 | 🔗 |
| `SignupScreen` | 1-4 회원가입, 1-5 SMS, 1-6 SMS확인, 1-11 이메일중복 | 🔗 |
| `FindAccountScreen` | 1-9 이메일찾기 | 🔗 |
| `PasswordResetScreen` | 1-7 인증코드발송, 1-8 비밀번호재설정 | 🔗 |
| `HomeScreen` | 3-1 공연목록, 3-9 오픈예정 | 🔗 |
| `ShowsScreen` | 3-1 공연목록 (region=Int, sort, keyword 필터) | 🔗 |
| `ShowDetailScreen` | 3-2 공연상세, 3-5/3-6 찜, 3-8 환불정책 | 🔗 |
| `ShowDateSelectionScreen` | 3-3 회차목록 | 🔗 |
| `WaitingQueueScreen` | 5-1~5-4 대기열 | ❌ mock |
| `SeatMapScreen` | 3-4 좌석조회 | 🔗 |
| `PaymentScreen` | 4-1 티켓구매 | ❌ mock (show 정보는 API) |
| `MyTicketsScreen` | 4-6 내 티켓 | ❌ mock |
| `TicketDetailScreen` | 4-7 환불 | ✅ |
| `QrCheckinScreen` | 4-5 QR생성 | ❌ mock |
| `TransferScreen` | 1-10 유저검색, 4-4 양도 | ❌ mock |
| `CollectionScreen` | 4-2 컬렉션 | ❌ mock |
| `ResaleScreen` | 7-2 2차거래 공연목록 | 🔗 |
| `ResaleTicketsScreen` | 7-3 2차거래 티켓목록 | 🔗 |
| `ResaleDetailScreen` | 7-4 구매, 7-5 취소 | ✅ |
| `WalletScreen` | 6-1 잔액 | 🔗 |
| `WalletHistoryScreen` | 6-3 거래내역 | 🔗 |
| `MyPageScreen` | 2-1 내정보, 2-2 회원탈퇴, 2-3 찜목록, 6-1/6-2 잔액 | 🔗 |
| `WishlistScreen` | 2-3 찜목록 | 🔗 |
| `SettingsScreen` | 1-2 로그아웃, 2-4 알림설정 | 🔗 |
| `PasswordChangeScreen` | 1-12 비밀번호변경 | 🔗 |

---

## 네트워크 공통

- **Base URL**: `https://j14d108.p.ssafy.io/`
- **인증**: Bearer token (EncryptedSharedPreferences에 저장)
- **토큰 갱신**: `AuthAuthenticator` — 401 시 자동 reissue
- **네트워크 감시**: `NetworkMonitor.kt` — ConnectivityManager callback Flow
- **오프라인 UI**: `NetworkStatusObserver` — 연결 끊기면 다이얼로그 자동 표시, 복구 시 자동 닫힘

---

## 참고: 주최측(Host) API (Customer App 미사용)

아래 API들은 주최측 전용이며, Customer App에서는 사용하지 않습니다:
- `POST /api/v1/hosts` — 주최자 회원가입
- `POST /api/v1/hosts/auth/login` — 주최자 로그인
- `POST /api/v1/hosts/business-no/duplicate` — 사업자번호 중복 확인
- `GET /api/v1/hosts` — 주최자 정보 조회
- `PATCH /api/v1/hosts` — 주최자 정보 수정
