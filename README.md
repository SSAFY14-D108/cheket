# CHEKET (체켓)

> **"Transparent Tickets, Forever Memories"**
>
> 공연 티켓의 발행 수량, 판매 가격, 정산 내역을 블록체인에 공개 기록하여
> 기획사·플랫폼의 불투명한 운영을 원천 차단하는 NFT 기반 티켓팅 플랫폼

<!-- 📸 서비스 대표 배너 이미지 -->
<!-- ![CHEKET Banner](images/banner.png) -->

---

## 목차

- [기획 배경](#기획-배경)
- [서비스 주요 기능](#서비스-주요-기능)
- [주요 화면 및 기능 소개](#주요-화면-및-기능-소개)
- [프로젝트 핵심 기술](#프로젝트-핵심-기술)
- [시스템 아키텍처](#시스템-아키텍처)
- [팀원 소개](#팀원-소개)
- [기술 스택](#기술-스택)

---

## 기획 배경

### 문제 정의

현재 공연 티켓 시장은 발행부터 정산까지 모든 기록이 기획사·플랫폼의 내부 서버에만 존재합니다.
외부 이해관계자(아티스트, 관객, 감독기관)가 이를 독립적으로 검증할 수단이 없습니다.

| 문제 | 구체적 피해 |
|---|---|
| **발행 수량 불투명** | "전석 매진" 발표 시 실제 발행 수량·초대권 비율 확인 불가 |
| **패키지 가격 은폐** | 티켓+굿즈 묶음 판매 시 티켓 실제 가격을 알 수 없음 → 아티스트 정산 기준 왜곡 |
| **정산 비공개** | 수익 분배 비율·시점을 외부에서 감사할 수단 없음 |
| **플랫폼 횡령 위험** | 판매 대금이 플랫폼에 집중되는 구조에서 정산 전 자금 유용 가능 |
| **리세일 시장 부재** | 공식 리세일 채널이 없어 외부 중고거래 사기 빈번, 가격 통제 불가 |

### 경쟁 서비스 대비 차별점

| 항목 | 기존 플랫폼 | CHEKET |
|---|---|---|
| 발행 수량 검증 | ❌ 기획사 서버에만 존재 | ✅ EventNFT 온체인 공개 |
| 개별 판매 가격 | ❌ 패키지 시 총액만 공개 | ✅ TicketNFT.price 온체인 기록 |
| 정산 투명성 | ❌ 내부 처리, 감사 불가 | ✅ StakeholderNFT 비율 확정 + Settlement 자동 정산 |
| 플랫폼 횡령 방지 | ❌ 플랫폼 지갑에 자금 집중 | ✅ 판매 즉시 컨트랙트 잠금 — 플랫폼 접근 불가 |
| 구매 수량 제한 | ⚠️ 서버 로직 (우회 가능) | ✅ 컨트랙트 강제 (우회 불가) |
| 리세일 가격 통제 | ❌ 없음 | ✅ resaleCapBps 컨트랙트 강제 |
| 안전한 양도 | ❌ 중고거래 사기 위험 | ✅ Escrow 스마트 컨트랙트 |
| 티켓 소장 | ❌ 공연 후 폐기 | ✅ NFT 보관함 영구 전시 |

<!-- 📸 차별점 비교 이미지 -->
<!-- ![차별점](images/comparison.png) -->

---

## 서비스 주요 기능

### 클라이언트 구성 (4개)

| 구분 | host-web | customer-app | scanner-app | customer-collection |
|---|---|---|---|---|
| 플랫폼 | Next.js 16 웹 | Android (Kotlin/Compose) | Android (Kotlin/Compose) | Next.js 16 웹 |
| 대상 | 공연 주최측 | 일반 관객 | 입장 스태프 | 일반 관객 |
| 핵심 기능 | 공연 등록·관리, 판매 대시보드, 정산 확인 | 공연 탐색, 티켓 구매, 리세일, 지정 양도 | QR 스캔 → 입장 체크인 | NFT 보관함 갤러리 |
| 계정 체계 | 사업자등록번호 기반 (이메일/비밀번호 로그인) | SMS 본인인증 가입 (이메일/비밀번호 로그인) | 호스트 계정 공유 | customer-app 계정 연동 |

### 핵심 기능 플로우

**NFT 3단계 발행 전략**
```
① 공연 등록 시     → StakeholderNFT 즉시 발행 (수익 분배 비율 온체인 확정)
② 예매 오픈 D-1   → EventNFT + TicketNFT 배치 발행 (예매 규칙 온체인 고정)
③ 구매 시          → TicketNFT 소유권 이전 (플랫폼 지갑 → 구매자)
```

**수익 분배 구조**
```
1차 판매: 플랫폼 8% 고정 + 나머지 92% 주최측 자유 배분
         → Settlement 컨트랙트가 shareBps 비율대로 자동 분배
         → 플랫폼 8%도 컨트랙트가 계산해서 지급 (조작 불가)

리세일:  판매 대금 전액 → 판매자 (원가 이하 양도만 허용)
```

**기능 요약**

| 기능 | 설명 |
|---|---|
| 공연 등록 | 공연 정보·이해관계자·좌석·가격 등록 → StakeholderNFT 즉시 발행 |
| 티켓 구매 | 대기열(Redis Sorted Set) → 좌석 선점(SET + 300초 TTL) → PurchaseRouter 온체인 구매 |
| 환불 | Settlement.refund()에서 환불 정책에 따라 SSF 반환 + TicketNFT 회수 |
| 리세일 | Escrow에서 등록·구매·취소·만료환불 + 가격 상한 온체인 강제 |
| 지정 양도 | 전화번호로 상대방 검색 → Marketplace로 1:1 무료 직접 양도 |
| QR 체크인 | OTP 동적 QR(30초 갱신) + 온체인 소유권 검증 + DB 중복 확인 3중 검증 |
| 자동 정산 | 공연 다음날 Settlement.finalizeSession() → shareBps 비율대로 자동 분배 |
| NFT 보관함 | 공연 후 TicketNFT 소각 없이 영구 보관 → 갤러리 전시 |
| AI 추천 | Embedding + 아티스트 선호도 + 검색 이력 기반 개인화 추천 |
| 주최측 대시보드 | 회차별 예매율, 입장 현황, 이해관계자별 정산 내역 모니터링 |

---

## 주요 화면 및 기능 소개

### Customer App (일반 관객용 Android)

#### 공연 탐색 및 상세
<!-- 📸 공연 목록 + 상세 화면 스크린샷 -->
<!-- ![공연 탐색](images/customer-show.png) -->

#### 좌석 선택 및 티켓 구매
<!-- 📸 좌석 선택 → 결제 화면 스크린샷 -->
<!-- ![좌석 선택](images/customer-seat.png) -->

<!-- 🎬 티켓 구매 플로우 영상 -->
<!-- ![티켓 구매 데모](videos/purchase-flow.gif) -->

#### 리세일 마켓
<!-- 📸 리세일 등록 + 마켓 화면 스크린샷 -->
<!-- ![리세일](images/customer-resale.png) -->

#### 지정 양도
<!-- 📸 전화번호 입력 → 닉네임 확인 → 양도 완료 -->
<!-- ![지정 양도](images/customer-transfer.png) -->

#### 내 티켓 / QR 체크인
<!-- 📸 내 티켓 목록 + OTP 동적 QR 코드 화면 -->
<!-- ![QR 체크인](images/customer-qr.png) -->

---

### Host Web (주최측용 웹)

#### 공연 등록
<!-- 📸 공연 등록 폼 + 이해관계자·수익 분배 설정 화면 -->
<!-- ![공연 등록](images/host-register.png) -->

#### 판매 대시보드
<!-- 📸 회차별 예매율 + 입장 현황 차트 -->
<!-- ![대시보드](images/host-dashboard.png) -->

#### 정산 확인
<!-- 📸 이해관계자별 정산 내역 패널 -->
<!-- ![정산 현황](images/host-settlement.png) -->

---

### Scanner App (입장 스태프용 Android)

<!-- 📸 QR 스캔 → 입장 승인/거부 결과 화면 -->
<!-- ![QR 스캔](images/scanner.png) -->

<!-- 🎬 QR 체크인 데모 영상 -->
<!-- ![체크인 데모](videos/checkin-demo.gif) -->

---

### Customer Collection (NFT 보관함 웹)

<!-- 📸 NFT 갤러리 화면 (3D 틸트, 파티클 효과) -->
<!-- ![NFT 보관함](images/collection.png) -->

<!-- 🎬 NFT 보관함 인터랙션 영상 -->
<!-- ![보관함 데모](videos/collection-demo.gif) -->

---

## 프로젝트 핵심 기술

### 7-Contract 스마트 컨트랙트 아키텍처

3-Layer Composable NFT와 4개의 거래·정산 컨트랙트로 구성됩니다.

```
━━━ 3-Layer Composable NFT ━━━

StakeholderNFT (ERC-721, Soulbound)
  │  이해관계자별 수익 분배 비율 확정 — 발행 후 양도·변경 불가
  │  _beforeTokenTransfer()에서 일반 전송 차단 (OpenZeppelin 4.9.6)
  ▼
EventNFT (ERC-721, Composable)
  │  StakeholderNFT 참조 + 예매 기간·maxPerWallet·resaleCapBps 온체인 고정
  │  회차(Session) 관리 — 다회차 공연 지원
  ▼
TicketNFT (ERC-721, ERC721URIStorage)
     개별 좌석 — section/row/seat/grade/price/status 온체인 기록
     TicketStatus: VALID → USED / EXPIRED / REFUNDED

━━━ 거래·정산 컨트랙트 ━━━

PurchaseRouter    transferFrom + SSF→Settlement 예치를 1TX로 원자적 처리
Settlement        판매 대금 컨트랙트 잠금, 공연 후 shareBps 비율로 자동 분배
Marketplace       지정 양도 (1:1 무료 직접 전송) + walletTicketCount 갱신
Escrow            리세일 전 과정 자기완결 + on-chain 가격 상한 강제
```

### Custodial 지갑 아키텍처

사용자는 블록체인을 모르는 일반 관객입니다. 기존 티켓팅 앱과 동일한 UX를 유지하면서 블록체인의 투명성을 제공합니다.

| 항목 | 구현 |
|---|---|
| 지갑 생성 | 회원가입 시 서버가 Keystore V3 지갑 자동 생성 + 암호화 보관 |
| 로그인 | 이메일/비밀번호 (지갑 존재를 모름) |
| TX 서명 | 서버가 대리 서명 (사용자 개입 없음) |
| 가스비 | 서버 대납 (gasPrice = 0, SSAFY 네트워크) |
| 초기 SSF | 가입 시 플랫폼 지갑에서 초기 SSF 자동 지급 |
| approve | 서버가 PurchaseRouter·Escrow에 대한 approve 사전 설정 |

### 대기열 시스템

티켓팅 오픈 시 동시 접속 폭주에 대응합니다.

```
접속 → Redis Sorted Set 순번 배정 (WAITING)
     → 순번 도달 시 ACTIVE 전환 → 구매 화면 진입
     → 좌석 선택 시 SEAT_SELECTION → SET + 300초 TTL로 좌석 잠금
     → 결제 완료 시 COMPLETED / 미결제 시 EXPIRED → 다음 대기자 진입
```

### 비동기 TX 파이프라인

블록체인 TX 확정을 기다리지 않고 즉시 응답을 반환합니다.

```
요청 → DB에 PENDING 기록 → API 즉시 응답 (~50ms)
     → @Async TX Worker: Nonce 할당(ReentrantLock) → Keystore V3 서명 → 전송 (SUBMITTED)
     → 블록 확정 감지 → CONFIRMED + Push 알림
     → 실패 시 → FAILED + 오류 안내
```

### QR 체크인 — 하이브리드 방식

```
실시간 입장 (오프체인, ~300ms):
  QR 스캔 → ① OTP 유효성 검증 (Redis TTL 30초)
          → ② 온체인 소유권 확인 (ownerOf, view call)
          → ③ DB 중복 입장 확인 (checked_in_at)
          → 즉시 입장 승인

공연 후 (온체인 배치 동기화):
  스케줄러 → 체크인된 티켓 batchCheckIn() → TicketStatus.USED 온체인 기록
```

### 성능 최적화

| 최적화 | Before | After | 개선 |
|---|---|---|---|
| TicketNFT 배치 발행 (5,000석) | 5,000 TX | 50 TX (100개씩) | **TX 수 99% 감소** |
| 구매 API 응답시간 | ~5,000ms (동기 대기) | ~50ms (비동기) | **응답시간 100배 단축** |
| 구매당 TX 수 | 2 TX (transfer + deposit) | 1 TX (PurchaseRouter) | **TX 50% 감소** |

### 컨트랙트 강제 규칙

서버 로직이 아닌 스마트 컨트랙트 코드로 규칙을 강제하여 우회를 불가능하게 합니다.

| 규칙 | 컨트랙트 | 메커니즘 |
|---|---|---|
| 구매 수량 제한 | EventNFT | `maxPerWallet` + `walletTicketCount` 온체인 검증 |
| 리세일 가격 상한 | Escrow | `TicketNFT.getPrice()` × `resaleCapBps` on-chain 검증 |
| 예매 기간 통제 | EventNFT | `isBookingOpen()` — `block.timestamp` 비교 |
| 분배 비율 고정 | StakeholderNFT | Soulbound — 발행 후 변경·양도 불가 |
| 자금 횡령 방지 | Settlement | 판매 즉시 컨트랙트 잠금, `finalizeSession()`만 분배 가능 |
| 거래 원자성 | PurchaseRouter / Escrow | 1 TX 처리, 부분 실패 시 전체 revert |

### 온체인 / 오프체인 데이터 분리

| 온체인 (불변·공개) | IPFS (분산·영구) | 오프체인 서버 DB |
|---|---|---|
| 티켓 소유권·거래 이력 | 공연 포스터 이미지 | 사용자 프로필 |
| 발행 수량 (totalSupply) | 티켓 디자인 이미지 | 공연 검색·필터·정렬 |
| 이해관계자 분배 비율 (shareBps) | 메타데이터 JSON | 좌석 배치도 레이아웃 |
| 판매 대금 예치 (Settlement) | | 대기열·좌석 Lock |
| 구매 제한·리세일 상한 | | TX 상태 추적 (폴링) |
| 체크인 기록 (배치 동기화) | | 알림·Push 설정 |

---

## 시스템 아키텍처

### 서비스 아키텍처

```
┌──────────────────────────────────────────────────────────────────┐
│                     프론트엔드 (4개 클라이언트)                      │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌──────────────┐  │
│  │ host-web  │  │ customer- │  │ scanner-  │  │ customer-    │  │
│  │ (Next.js) │  │ app       │  │ app       │  │ collection   │  │
│  │ 공연 등록 │  │ (Android) │  │ (Android) │  │ (Next.js)    │  │
│  │ 대시보드  │  │ 티켓 구매 │  │ QR 체크인 │  │ NFT 보관함   │  │
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └──────┬───────┘  │
└────────┼──────────────┼──────────────┼────────────────┼──────────┘
         │          REST API (JWT)     │                │
┌────────▼──────────────▼──────────────▼────────────────▼──────────┐
│                    Spring Boot Backend                            │
│                                                                   │
│  ┌────────────┐  ┌────────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Domain API │  │ TX Worker  │  │ Queue    │  │ AI 추천      │  │
│  │ (REST)     │  │ (@Async)   │  │ Service  │  │ (FastAPI)    │  │
│  └────────────┘  └────────────┘  └──────────┘  └──────────────┘  │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │  Custodial 서버 지갑 (Web3j + Keystore V3)                 │   │
│  │  ReentrantLock Nonce 관리 · TX Receipt 추적                │   │
│  └────────────────────────────────────────────────────────────┘   │
└──────┬──────────────────┬──────────────────────┬─────────────────┘
       │                  │                      │
┌──────▼───────┐  ┌───────▼───────────────────┐  │
│ MySQL + Redis │  │ SSAFY Ethereum Network    │  │
│              │  │                           │  │
│ 오프체인 DB  │  │ StakeholderNFT (Soulbound)│  │
│ 좌석 Lock    │  │ EventNFT (Composable)     │  │
│ 대기열       │  │ TicketNFT (ERC-721)       │  │
│ TX 상태 추적 │  │ PurchaseRouter            │  │
│              │  │ Settlement                │  │
└──────────────┘  │ Marketplace               │  │
                  │ Escrow                    │  │
                  │ SSF (ERC-20)              │  │
                  └───────────────────────────┘  │
                                                 │
                            ┌────────────────────▼──┐
                            │ IPFS (Pinata) + AWS S3 │
                            │ 포스터·메타데이터 저장  │
                            └────────────────────────┘
```

<!-- 📸 서비스 아키텍처 다이어그램 (고해상도 이미지가 있으면 대체) -->
<!-- ![서비스 아키텍처](images/service-architecture.png) -->

### CI/CD 아키텍처

```
┌─────────┐     ┌──────────┐     ┌──────────────┐     ┌─────────────┐
│  GitLab  │────▶│ Jenkins  │────▶│ Docker Build │────▶│ EC2 서버    │
│  Push    │     │ Pipeline │     │ & Compose    │     │ 배포 완료   │
└─────────┘     └──────────┘     └──────────────┘     └─────────────┘
                     │
           ┌─────────┼─────────┐
           ▼         ▼         ▼
      Build &    Docker     Deploy
      Test      Image 생성  docker-compose up
```

| 단계 | 도구 | 설명 |
|---|---|---|
| 소스 관리 | GitLab | 브랜치 전략 (fe/*, be/*) |
| CI/CD | Jenkins | Push 트리거 → 빌드 → 배포 자동화 |
| 컨테이너 | Docker Compose | backend + MySQL + Redis 멀티 서비스 |
| 리버스 프록시 | Nginx | HTTPS 종단, 프론트/백엔드 라우팅 |
| 블록체인 | Hardhat | 스마트 컨트랙트 컴파일·배포 (SSAFY 네트워크) |

<!-- 📸 CI/CD 파이프라인 다이어그램 -->
<!-- ![CI/CD](images/cicd.png) -->

### 프로젝트 구조

```
S14P21D108/
├── frontend/                           # 프론트엔드 모노레포
│   └── apps/
│       ├── host-web/                   # 주최측 웹 (Next.js 16)
│       ├── customer-app/
│       │   └── android/                # 고객 모바일앱 (Kotlin 2.0.21 / Jetpack Compose)
│       ├── customer-collection/        # NFT 보관함 웹 (Next.js 16)
│       └── scanner-app/               # QR 스캐너 앱 (Android / ML Kit Vision)
├── backend/                            # Spring Boot 3.5.11 API 서버
│   └── src/main/java/com/ssafy/cheket/
│       ├── domain/                     # 도메인별 Controller / Service / Repository
│       ├── blockchain/                 # Web3j 컨트랙트 래퍼 (7개 컨트랙트)
│       └── config/                     # Spring / Web3j / Redis 설정
├── blockchain/                         # Hardhat 프로젝트
│   └── contracts/                      # Solidity 스마트 컨트랙트 (7개)
├── ai-server/                          # FastAPI 추천 서비스
│   └── app/                            # Embedding + 스코어링 엔진
├── docker-compose.yml                  # 프로덕션 배포 (backend + MySQL + Redis)
└── exec/
    └── 포팅_매뉴얼.md
```

---

## 팀원 소개

<table>
  <tr>
    <td align="center" width="16%">
      <img src="" width="120" height="150" alt="팀원1"/><br/>
      <b>이름</b><br/>
      <sub>역할 (예: Backend & Leader)</sub>
    </td>
    <td align="center" width="16%">
      <img src="" width="120" height="150" alt="팀원2"/><br/>
      <b>이름</b><br/>
      <sub>역할</sub>
    </td>
    <td align="center" width="16%">
      <img src="" width="120" height="150" alt="팀원3"/><br/>
      <b>이름</b><br/>
      <sub>역할</sub>
    </td>
    <td align="center" width="16%">
      <img src="" width="120" height="150" alt="팀원4"/><br/>
      <b>이름</b><br/>
      <sub>역할</sub>
    </td>
    <td align="center" width="16%">
      <img src="" width="120" height="150" alt="팀원5"/><br/>
      <b>이름</b><br/>
      <sub>역할</sub>
    </td>
    <td align="center" width="16%">
      <img src="" width="120" height="150" alt="팀원6"/><br/>
      <b>이름</b><br/>
      <sub>역할</sub>
    </td>
  </tr>
  <tr>
    <td>
      - 담당 업무 1<br/>
      - 담당 업무 2<br/>
      - 담당 업무 3
    </td>
    <td>
      - 담당 업무 1<br/>
      - 담당 업무 2<br/>
      - 담당 업무 3
    </td>
    <td>
      - 담당 업무 1<br/>
      - 담당 업무 2<br/>
      - 담당 업무 3
    </td>
    <td>
      - 담당 업무 1<br/>
      - 담당 업무 2<br/>
      - 담당 업무 3
    </td>
    <td>
      - 담당 업무 1<br/>
      - 담당 업무 2<br/>
      - 담당 업무 3
    </td>
    <td>
      - 담당 업무 1<br/>
      - 담당 업무 2<br/>
      - 담당 업무 3
    </td>
  </tr>
</table>

---

## 기술 스택

| 영역 | 기술 |
|---|---|
| Android 앱 | Kotlin 2.0.21, Jetpack Compose (BOM 2024.09.00), Retrofit, ML Kit Vision (QR) |
| 웹 프론트엔드 | Next.js 16, React 19, TypeScript, Tailwind CSS 4, Radix UI |
| 백엔드 | Spring Boot 3.5.11, Java 17, Spring Data JPA, Spring Security |
| 블록체인 연동 | Web3j 4.12.3, Keystore V3, ReentrantLock Nonce 관리 |
| 스마트 컨트랙트 | Solidity 0.8.28, Hardhat 2.28.6, OpenZeppelin 4.9.6 |
| 블록체인 네트워크 | SSAFY Ethereum (Hyperledger Besu, PoA), SSF (ERC-20) |
| 블록 익스플로러 | Blockscout (컨트랙트 Verify + 온체인 데이터 조회) |
| DB | MySQL 8.0.43, Redis 7.4 (대기열 Sorted Set + 좌석 Lock) |
| AI 추천 | FastAPI, OpenAI text-embedding-3-large |
| 파일 저장 | AWS S3 (CDN 서빙) + IPFS / Pinata (영구 분산 저장) |
| 인증 | JWT Access/Refresh, SMS 본인인증 (Coolsms), Firebase Push |
| 배포 | Docker Compose, Jenkins CI/CD, Nginx |

<!-- 📸 기술 스택 아이콘 배지 -->
<!-- ![기술 스택](images/tech-stack.png) -->
