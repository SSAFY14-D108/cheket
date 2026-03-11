// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

/**
 * @title Escrow (SSF 에스크로)
 * @notice 티켓 구매 시 SSF를 에스크로에 보관, 정산 또는 환불 처리
 *
 * [흐름]
 * 1. 구매자가 SSF approve → deposit() → 에스크로에 SSF 잠금
 * 2-A. 공연 완료 → release() → Settlement로 SSF 전송 (정산)
 * 2-B. 환불 요청 → refund() → 구매자에게 SSF 반환
 */
contract Escrow is Ownable {

    // ========== 상태 변수 ==========

    IERC20 public ssfToken;    // SSF 토큰 컨트랙트

    struct EscrowInfo {
        address buyer;          // 구매자 지갑
        uint256 amount;         // 에스크로 금액 (SSF)
        uint256 eventId;        // 이벤트 ID
        uint256 sessionId;      // 회차 ID
        uint256 ticketId;       // 티켓 NFT ID
        EscrowStatus status;    // 상태
        uint256 createdAt;      // 생성 시각
    }

    enum EscrowStatus {
        LOCKED,      // 잠금 (결제 완료, 정산 대기)
        RELEASED,    // 정산 완료
        REFUNDED     // 환불 완료
    }

    uint256 private _nextEscrowId;

    // escrowId => EscrowInfo
    mapping(uint256 => EscrowInfo) public escrows;

    // ticketId => escrowId (티켓별 에스크로 조회용)
    mapping(uint256 => uint256) public ticketEscrow;

    // ========== 이벤트 ==========

    event Deposited(
        uint256 indexed escrowId,
        address indexed buyer,
        uint256 amount,
        uint256 ticketId
    );

    event Released(
        uint256 indexed escrowId,
        address indexed to,
        uint256 amount
    );

    event Refunded(
        uint256 indexed escrowId,
        address indexed buyer,
        uint256 amount
    );

    // ========== 생성자 ==========

    /**
     * @param _ssfToken SSF 토큰 컨트랙트 주소
     */
    constructor(address _ssfToken) Ownable(msg.sender) {
        require(_ssfToken != address(0), "Invalid SSF address");
        ssfToken = IERC20(_ssfToken);
    }

    // ========== 에스크로 입금 ==========

    /**
     * @notice 티켓 구매 시 SSF를 에스크로에 입금
     * @dev 호출 전 구매자가 ssfToken.approve(escrow주소, amount) 필요
     * @param buyer 구매자 지갑 주소
     * @param amount SSF 금액
     * @param eventId 이벤트 ID
     * @param sessionId 회차 ID
     * @param ticketId 티켓 NFT ID
     * @return escrowId 생성된 에스크로 ID
     */
    function deposit(
        address buyer,
        uint256 amount,
        uint256 eventId,
        uint256 sessionId,
        uint256 ticketId
    ) external onlyOwner returns (uint256) {
        require(amount > 0, "Amount must be > 0");

        // SSF를 구매자 → 에스크로 컨트랙트로 전송
        bool success = ssfToken.transferFrom(buyer, address(this), amount);
        require(success, "SSF transfer failed");

        uint256 escrowId = _nextEscrowId++;

        escrows[escrowId] = EscrowInfo({
            buyer: buyer,
            amount: amount,
            eventId: eventId,
            sessionId: sessionId,
            ticketId: ticketId,
            status: EscrowStatus.LOCKED,
            createdAt: block.timestamp
        });

        ticketEscrow[ticketId] = escrowId;

        emit Deposited(escrowId, buyer, amount, ticketId);
        return escrowId;
    }

    // ========== 정산 (릴리즈) ==========

    /**
     * @notice 에스크로 SSF를 정산 주소(Settlement)로 전송
     * @param escrowId 에스크로 ID
     * @param to 정산 컨트랙트 또는 수신 주소
     */
    function release(uint256 escrowId, address to) external onlyOwner {
        EscrowInfo storage e = escrows[escrowId];
        require(e.status == EscrowStatus.LOCKED, "Not locked");
        require(to != address(0), "Invalid address");

        e.status = EscrowStatus.RELEASED;

        bool success = ssfToken.transfer(to, e.amount);
        require(success, "SSF transfer failed");

        emit Released(escrowId, to, e.amount);
    }

    // ========== 환불 ==========

    /**
     * @notice 에스크로 SSF를 구매자에게 환불
     * @param escrowId 에스크로 ID
     */
    function refund(uint256 escrowId) external onlyOwner {
        EscrowInfo storage e = escrows[escrowId];
        require(e.status == EscrowStatus.LOCKED, "Not locked");

        e.status = EscrowStatus.REFUNDED;

        bool success = ssfToken.transfer(e.buyer, e.amount);
        require(success, "SSF transfer failed");

        emit Refunded(escrowId, e.buyer, e.amount);
    }

    // ========== 조회 함수 ==========

    function getEscrow(uint256 escrowId) external view returns (
        address buyer,
        uint256 amount,
        uint256 eventId,
        uint256 sessionId,
        uint256 ticketId,
        EscrowStatus status
    ) {
        EscrowInfo storage e = escrows[escrowId];
        return (e.buyer, e.amount, e.eventId, e.sessionId, e.ticketId, e.status);
    }

    function getEscrowByTicket(uint256 ticketId) external view returns (uint256) {
        return ticketEscrow[ticketId];
    }

    function totalEscrows() external view returns (uint256) {
        return _nextEscrowId;
    }
}
