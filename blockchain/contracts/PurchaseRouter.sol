// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
// → ERC-20 토큰(SSF)의 인터페이스
// → transferFrom(), balanceOf() 등 함수 시그니처 제공

import "@openzeppelin/contracts/access/Ownable.sol";
// → onlyOwner modifier 제공 (배포한 사람만 특정 함수 호출 가능)

// ========== 다른 컨트랙트 인터페이스 ==========
/**
 * [왜 contract 바깥에 선언?]
 * Solidity에서 interface는 contract 내부에 선언할 수 없음
 * → contract 바깥에 선언해야 컴파일 가능
 *
 * [왜 이름이 IPurchase~ 인가?]
 * Settlement.sol에도 같은 이름의 interface가 있으면 충돌
 * → PurchaseRouter용은 IPurchase~ 접두사로 구분
 */

// TicketNFT에서 필요한 함수들
interface IPurchaseTicketNFT {
    // 가격 조회 (on-chain에서 직접 읽음, 조작 불가)
    // → 백엔드가 가격을 파라미터로 넘기지 않음
    // → 컨트랙트가 직접 읽어서 SSF 전송액 결정
    function getPrice(uint256 tokenId) external view returns (uint256);

    // 티켓 정보 조회 (eventId 가져오기 위해)
    // → TicketNFT의 tickets public mapping 자동 getter 사용
    // → public mapping은 struct를 개별 값으로 반환 (getTicket()은 struct 반환이라 불일치)
    function tickets(uint256 tokenId) external view returns (
        uint256 eventId,        // 이벤트 ID
        uint256 sessionId,      // 회차 ID
        string memory section,  // 구역
        uint256 row,            // 열
        uint256 seat,           // 좌석 번호
        string memory grade,    // 등급
        uint256 price,          // 가격
        uint8 status,           // 상태 (enum → uint8)
        uint256 mintedAt        // 발행 시각
    );

    // NFT 소유권 이전 (플랫폼 → 구매자)
    // → ERC721 표준 함수
    // → 사전 조건: 플랫폼이 setApprovalForAll(PurchaseRouter, true) 해놔야 함
    function transferFrom(address from, address to, uint256 tokenId) external;

    // 1인당 보유 수 업데이트 (구매 제한 추적)
    // → 플랫폼 -1, 구매자 +1
    // → 사전 조건: setAuthorizedCaller(PurchaseRouter, true) 해놔야 함
    function updateWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address from,
        address to
    ) external;

    // 1인당 보유 수 조회 (구매 전 한도 체크)
    // → currentCount < maxPerWallet 이면 구매 가능
    function getWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address wallet
    ) external view returns (uint256);

    // 환불/만료된 티켓 재판매 시 VALID로 리셋
    function resetTicketStatus(uint256 tokenId) external;
}

// Settlement에서 필요한 함수
interface IPurchaseSettlement {
    // 입금 기록 (SSF가 도착한 후 장부에 기록)
    // → SSF 이동은 PurchaseRouter가 직접 처리
    // → Settlement은 장부만 기록 (역할 분리)
    function recordDeposit(uint256 sessionId, uint256 amount) external;
}

// EventNFT에서 필요한 함수
interface IPurchaseEventNFT {
    // 예매 가능 여부 확인
    // → bookingStartTime <= block.timestamp <= bookingEndTime
    // → 예매 기간이 아니면 false → 구매 불가
    function isBookingOpen(uint256 eventId) external view returns (bool);

    // 이벤트 정보 조회 (maxPerWallet 가져오기 위해)
    // → 1인당 최대 구매 수를 on-chain에서 직접 읽음
    function getEventInfo(uint256 eventId) external view returns (
        string memory metadataCID,      // IPFS CID
        uint256 totalSupply,            // 총 티켓 수
        uint256 maxPerWallet,           // 1인당 최대 구매 수
        uint256 resaleCapBps,           // 리세일 가격 상한
        uint256 bookingStartTime,       // 예매 시작
        uint256 bookingEndTime,         // 예매 종료
        bool isActive                   // 활성 상태
    );
}

/**
 * @title PurchaseRouter (1차 구매 원자적 처리 — 기획안 최적화 3)
 * @notice 1개 TX로 SSF 예치 + NFT 소유권 이전을 원자적으로 처리
 *
 * [원자적(Atomic)이란?]
 * "전부 성공하거나 전부 실패하거나"
 * SSF 전송은 성공했는데 NFT 이전은 실패? → 있을 수 없음
 * 하나라도 실패하면 전부 revert → 부분 실패 불가
 *
 * [기획안 — 최적화 3]
 * "구매 1건 = transferFrom TX + Settlement.deposit TX = 2 TX
 *  → purchaseTicket() 1개 함수로 원자적 처리
 *  → Before: 2 TX/구매 → After: 1 TX/구매 → 50% 감소"
 *
 * setApprovalForAll = ERC-721 표준 함수 (NFT 전송 허가)
 * setAuthorizedCaller = 우리가 만든 커스텀 함수 (함수 호출 허가)
 * 
 * [purchaseTicket() 내부 동작]
 * ❶ TicketNFT.getPrice(ticketId) → on-chain에서 가격 조회 (조작 불가)
 * ❷ TicketNFT.getTicket(ticketId) → eventId 조회
 * ❸ EventNFT.isBookingOpen(eventId) → 예매 기간 on-chain 검증
 * ❹ maxPerWallet on-chain 검증 → 구매 한도 컨트랙트 강제
 * ❺ SSF: 구매자 → Settlement 직접 전송 (플랫폼 안 거침 = 횡령 방지)
 * ❻ Settlement.recordDeposit() → 장부 기록
 * ❼ TicketNFT: 플랫폼 → 구매자 소유권 이전
 * ❽ walletTicketCount 업데이트
 *
 * CHEKET:
 *   구매자 SSF → Settlement 컨트랙트 (금고에 직접 잠김)
 *                ↑ 플랫폼도 접근 불가 → 횡령 불가능
 *
 * [사전 설정 필요 — 배포 후 1회]
 * ① 플랫폼 → TicketNFT.setApprovalForAll(PurchaseRouter, true)
 *    → PurchaseRouter가 플랫폼 소유 NFT를 transferFrom 가능
 *    → "내 NFT를 PurchaseRouter가 대신 보내도 됨" 허가
 *
 * ② TicketNFT.setAuthorizedCaller(PurchaseRouter주소, true)
 *    → PurchaseRouter가 updateWalletTicketCount() 호출 가능
 *
 * ③ Settlement.setPurchaseRouter(PurchaseRouter주소)
 *    → PurchaseRouter가 recordDeposit() 호출 가능
 *
 * [구매할 때마다 사전 조건]
 * ④ 구매자 → SSF.approve(PurchaseRouter, price)
 *    → PurchaseRouter가 구매자의 SSF를 가져갈 수 있도록 허가
 *    → Custodial 구조에서 백엔드가 구매자 Keystore로 대리 서명
 *    → 사용자는 "구매" 버튼만 누르면 됨
 *
 * [호출 주체]
 * 백엔드 TX Worker가 플랫폼 개인키로 호출
 * → msg.sender = 플랫폼 지갑 = owner
 * → onlyOwner 통과
 */
contract PurchaseRouter is Ownable {
    // ========== 상태 변수 (storage = 블록체인 영구 저장) ==========

    // SSF 토큰 컨트랙트 (SSAFY 네트워크에 이미 배포되어 있는 것)
    IERC20 public ssfToken;

    // ========== 컨트랙트 주소 ==========

    // 배포 후 setContracts()로 설정
    // 순환 의존 때문에 생성자에서 못 넣고 나중에 설정
    address public ticketNFTAddress;     // TicketNFT 컨트랙트 주소
    address public settlementAddress;   // Settlement 컨트랙트 주소
    address public eventNFTAddress;     // EventNFT 컨트랙트 주소

    // 플랫폼 지갑 주소
    // → NFT의 현재 소유자 (배치 민팅 시 플랫폼이 전부 소유)
    // → transferFrom(platformWallet, buyer, ticketId) 에서 from으로 사용
    address public platformWallet;

    // setContracts() 1회 제한 플래그
    bool private _contractsInitialized;

    // ========== 이벤트 (블록체인 로그) ==========

    // 구매 완료 시 발생 → 백엔드 Event Listener가 감지 → DB 업데이트
    // topics[1]: 어떤 티켓?
    // topics[2]: 누가 샀는지?
    // topics[3]: 어떤 회차?
    // data: 얼마에?
    event TicketPurchased(
        uint256 indexed ticketId,       
        address indexed buyer,          
        uint256 indexed sessionId,      
        uint256 price                   
    );

    // ========== 생성자 (배포 시 1번만 실행) ==========

    /**
     * @param _ssfToken SSF 토큰 컨트랙트 주소 (SSAFY 네트워크)
     * @param _platformWallet 플랫폼 지갑 주소 (NFT 재고 보유자)
     *
     * SSF 주소와 플랫폼 지갑은 배포 시점에 이미 알고 있으므로
     * 생성자에서 바로 설정
     *
     * TicketNFT, Settlement, EventNFT 주소는 아직 배포 안 됐을 수 있으므로
     * setContracts()로 나중에 설정
     */
    constructor(
        address _ssfToken,
        address _platformWallet
    ) {
        // v4: Ownable() 자동으로 msg.sender가 owner
        require(_ssfToken != address(0), "Invalid SSF");
        require(_platformWallet != address(0), "Invalid platform wallet");
        ssfToken = IERC20(_ssfToken);
        // IERC20(_ssfToken) = "이 주소의 컨트랙트를 ERC-20으로 취급"
        // → ssfToken.transferFrom() 호출 가능
        platformWallet = _platformWallet;
    }

    // ========== 컨트랙트 주소 설정 (배포 후 1회) ==========

    /**
     * @notice TicketNFT, Settlement, EventNFT 주소 설정
     *
     * [왜 생성자에서 안 넣나?]
     * 배포 순서:
     *   ① TicketNFT 배포 → 주소 확보
     *   ② Settlement 배포 → 주소 확보
     *   ③ EventNFT 배포 → 주소 확보
     *   ④ PurchaseRouter 배포
     *   ⑤ PurchaseRouter.setContracts(①, ②, ③) ← 여기서 설정
     *
     * PurchaseRouter 배포 시점에 다른 컨트랙트가 배포 안 됐을 수 있음
     * → setter로 분리하여 유연성 확보
     */
    function setContracts(
        address _ticketNFT,
        address _settlement,
        address _eventNFT
    ) external onlyOwner {
        require(!_contractsInitialized, "Already initialized");
        require(_ticketNFT != address(0), "Invalid TicketNFT");
        require(_settlement != address(0), "Invalid Settlement");
        require(_eventNFT != address(0), "Invalid EventNFT");
        ticketNFTAddress = _ticketNFT;
        settlementAddress = _settlement;
        eventNFTAddress = _eventNFT;
        _contractsInitialized = true;
    }

    // ========== 핵심: 티켓 구매 (1 TX 원자적 처리) ==========

    /**
     * @notice 티켓 구매 — SSF 예치 + NFT 이전을 1 TX로 처리
     *
     * [비동기 처리 — 기획안 7.3]
     * 백엔드 흐름:
     *   사용자가 "구매" 버튼 클릭
     *   → DB에 Ticket PENDING 기록 → 즉시 API 응답 (~50ms)
     *   → TX Worker(백그라운드):
     *     a) 구매자 Keystore로 SSF.approve(PurchaseRouter, price) 서명
     *     b) 플랫폼 개인키로 PurchaseRouter.purchaseTicket() 호출 ← 이 함수
     *   → Event Listener: TicketPurchased 감지 → DB CONFIRMED → Push 알림
     *
     * [원자성 보장]
     * ❶~❽ 중 어느 하나라도 실패 → 전부 revert
     * → "돈은 냈는데 티켓 못 받음" 불가능
     * → "티켓은 받았는데 돈 안 냄" 불가능
     *
     * 상태 흐름:
     *   PENDING → SUBMITTED → CONFIRMED / FAILED
     *
     * @param buyer 구매자 지갑 주소
     * @param ticketId 구매할 TicketNFT tokenId
     * @param sessionId 회차 ID (Settlement 입금 기록용)
     */
    function purchaseTicket(
        address buyer,          
        uint256 ticketId,       
        uint256 sessionId       
    ) external onlyOwner {
        // onlyOwner = 플랫폼(백엔드)만 호출 가능
        // 사용자가 직접 호출할 수 없음 (Custodial 구조)
        // 백엔드 TX Worker가 플랫폼 개인키로 서명하여 호출

        require(buyer != address(0), "Invalid buyer");

        // 컨트랙트 주소 설정 확인
        require(ticketNFTAddress != address(0), "TicketNFT not set");
        require(settlementAddress != address(0), "Settlement not set");
        require(eventNFTAddress != address(0), "EventNFT not set");

        // 인터페이스로 다른 컨트랙트 연결
        IPurchaseTicketNFT ticketNFT = IPurchaseTicketNFT(ticketNFTAddress);
        IPurchaseSettlement settlement = IPurchaseSettlement(settlementAddress);
        IPurchaseEventNFT eventNFT = IPurchaseEventNFT(eventNFTAddress);

        // ========== ❶ on-chain에서 가격 조회 (조작 불가) ==========
        uint256 price = ticketNFT.getPrice(ticketId);
        require(price > 0, "Invalid ticket price");

        // ========== ❷ 티켓 정보에서 eventId, sessionId 조회 및 검증 ==========
        // tickets() = TicketNFT의 public mapping 자동 getter
        // struct를 개별 값으로 반환하므로 ABI 불일치 없음
        (uint256 eventId, uint256 ticketSessionId, , , , , , , ) = ticketNFT.tickets(ticketId);

        // 파라미터로 받은 sessionId와 티켓 실제 sessionId 일치 검증
        // 불일치 시 잘못된 session의 deposit이 기록되어 정산/환불 오류 발생
        require(ticketSessionId == sessionId, "Session mismatch");

        // ========== ❸ 예매 가능 여부 — 백엔드에서 검증 (온체인 검증 제거) ==========
        // 예매 기간 검증은 백엔드에서 처리 (DB 기준)
        // 온체인 bookingStartTime/bookingEndTime은 민팅 시 고정되어 DB 변경과 동기화 안 됨

        // ========== ❹ 1인당 구매 한도 on-chain 검증 ==========
        (, , uint256 maxPerWallet, , , , ) = eventNFT.getEventInfo(eventId);
        uint256 currentCount = ticketNFT.getWalletTicketCount(eventId, sessionId, buyer);
        require(currentCount < maxPerWallet, "Exceeds max per wallet");

        // ========== ❺ SSF 전송: 구매자 → Settlement 직접 ==========
        bool success = ssfToken.transferFrom(buyer, settlementAddress, price);
        require(success, "SSF transfer failed");

        // ========== ❻ Settlement 장부 기록 ==========
        settlement.recordDeposit(sessionId, price);

        // ========== ❼ 환불/만료된 티켓이면 VALID로 리셋 ==========
        // 환불된 NFT가 플랫폼에 회수된 후 재판매될 때
        // 상태가 REFUNDED/EXPIRED인 채로 남아있으면
        // 이후 환불/입장 시 "Ticket not valid" 에러 발생
        ticketNFT.resetTicketStatus(ticketId);

        // ========== ❽ NFT 소유권 이전: 플랫폼 → 구매자 ==========
        ticketNFT.transferFrom(platformWallet, buyer, ticketId);

        // ========== ❽ 보유 수 업데이트 ==========
        ticketNFT.updateWalletTicketCount(eventId, sessionId, platformWallet, buyer);

        // ========== 이벤트 로그 ==========
        emit TicketPurchased(ticketId, buyer, sessionId, price);
    }
}
