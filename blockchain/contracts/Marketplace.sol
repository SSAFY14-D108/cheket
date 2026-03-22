// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/access/Ownable.sol";
// → onlyOwner modifier 제공 (배포한 사람만 특정 함수 호출 가능)

import "@openzeppelin/contracts/token/ERC721/IERC721.sol";
// → ERC-721 NFT(TicketNFT)의 인터페이스
// → transferFrom(), ownerOf() 등 호출 가능

// TicketNFT에서 양도 시 필요한 함수들
interface IMarketplaceTicketNFT {
    // 티켓 정보 조회 (eventId, sessionId 가져오기 위해)
    // → TicketNFT의 tickets public mapping 자동 getter 사용
    function tickets(uint256 tokenId) external view returns (
        uint256 eventId,
        uint256 sessionId,
        string memory section,
        uint256 row,
        uint256 seat,
        string memory grade,
        uint256 price,
        uint8 status,
        uint256 mintedAt
    );

    // 1인당 보유 수 업데이트 (양도 시 from -1, to +1)
    function updateWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address from,
        address to
    ) external;

    // NFT 소유권 이전
    function transferFrom(address from, address to, uint256 tokenId) external;

    // NFT 소유자 조회
    function ownerOf(uint256 tokenId) external view returns (address);

    // 티켓 상태 조회 (VALID인 티켓만 양도 허용)
    function getTicketStatus(uint256 tokenId) external view returns (uint8);
}

/**
 * @title Marketplace (지정 양도 — 기획안 7.7)
 * @notice 1:1 무료 직접 양도 처리
 *
 * [역할 분리]
 * 유료 거래 (리세일) → Escrow가 처리 (SSF + NFT 원자적 교환)
 * 무료 양도           → Marketplace가 처리 (NFT만 전송, SSF 이동 없음)
 *
 * [양도 흐름 — 기획안 7.7]
 * 1. 보내는 사람이 받는 사람 전화번호 입력
 * 2. 서버: 전화번호 → 계정 → Custodial 지갑 주소 매핑
 * 3. 화면에 닉네임 표시 ("박지연 님에게 양도합니다")
 * 4. 확인 → maxPerWallet 검증 (받는 사람)
 * 5. Marketplace.directTransfer()
 * 6. 양측 Push 알림
 *
 * [Custodial 구조]
 * 사용자는 지갑 주소를 모름
 * 전화번호 → 계정 → 지갑 주소 매핑은 백엔드가 처리
 * 사용자 입장에서는 "전화번호로 양도"
 * → 블록체인을 모르는 일반 관객도 쉽게 사용 가능
 *
 * [왜 Marketplace를 통하는가?]
 * TicketNFT.transferFrom()을 직접 호출해도 되지만:
 * ① 이벤트 로그를 남기기 위해 (DirectTransferred)
 *    → 양도 이력을 on-chain에 기록
 *    → 누가 누구에게 양도했는지 투명하게 추적
 * ② 추후 양도 제한 로직 추가 가능
 *    → 예: 공연 당일 양도 불가
 *    → 예: 동일 티켓 양도 횟수 제한
 * ③ 양도 이력 관리 (이벤트 로그 기반)
 *    → 앱에서 "이 티켓의 전체 거래 이력" 표시 가능
 *
 * [기획안 7.7]
 * "무료 양도 전용. 유료 양도는 리세일 마켓(Escrow) 이용."
 * "지갑 주소 대신 전화번호를 식별자로 사용
 *  — Custodial 구조에서 사용자는 자신의 지갑 주소를 알 필요 없음."
 *
 * [사전 조건]
 * 보내는 사람이 TicketNFT.approve(Marketplace주소, ticketId) 필요
 * → "Marketplace가 내 NFT를 대신 보내도 됨"
 * → Custodial 구조에서 백엔드가 보내는 사람 Keystore로 대리 서명
 */
contract Marketplace is Ownable {

    // ========== 상태 변수 (storage = 블록체인 영구 저장) ==========

    // TicketNFT 컨트랙트 (IERC721 기본 인터페이스)
    IERC721 public ticketNFT;
    // TicketNFT 컨트랙트 주소 (커스텀 인터페이스용)
    address public ticketNFTAddress;

    // ========== 이벤트 (블록체인 로그) ==========

    // 양도 완료 시 발생 → 백엔드 Event Listener가 감지 → DB 업데이트
    event DirectTransferred(
        uint256 indexed ticketId,       // topics[1]: 어떤 티켓?
        address indexed from,           // topics[2]: 보내는 사람
        address indexed to              // topics[3]: 받는 사람
    );
    // 로그 예시:
    //   "티켓 #42가 0xAAA → 0xBBB로 양도됨"
    //   → 앱에서 거래 이력 조회 시 표시
    //   → 누구나 조회 가능 (투명성)

    // ========== 생성자 (배포 시 1번만 실행) ==========

    /**
     * @param _ticketNFT TicketNFT 컨트랙트 주소
     *
     * Marketplace는 TicketNFT만 다루므로 (SSF 이동 없음)
     * 생성자에서 TicketNFT 주소만 받으면 됨
     *
     * [왜 SSF(IERC20)가 없는가?]
     * 무료 양도 → SSF 이동 없음 → SSF 컨트랙트 불필요
     * 유료 거래(리세일)는 Escrow가 담당
     */
    constructor(address _ticketNFT) {
        // v4: Ownable() 자동으로 msg.sender가 owner
        require(_ticketNFT != address(0), "Invalid TicketNFT");
        ticketNFT = IERC721(_ticketNFT);
        ticketNFTAddress = _ticketNFT;
        // ticketNFT: ERC-721 표준 인터페이스 (transferFrom, ownerOf)
        // ticketNFTAddress: 커스텀 함수 호출용 (updateWalletTicketCount, getTicket)
    }

    // ========== 지정 양도 (무료 1:1 전송) ==========

    /**
     * @notice 티켓을 다른 사람에게 무료 양도
     *
     * [기획안 7.7 흐름]
     * 1. 앱: 보내는 사람이 받는 사람 전화번호 입력
     * 2. 백엔드: 전화번호 → DB에서 계정 조회 → Custodial 지갑 주소 매핑
     * 3. 앱: 화면에 닉네임 표시 ("박지연 님에게 양도합니다")
     * 4. 앱: 사용자가 "확인" 버튼 클릭
     * 5. 백엔드: maxPerWallet 검증 (받는 사람이 구매 한도 초과하지 않는지)
     * 6. 백엔드: 보내는 사람 Keystore로 approve TX 대리 서명
     * 7. 백엔드: 플랫폼 개인키로 Marketplace.directTransfer() 호출 ← 이 함수
     * 8. Event Listener: DirectTransferred 감지 → DB 업데이트
     * 9. 백엔드: 양측 Push 알림
     *
     * [on-chain 소유권 검증]
     * ownerOf(ticketId) == from 검증
     * → DB가 해킹되어 잘못된 from이 와도 on-chain에서 차단
     * → "실제 소유자만 양도 가능" 보장
     *
     * [왜 onlyOwner인가?]
     * Custodial 구조 → 사용자가 직접 호출하지 않음
     * 백엔드가 검증 후 플랫폼 개인키로 호출
     * → maxPerWallet 등 추가 검증을 백엔드에서 처리
     *
     * [사전 조건]
     * 보내는 사람이 TicketNFT.approve(Marketplace주소, ticketId) 필요
     * → "Marketplace가 내 NFT를 대신 보내도 됨"
     * → Custodial 구조에서 백엔드가 보내는 사람 Keystore로 대리 서명
     * → 사용자는 "양도" 버튼만 누르면 됨
     *
     * @param from 보내는 사람 지갑 (양도하는 사람)
     * @param to 받는 사람 지갑 (양도받는 사람)
     * @param ticketId TicketNFT tokenId
     */
    function directTransfer(
        address from,       // 보내는 사람 지갑 → 값 타입 → 스택
        address to,         // 받는 사람 지갑 → 값 타입 → 스택
        uint256 ticketId    // 티켓 tokenId → 값 타입 → 스택
    ) external onlyOwner {
        // onlyOwner = 플랫폼(백엔드)만 호출 가능
        // 사용자가 직접 호출 불가 (Custodial 구조)

        IMarketplaceTicketNFT ticket = IMarketplaceTicketNFT(ticketNFTAddress);

        require(ticket.ownerOf(ticketId) == from, "Not ticket owner");
        // on-chain 소유권 검증

        require(to != address(0), "Invalid address");
        require(to != from, "Cannot transfer to self");

        // ========== 티켓 상태 on-chain 검증 ==========
        // VALID(0)인 티켓만 양도 허용
        // USED/REFUNDED/EXPIRED 티켓 양도 차단 (컨트랙트 레벨 강제)
        require(ticket.getTicketStatus(ticketId) == 0, "Ticket not valid");

        // ========== NFT 전송: 보내는 사람 → 받는 사람 ==========

        ticket.transferFrom(from, to, ticketId);

        // ========== walletTicketCount 갱신 ==========
        // ERC-721 transferFrom()은 커스텀 매핑(walletTicketCount)을 갱신하지 않음
        // → 수동으로 from -1, to +1 처리해야 maxPerWallet이 정확하게 작동
        (uint256 eventId, uint256 sessionId, , , , , , , ) = ticket.tickets(ticketId);
        ticket.updateWalletTicketCount(eventId, sessionId, from, to);

        // ========== 이벤트 로그 ==========

        emit DirectTransferred(ticketId, from, to);
        // 블록체인에 로그 영구 기록:
        //   "ticketId #42가 0xAAA에서 0xBBB로 양도됨"
        //
        // 백엔드 Event Listener가 감지:
        //   → DB 소유자 업데이트 (tickets.user_id = 받는 사람)
        //   → walletTicketCount 업데이트 (백엔드에서 별도 처리)
        //   → 양측 Push 알림:
        //     보내는 사람: "박지연 님에게 양도가 완료되었습니다"
        //     받는 사람: "김철수 님이 티켓을 양도했습니다"
        //
        // [거래 이력 조회]
        // ERC721 Transfer 이벤트 + DirectTransferred 이벤트
        // → 앱에서 "이 티켓의 전체 이력" 표시 가능:
        //   [2026-03-15] 발행됨 (플랫폼)
        //   [2026-03-16] 구매 (구매자A)
        //   [2026-03-17] 양도 (구매자A → 구매자B) ← 이 이벤트
        //   [2026-03-18] 입장 (구매자B)
    }
}
