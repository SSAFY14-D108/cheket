// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title TicketNFT (리팩토링)
 * @notice 공연 티켓을 나타내는 ERC-721 NFT
 *
 * [변경사항]
 * - 좌석 정보 구조화 (section/row/seat/grade)
 * - 티켓 상태 enum (VALID → USED / EXPIRED / REFUNDED)
 * - 일괄 발행 (batchMintTickets)
 * - 입장 처리 (checkIn)
 * - 환불/회수 (reclaimTicket)
 */
contract TicketNFT is ERC721, Ownable {

    // ========== 상태 변수 ==========

    uint256 private _nextTokenId;

    enum TicketStatus {
        VALID,      // 유효 (사용 가능)
        USED,       // 사용됨 (입장 완료)
        EXPIRED,    // 만료됨
        REFUNDED    // 환불됨
    }

    struct TicketInfo {
        uint256 eventId;        // 이벤트 ID (EventNFT 연결)
        uint256 sessionId;      // 회차 ID
        string section;         // 구역 (예: "A", "VIP")
        uint256 row;            // 열
        uint256 seat;           // 좌석 번호
        string grade;           // 등급 (예: "VIP", "R", "S", "A")
        uint256 price;          // 티켓 가격 (SSF)
        TicketStatus status;    // 티켓 상태
        uint256 mintedAt;       // 발행 시각
    }

    // tokenId => TicketInfo
    mapping(uint256 => TicketInfo) public tickets;

    // eventId => sessionId => 지갑별 보유 수
    mapping(uint256 => mapping(uint256 => mapping(address => uint256))) public walletTicketCount;

    // ========== 이벤트 ==========

    event TicketMinted(
        uint256 indexed tokenId,
        address indexed to,
        uint256 eventId,
        uint256 sessionId
    );

    event TicketCheckedIn(
        uint256 indexed tokenId,
        address indexed holder
    );

    event TicketReclaimed(
        uint256 indexed tokenId,
        TicketStatus newStatus
    );

    // ========== 생성자 ==========

    constructor() ERC721("CHEKET Ticket", "CTKT") Ownable(msg.sender) {}

    // ========== 단건 발행 ==========

    /**
     * @notice 티켓 NFT 1장 발행
     */
    function mintTicket(
        address to,
        uint256 eventId,
        uint256 sessionId,
        string calldata section,
        uint256 row,
        uint256 seat,
        string calldata grade,
        uint256 price
    ) external onlyOwner returns (uint256) {
        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);

        tickets[tokenId] = TicketInfo({
            eventId: eventId,
            sessionId: sessionId,
            section: section,
            row: row,
            seat: seat,
            grade: grade,
            price: price,
            status: TicketStatus.VALID,
            mintedAt: block.timestamp
        });

        walletTicketCount[eventId][sessionId][to]++;

        emit TicketMinted(tokenId, to, eventId, sessionId);
        return tokenId;
    }

    // ========== 일괄 발행 ==========

    /**
     * @notice 같은 구역/열의 연속 좌석 티켓 일괄 발행
     */
    function batchMintTickets(
        address to,
        uint256 eventId,
        uint256 sessionId,
        string calldata section,
        uint256 row,
        uint256 startSeat,
        uint256 count,
        string calldata grade,
        uint256 price
    ) external onlyOwner returns (uint256) {
        require(count > 0, "Count must be > 0");

        uint256 startTokenId = _nextTokenId;

        for (uint256 i = 0; i < count; i++) {
            _safeMint(to, startTokenId + i);

            tickets[startTokenId + i] = TicketInfo({
                eventId: eventId,
                sessionId: sessionId,
                section: section,
                row: row,
                seat: startSeat + i,
                grade: grade,
                price: price,
                status: TicketStatus.VALID,
                mintedAt: block.timestamp
            });
        }

        _nextTokenId = startTokenId + count;
        walletTicketCount[eventId][sessionId][to] += count;

        return startTokenId;
    }

    // ========== 입장 처리 ==========

    /**
     * @notice QR 스캔 시 입장 처리
     */
    function checkIn(uint256 tokenId) external onlyOwner {
        require(ownerOf(tokenId) != address(0), "Ticket does not exist");
        require(tickets[tokenId].status == TicketStatus.VALID, "Ticket not valid");

        tickets[tokenId].status = TicketStatus.USED;

        emit TicketCheckedIn(tokenId, ownerOf(tokenId));
    }

    // ========== 환불/회수 ==========

    /**
     * @notice 티켓 상태를 환불 또는 만료로 변경
     */
    function reclaimTicket(uint256 tokenId, TicketStatus newStatus) external onlyOwner {
        require(
            newStatus == TicketStatus.EXPIRED || newStatus == TicketStatus.REFUNDED,
            "Invalid status"
        );
        require(tickets[tokenId].status == TicketStatus.VALID, "Ticket not valid");

        tickets[tokenId].status = newStatus;

        emit TicketReclaimed(tokenId, newStatus);
    }

    // ========== 조회 함수 ==========

    /**
     * @notice 티켓 정보 전체 조회 (struct 리턴)
     */
    function getTicket(uint256 tokenId) external view returns (TicketInfo memory) {
        return tickets[tokenId];
    }

    function getTicketStatus(uint256 tokenId) external view returns (TicketStatus) {
        return tickets[tokenId].status;
    }

    function getWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address wallet
    ) external view returns (uint256) {
        return walletTicketCount[eventId][sessionId][wallet];
    }

    function totalSupply() external view returns (uint256) {
        return _nextTokenId;
    }
}
