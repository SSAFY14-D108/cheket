// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title EventNFT (Composable NFT)
 * @notice 공연/이벤트를 나타내는 NFT. StakeholderNFT들을 참조(compose)한다.
 *
 * [구조]
 * EventNFT
 *   ├── StakeholderNFT #0 (주최자 70%)
 *   ├── StakeholderNFT #1 (아티스트 20%)
 *   ├── StakeholderNFT #2 (장소 5%)
 *   └── StakeholderNFT #3 (플랫폼 5%)
 *
 * [Session]
 * 하나의 이벤트에 여러 회차 가능 (다회차 공연)
 */
contract EventNFT is ERC721, Ownable {
    // ========== 상태 변수 ==========

    uint256 private _nextEventId;
    uint256 private _nextSessionId;

    struct EventInfo {
        uint256[] stakeholderTokenIds;  // 연결된 StakeholderNFT ID 목록
        string metadataCID;             // IPFS 메타데이터 CID
        uint256 totalSupply;            // 총 티켓 수
        uint256 maxPerWallet;           // 1인당 최대 구매 수
        uint256 resaleCapBps;           // 재판매 가격 상한 (10000 = 원가 100%)
        uint256 bookingStartTime;       // 예매 시작 시각
        uint256 bookingEndTime;         // 예매 종료 시각
        bool isActive;                  // 활성 상태
        uint256 createdAt;              // 생성 시각
    }

    struct Session {
        uint256 eventId;                // 이벤트 ID
        uint256 sessionTimestamp;       // 공연 일시
        uint256 ticketSupply;           // 해당 회차 티켓 수
        bool isFinalized;              // 정산 완료 여부
    }

    // eventId => EventInfo
    mapping(uint256 => EventInfo) public events;

    // sessionId => Session
    mapping(uint256 => Session) public sessions;

    // eventId => sessionId[]
    mapping(uint256 => uint256[]) public eventSessions;

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

    // ========== 생성자 ==========

    constructor() ERC721("CHEKET Event", "CEVT") Ownable(msg.sender) {}

    // ========== 이벤트 생성 ==========

    /**
     * @notice 새 이벤트 생성
     * @param stakeholderTokenIds 이해관계자 StakeholderNFT ID 배열
     * @param metadataCID IPFS 메타데이터 CID
     * @param totalSupply 총 티켓 수
     * @param maxPerWallet 1인당 최대 구매 수
     * @param resaleCapBps 재판매 가격 상한 (10000 = 100%)
     * @param bookingStartTime 예매 시작 시각 (unix timestamp)
     * @param bookingEndTime 예매 종료 시각 (unix timestamp)
     * @return eventId 생성된 이벤트 ID
     */
    function createEvent(
        uint256[] calldata stakeholderTokenIds,
        string calldata metadataCID,
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

        uint256 eventId = _nextEventId++;
        _safeMint(msg.sender, eventId);

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

        emit EventCreated(eventId, metadataCID, totalSupply, maxPerWallet);
        return eventId;
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

        eventSessions[eventId].push(sessionId);

        emit SessionAdded(eventId, sessionId, sessionTimestamp, ticketSupply);
        return sessionId;
    }

    // ========== 상태 변경 ==========

    /**
     * @notice 회차 정산 완료 처리
     */
    function finalizeSession(uint256 sessionId) external onlyOwner {
        require(!sessions[sessionId].isFinalized, "Already finalized");
        sessions[sessionId].isFinalized = true;
    }

    /**
     * @notice 이벤트 비활성화
     */
    function deactivateEvent(uint256 eventId) external onlyOwner {
        events[eventId].isActive = false;
    }

    // ========== 조회 함수 ==========

    /**
     * @notice 예매 가능 여부 확인
     */
    function isBookingOpen(uint256 eventId) external view returns (bool) {
        EventInfo storage e = events[eventId];
        return e.isActive
            && block.timestamp >= e.bookingStartTime
            && block.timestamp <= e.bookingEndTime;
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
        string memory metadataCID,
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

    function totalEvents() external view returns (uint256) {
        return _nextEventId;
    }

    function totalSessions() external view returns (uint256) {
        return _nextSessionId;
    }
}
