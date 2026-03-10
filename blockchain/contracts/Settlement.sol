// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

/**
 * @title Settlement (수익 정산)
 * @notice StakeholderNFT의 bps 비율에 따라 SSF를 분배
 *
 * [흐름]
 * 1. Escrow에서 release() → Settlement에 SSF 도착
 * 2. settle() 호출 → 이해관계자들에게 bps 비율대로 분배
 *
 * [예시] 티켓 10,000 SSF 정산
 * - 주최자 70% → 7,000 SSF
 * - 아티스트 20% → 2,000 SSF
 * - 장소 5% → 500 SSF
 * - 플랫폼 5% → 500 SSF
 */
contract Settlement is Ownable {

    // ========== 상태 변수 ==========

    IERC20 public ssfToken;

    struct SettlementRecord {
        uint256 eventId;         // 이벤트 ID
        uint256 sessionId;       // 회차 ID
        uint256 totalAmount;     // 총 정산 금액
        uint256 settledAt;       // 정산 시각
    }

    uint256 private _nextSettlementId;

    // settlementId => SettlementRecord
    mapping(uint256 => SettlementRecord) public settlements;

    // 분배 내역 (settlementId => stakeholder 주소 => 금액)
    mapping(uint256 => mapping(address => uint256)) public distributions;

    // ========== 이벤트 ==========

    event Settled(
        uint256 indexed settlementId,
        uint256 indexed eventId,
        uint256 indexed sessionId,
        uint256 totalAmount
    );

    event Distributed(
        uint256 indexed settlementId,
        address indexed stakeholder,
        uint256 amount,
        uint256 shareBps
    );

    // ========== 생성자 ==========

    constructor(address _ssfToken) Ownable(msg.sender) {
        require(_ssfToken != address(0), "Invalid SSF address");
        ssfToken = IERC20(_ssfToken);
    }

    // ========== 정산 함수 ==========

    /**
     * @notice 이해관계자들에게 SSF 분배
     * @dev 호출 전 Settlement 컨트랙트에 충분한 SSF가 있어야 함
     * @param eventId 이벤트 ID
     * @param sessionId 회차 ID
     * @param totalAmount 총 정산 금액
     * @param wallets 이해관계자 지갑 주소 배열
     * @param bpsArray 각 이해관계자의 분배 비율 (bps) 배열
     * @return settlementId 정산 기록 ID
     */
    function settle(
        uint256 eventId,
        uint256 sessionId,
        uint256 totalAmount,
        address[] calldata wallets,
        uint256[] calldata bpsArray
    ) external onlyOwner returns (uint256) {
        require(wallets.length > 0, "No stakeholders");
        require(wallets.length == bpsArray.length, "Array length mismatch");
        require(totalAmount > 0, "Amount must be > 0");

        // bps 합계 검증 (10000 = 100%)
        uint256 totalBps = 0;
        for (uint256 i = 0; i < bpsArray.length; i++) {
            totalBps += bpsArray[i];
        }
        require(totalBps == 10000, "Total bps must be 10000");

        // Settlement 컨트랙트 잔액 확인
        require(
            ssfToken.balanceOf(address(this)) >= totalAmount,
            "Insufficient SSF balance"
        );

        uint256 settlementId = _nextSettlementId++;

        settlements[settlementId] = SettlementRecord({
            eventId: eventId,
            sessionId: sessionId,
            totalAmount: totalAmount,
            settledAt: block.timestamp
        });

        // 분배 실행
        uint256 distributed = 0;
        for (uint256 i = 0; i < wallets.length; i++) {
            uint256 share = (totalAmount * bpsArray[i]) / 10000;

            // 마지막 이해관계자에게 나머지 몰아주기 (반올림 오차 방지)
            if (i == wallets.length - 1) {
                share = totalAmount - distributed;
            }

            distributions[settlementId][wallets[i]] = share;

            bool success = ssfToken.transfer(wallets[i], share);
            require(success, "SSF transfer failed");

            emit Distributed(settlementId, wallets[i], share, bpsArray[i]);
            distributed += share;
        }

        emit Settled(settlementId, eventId, sessionId, totalAmount);
        return settlementId;
    }

    // ========== 조회 함수 ==========

    function getSettlement(uint256 settlementId) external view returns (
        uint256 eventId,
        uint256 sessionId,
        uint256 totalAmount,
        uint256 settledAt
    ) {
        SettlementRecord storage s = settlements[settlementId];
        return (s.eventId, s.sessionId, s.totalAmount, s.settledAt);
    }

    function getDistribution(
        uint256 settlementId,
        address wallet
    ) external view returns (uint256) {
        return distributions[settlementId][wallet];
    }

    function totalSettlements() external view returns (uint256) {
        return _nextSettlementId;
    }
}
