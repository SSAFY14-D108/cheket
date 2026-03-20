// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title EventNFT (Composable NFT)
 * @notice 공연/이벤트를 나타내는 NFT. StakeholderNFT들을 참조(compose)한다.
 *
 * [역할]
 * - 공연 자체를 NFT로 표현
 * - StakeholderNFT ID들을 배열로 참조 → "이 공연의 수익은 이 stakeholder들에게"
 * - 회차(Session) 관리 (다회차 공연)
 * - 예매 규칙을 온체인에 확정 (예매 기간, 구매 한도, 리세일 상한)
 * - 환불 정책을 온체인에 확정 (D-7: 100%, D-3: 50%, D-1: 0%)
 *
 * [Composable이란?]
 * EventNFT가 StakeholderNFT ID들을 "합성(compose)"하여 연결
 * → NFT끼리 참조 관계를 가지는 구조
 *
 * [발행 시점]
 * 예매 오픈 D-1 (2단계)
 * → 이 시점에 예매 규칙 + 환불 정책이 온체인에 확정됨
 * → MINTED 이후에는 예매 기간, 구매 한도, 리세일 상한, 환불 정책 변경 불가
 *
 * [IPFS CID란?]
 * IPFS = 분산 파일 시스템 (서버 없이 파일 저장/공유)
 * CID = Content ID (파일의 해시값 = 파일 주소)
 * 예: "QmXyz..." → 이 해시로 파일을 다운로드
 * 블록체인에 이미지 직접 저장 → 가스비 폭탄
 * IPFS에 이미지 저장 → CID만 블록체인에 기록 (저렴)
 *
 * [구조 예시]
 * EventNFT #0: "아이유 콘서트"
 *   ├── stakeholderTokenIds: [0, 1, 2]
 *   │     └── StakeholderNFT #0=주최자, #1=아티스트, #2=플랫폼
 *   ├── metadataCID: "QmXyz..." (IPFS 포스터/설명)
 *   ├── totalSupply: 500 (총 좌석)
 *   ├── maxPerWallet: 4 (1인당 최대)
 *   ├── resaleCapBps: 10000 (원가 100%까지 리세일 허용)
 *   ├── refundPolicies: [{7, 10000}, {3, 5000}, {1, 0}]
 *   │     └── D-7: 100%, D-3: 50%, D-1: 환불불가
 *   ├── Session #0: 1회차 (3/15 19:00, 250석)
 *   └── Session #1: 2회차 (3/16 19:00, 250석)
 */
contract EventNFT is ERC721, Ownable {

    // ========== 상태 변수 ==========

    uint256 private _nextEventId;    // 다음 이벤트 ID
    uint256 private _nextSessionId;  // 다음 회차 ID

    struct EventInfo {
        uint256[] stakeholderTokenIds;  // 연결된 StakeholderNFT ID 배열
        string metadataCID;             // IPFS 메타데이터 CID
        uint256 totalSupply;            // 총 티켓 수
        uint256 maxPerWallet;           // 1인당 최대 구매 수
        uint256 resaleCapBps;           // 재판매 가격 상한 (10000 = 원가 100%)
        uint256 bookingStartTime;       // 예매 시작 시각
        uint256 bookingEndTime;         // 예매 종료 시각
        bool isActive;                  // 활성 상태
        uint256 createdAt;              // 생성 시각
    }

    // 회차 정보
    // 하나의 공연에 여러 회차 가능 (1회차, 2회차...)
    struct Session {
        uint256 eventId;               // 이벤트 ID
        uint256 sessionTimestamp;      // 공연 일시
        uint256 ticketSupply;          // 해당 회차 티켓 수
        bool isFinalized;             // 정산 완료 여부
    }

    /**
     * 환불 규칙 구조체
     * 공연까지 남은 일수에 따라 환불률이 달라짐
     *
     * 예시:
     *   { daysRemaining: 7, refundRateBps: 10000 }  → D-7 이상: 100% 환불
     *   { daysRemaining: 3, refundRateBps: 5000 }   → D-3 이상: 50% 환불
     *   { daysRemaining: 1, refundRateBps: 0 }      → D-1 이하: 환불 불가
     *
     * daysRemaining = "공연까지 최소 이만큼 남아야 이 환불률 적용"
     * refundRateBps = 환불 비율 (10000 = 100%, 5000 = 50%, 0 = 환불 불가)
     *
     * [왜 on-chain에 저장?]
     * 백엔드가 환불액을 계산하면 → 조작 가능
     * 컨트랙트가 on-chain에서 직접 계산하면 → 조작 불가 (투명성)
     * → Settlement.refund()가 이 정책을 읽어서 환불액을 자동 계산
     */
    struct RefundRule {
        uint256 daysRemaining;      // 공연까지 남은 최소 일수
        uint256 refundRateBps;      // 환불 비율 (10000 = 100%)
    }

    // eventId → 이벤트 정보
    mapping(uint256 => EventInfo) public events;
    // sessionId → 회차 정보
    mapping(uint256 => Session) public sessions;
    // eventId → 해당 이벤트의 회차 ID 배열
    mapping(uint256 => uint256[]) public eventSessions;
    // eventId → 환불 정책 배열 (내림차순: 7일, 3일, 1일...)
    mapping(uint256 => RefundRule[]) public refundPolicies;

    // ========== 이벤트 ==========

    event EventCreated(
        uint256 indexed eventId,
        string metadataCID,
        uint256 totalSupply,
        uint256 maxPerWallet
    );

    event SessionAdded(
        uint256 indexed eventId,
        uint256 indexed sessionId,
        uint256 sessionTimestamp,
        uint256 ticketSupply
    );

    // 환불 정책 설정 시 발생
    event RefundPolicySet(
        uint256 indexed eventId,
        uint256 rulesCount
    );

    // ========== 생성자 ==========
    // constructor = 컨트랙트 배포 시 1번만 실행되는 초기화 함수
    // ERC721("이름", "심볼") → NFT 컬렉션 이름과 티커 설정
    // Ownable(msg.sender) → 배포한 사람(= 플랫폼 지갑)이 owner

    constructor() ERC721("CHEKET Event", "CEVT") {}

    // ========== 이벤트 생성 ==========

    /**
     * external = 외부에서만 호출 가능 (가스비 절약)
     * onlyOwner = 플랫폼 서버(owner)만 호출 가능
     * 
     * @notice 새 이벤트 생성
     * @param stakeholderTokenIds 이해관계자 StakeholderNFT ID 배열
     * → StakeholderNFT #0, #1, #2를 이 공연에 연결
     * @param metadataCID IPFS 메타데이터 CID
     * @param totalSupply 총 티켓 수
     * @param maxPerWallet 1인당 최대 구매 수
     * @param resaleCapBps 재판매 가격 상한 (10000 = 100%)
     * @param bookingStartTime 예매 시작 시각 (unix timestamp)
     * @param bookingEndTime 예매 종료 시각 (unix timestamp)
     * @return eventId 생성된 이벤트 ID
     */
    function createEvent(
        uint256[] calldata stakeholderTokenIds, // 참조값이라서 calldata, 나머지는 스택
        string calldata metadataCID,            // 참조값이라서 calldata, 나머지는 스택
        uint256 totalSupply,
        uint256 maxPerWallet,
        uint256 resaleCapBps,
        uint256 bookingStartTime,
        uint256 bookingEndTime
    ) external onlyOwner returns (uint256) {
        require(stakeholderTokenIds.length > 0, "No stakeholders");
        require(totalSupply > 0, "Invalid totalSupply");
        require(maxPerWallet > 0, "Invalid maxPerWallet");
        require(resaleCapBps <= 10000, "Invalid resaleCapBps");
        require(bookingStartTime < bookingEndTime, "Invalid booking time");

        // ---- storage 쓰기 (가장 비쌈) ----
        //  ① storage에서 _nextEventId 읽음 (예: 0)
        //  ② eventId에 0 대입 (스택)
        //  ③ storage의 _nextEventId를 1로 업데이트
        uint256 eventId = _nextEventId++;
        _safeMint(msg.sender, eventId); // EventNFT 발행 (owner 소유)
        //  내부에서 storage 쓰기 발생:
        //  _owners[tokenId] = to     (NFT 소유자 기록)
        //  _balances[to] += 1        (보유 수 증가)

        // ---- calldata → storage 저장 ----
        events[eventId] = EventInfo({
            stakeholderTokenIds: stakeholderTokenIds,
            metadataCID: metadataCID,
            totalSupply: totalSupply,
            maxPerWallet: maxPerWallet,
            resaleCapBps: resaleCapBps,
            bookingStartTime: bookingStartTime,
            bookingEndTime: bookingEndTime,
            isActive: true,
            createdAt: block.timestamp
        });

        // ---- calldata에서 읽어서 로그 기록 ----
        //  이벤트 로그에 기록됨 (storage보다 저렴)
        // emit: "블록체인에 로그를 남겨라"(영수증): 해당 로그는 백엔드 Event Listener가 읽은 후 DB 동기화
        emit EventCreated(eventId, metadataCID, totalSupply, maxPerWallet);

        return eventId; // 스택의 값 반환
    }

    // ========== 회차 추가 ==========

    /**
     * @notice 이벤트에 회차 추가
     * @param eventId 이벤트 ID
     * @param sessionTimestamp 공연 일시 (unix timestamp)
     * @param ticketSupply 해당 회차 티켓 수
     * @return sessionId 생성된 회차 ID
     */
    function addSession(
        uint256 eventId,
        uint256 sessionTimestamp,
        uint256 ticketSupply
    ) external onlyOwner returns (uint256) {
        require(events[eventId].isActive, "Event not active");
        require(ticketSupply > 0, "Invalid ticketSupply");

        uint256 sessionId = _nextSessionId++;

        sessions[sessionId] = Session({
            eventId: eventId,
            sessionTimestamp: sessionTimestamp,
            ticketSupply: ticketSupply,
            isFinalized: false
        });

        // 이벤트에 회차 ID 추가 (배열에 push)
        eventSessions[eventId].push(sessionId);

        // ---- calldata에서 읽어서 로그 기록 ----
        //  이벤트 로그에 기록됨 (storage보다 저렴)
        // emit: "블록체인에 로그를 남겨라"(영수증): 해당 로그는 백엔드 Event Listener가 읽은 후 DB 동기화
        emit SessionAdded(eventId, sessionId, sessionTimestamp, ticketSupply);

        return sessionId;
    }

    // ========== 환불 정책 설정 ==========

    /**
     * @notice 이벤트에 환불 정책 설정 (createEvent 후 호출)
     *
     * [사용 시점]
     * 공연 등록 시 (1단계) createEvent() 호출 후
     * setRefundPolicy()로 환불 규칙을 on-chain에 등록
     * → MINTED 이후에는 환불 정책도 변경 불가 (투명성)
     *
     * [규칙]
     * daysRemaining 내림차순으로 입력 (7, 3, 1 순서)
     * → getRefundRate()에서 위에서부터 순서대로 검사
     * → 첫 번째로 조건에 맞는 규칙이 적용됨
     *
     * [예시 호출]
     * setRefundPolicy(0, [7, 3, 1], [10000, 5000, 0])
     * → eventId 0에 대해:
     *   7일 이상 남음 → 100% 환불
     *   3일 이상 남음 → 50% 환불
     *   1일 이상 남음 → 0% (환불 불가)
     *
     * [왜 on-chain?]
     * 백엔드가 환불률을 결정하면 → 조작 가능
     * on-chain에 확정하면 → 조작 불가
     * → Settlement.refund()가 이 값을 직접 읽어서 계산
     * → 누구나 getRefundPolicies()로 환불 정책 확인 가능 (투명성)
     *
     * @param eventId 이벤트 ID
     * @param daysArray 남은 일수 배열 (내림차순: 7, 3, 1)
     * @param rateBpsArray 환불 비율 배열 (대응: 10000, 5000, 0)
     */
    function setRefundPolicy(
        uint256 eventId,
        uint256[] calldata daysArray,
        uint256[] calldata rateBpsArray
    ) external onlyOwner {
        require(events[eventId].isActive, "Event not active");
        require(daysArray.length > 0, "Empty policy");
        require(daysArray.length == rateBpsArray.length, "Array length mismatch");

        // 기존 정책 삭제 후 새로 설정
        // delete = storage 배열을 비움 (길이 0으로 초기화)
        delete refundPolicies[eventId];

        for (uint256 i = 0; i < daysArray.length; i++) {
            require(rateBpsArray[i] <= 10000, "Invalid refund rate");

            // 내림차순 검증 (7 > 3 > 1)
            // 첫 번째 원소(i=0)는 비교 대상 없으므로 건너뜀
            if (i > 0) {
                require(daysArray[i] < daysArray[i - 1], "Must be descending order");
            }

            // storage 배열에 push (영구 저장)
            refundPolicies[eventId].push(RefundRule({
                daysRemaining: daysArray[i],
                refundRateBps: rateBpsArray[i]
            }));
        }

        emit RefundPolicySet(eventId, daysArray.length);
    }

    // ========== 상태 변경 ==========

    /**
     * @notice 회차 정산 완료 표시
     * @dev 정산 후 호출. isFinalized=true가 되면 다시 정산 불가 (이중 정산 방지)
     */
    function finalizeSession(uint256 sessionId) external onlyOwner {
        require(!sessions[sessionId].isFinalized, "Already finalized");
        sessions[sessionId].isFinalized = true;
    }

    /**
     * @notice 이벤트 비활성화 (공연 취소 등)
     */
    function deactivateEvent(uint256 eventId) external onlyOwner {
        events[eventId].isActive = false;
    }

    // ========== 조회 함수 ==========
    // view = 블록체인 상태를 읽기만 함 (가스비 0, 무료 호출)
    // storage = 블록체인에 영구 저장된 데이터를 직접 참조 (복사 안 함)

    /**
     * @notice 현재 시각이 예매 가능 시간인지 확인
     * block.timestamp = 현재 블록이 생성된 시각 (unix timestamp)
     */
    function isBookingOpen(uint256 eventId) external view returns (bool) {
        EventInfo storage e = events[eventId];
        // storage 키워드 = 복사 안 함, 블록체인 데이터를 직접 가리킴
        return e.isActive
            && block.timestamp >= e.bookingStartTime
            && block.timestamp <= e.bookingEndTime;
    }

    /**
     * @notice 현재 시점에서의 환불 비율 조회
     *
     * [동작 원리]
     * 공연 시각 - 현재 시각 = 남은 초
     * 남은 초 / 86400 = 남은 일수 (86400 = 60초 × 60분 × 24시간)
     * 환불 정책을 위에서부터 순서대로 검사 (내림차순이니까 가장 관대한 것부터)
     * 첫 번째로 "남은 일수 >= daysRemaining"인 규칙 적용
     *
     * [예시]
     * 정책: [{7, 10000}, {3, 5000}, {1, 0}]
     *
     * 남은 일수 = 10일
     * → 10 >= 7? YES → 10000 (100%) 반환
     *
     * 남은 일수 = 5일
     * → 5 >= 7? NO
     * → 5 >= 3? YES → 5000 (50%) 반환
     *
     * 남은 일수 = 0일
     * → 0 >= 7? NO → 0 >= 3? NO → 0 >= 1? NO
     * → 전부 불만족 → 0 반환 (환불 불가)
     *
     * [누가 이 함수를 호출?]
     * Settlement.refund()가 호출
     * → 환불액 = ticketPrice × getRefundRate() / 10000
     * → 백엔드가 환불액을 조작할 수 없음 (on-chain 계산)
     * → 누구나 이 함수를 호출해서 현재 환불률 확인 가능 (투명성)
     *
     * @param eventId 이벤트 ID
     * @param sessionTimestamp 해당 회차의 공연 시각 (unix timestamp)
     * @return refundRateBps 환불 비율 (10000=100%, 5000=50%, 0=환불불가)
     */
    function getRefundRate(
        uint256 eventId,
        uint256 sessionTimestamp
    ) external view returns (uint256) {
        // 공연 시각이 이미 지났으면 환불 불가
        if (block.timestamp >= sessionTimestamp) {
            return 0;
        }

        // 남은 초 → 남은 일수 계산
        uint256 secondsRemaining = sessionTimestamp - block.timestamp;
        uint256 daysLeft = secondsRemaining / 86400;
        // 86400 = 1일 (초 단위)

        // 정책을 위에서부터 순서대로 검사
        RefundRule[] storage rules = refundPolicies[eventId];
        // storage = 블록체인에서 직접 참조 (복사 안 함)

        for (uint256 i = 0; i < rules.length; i++) {
            if (daysLeft >= rules[i].daysRemaining) {
                return rules[i].refundRateBps;
                // 첫 번째로 조건 만족하는 규칙 반환 후 함수 종료
            }
        }

        // 어떤 규칙도 만족 못하면 환불 불가
        return 0;
    }

    /**
     * @notice 이벤트의 이해관계자 토큰 ID 목록 조회
     */
    function getStakeholderTokenIds(uint256 eventId) external view returns (uint256[] memory) {
        return events[eventId].stakeholderTokenIds;
    }

    /**
     * @notice 이벤트의 회차 ID 목록 조회
     */
    function getSessionIds(uint256 eventId) external view returns (uint256[] memory) {
        return eventSessions[eventId];
    }

    /**
     * @notice 이벤트 정보 조회
     */
    function getEventInfo(uint256 eventId) external view returns (
        string memory metadataCID, // 참조 타입 → memory (storage에서 복사해서 반환)
        uint256 totalSupply,
        uint256 maxPerWallet,
        uint256 resaleCapBps,
        uint256 bookingStartTime,
        uint256 bookingEndTime,
        bool isActive
    ) {
        EventInfo storage e = events[eventId];
        return (
            e.metadataCID,
            e.totalSupply,
            e.maxPerWallet,
            e.resaleCapBps,
            e.bookingStartTime,
            e.bookingEndTime,
            e.isActive
        );
        //  [왜 memory로 반환?]
        //  이건 TX에서 들어온 데이터가 아님 (calldata ❌)
        //  블록체인에서 꺼낸 데이터임 → memory에 복사해서 반환
    }

    /**
     * @notice 회차 정보 조회
     */
    function getSession(uint256 sessionId) external view returns (
        uint256 eventId,
        uint256 sessionTimestamp,
        uint256 ticketSupply,
        bool isFinalized
    ) {
        Session storage s = sessions[sessionId];
        return (s.eventId, s.sessionTimestamp, s.ticketSupply, s.isFinalized);
    }

    /**
     * @notice 환불 정책 전체 조회
     * 누구나 호출 가능 → 환불 정책도 투명하게 공개
     *
     * 반환 예시:
     *   daysArray = [7, 3, 1]
     *   rateBpsArray = [10000, 5000, 0]
     *   → "7일 전 100%, 3일 전 50%, 1일 전 환불불가"
     */
    function getRefundPolicies(uint256 eventId) external view returns (
        uint256[] memory daysArray,
        uint256[] memory rateBpsArray
    ) {
        RefundRule[] storage rules = refundPolicies[eventId];
        // memory 배열 생성 (반환용 임시 배열)
        // new uint256[](길이) = 길이만큼의 빈 배열을 memory에 생성
        daysArray = new uint256[](rules.length);
        rateBpsArray = new uint256[](rules.length);

        for (uint256 i = 0; i < rules.length; i++) {
            daysArray[i] = rules[i].daysRemaining;
            rateBpsArray[i] = rules[i].refundRateBps;
        }

        return (daysArray, rateBpsArray);
    }

    function totalEvents() external view returns (uint256) {
        return _nextEventId;
    }

    function totalSessions() external view returns (uint256) {
        return _nextSessionId;
    }
}
