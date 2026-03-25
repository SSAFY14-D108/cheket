// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
// → ERC-20 토큰(SSF)의 인터페이스
// → transfer(), transferFrom(), balanceOf() 등 함수 시그니처 제공
// → "I"ERC20의 I = Interface (구현이 아닌 시그니처만)

import "@openzeppelin/contracts/access/Ownable.sol";
// → onlyOwner modifier 제공 (배포한 사람만 특정 함수 호출 가능)

// ========== 다른 컨트랙트 인터페이스 ==========
/**
 * [interface란?]
 * 다른 컨트랙트의 함수를 호출하기 위한 "함수 시그니처 목록"
 * 실제 구현(코드)은 없고, "이 함수가 있다"는 것만 선언
 *
 * [왜 필요한가?]
 * Settlement가 EventNFT/StakeholderNFT/TicketNFT의 함수를 호출하려면
 * "그 컨트랙트에 어떤 함수가 있는지" 알아야 함
 * → 인터페이스로 함수 시그니처를 선언
 * → 그 주소로 호출하면 실제 컨트랙트의 함수가 실행됨
 *
 * [왜 contract 바깥에 선언?]
 * Solidity에서 interface는 contract 내부에 선언 불가 → 컴파일 에러
 *
 * [왜 ISettlement~ 접두사?]
 * PurchaseRouter.sol에도 같은 이름의 interface가 있으면 충돌
 * → Settlement용은 ISettlement~ 접두사로 구분
 */

// EventNFT에서 필요한 함수들
interface ISettlementEventNFT {
    // eventId → 해당 공연의 StakeholderNFT ID 배열 반환
    // 예: getStakeholderTokenIds(0) → [0, 1, 2]
    function getStakeholderTokenIds(uint256 eventId) external view returns (uint256[] memory);

    // 회차 정산 완료 표시 (isFinalized = true → 이중 정산 방지)
    function finalizeSession(uint256 sessionId) external;

    // 현재 시점에서의 환불 비율 조회 (on-chain 환불 정책)
    // → Settlement가 환불액을 직접 계산 (백엔드 조작 불가)
    function getRefundRate(uint256 eventId, uint256 sessionTimestamp) external view returns (uint256);

    // 회차 정보 조회 (공연 시각 가져오기 위해)
    function getSession(uint256 sessionId) external view returns (
        uint256 eventId,
        uint256 sessionTimestamp,
        uint256 ticketSupply,
        bool isFinalized
    );
}

// StakeholderNFT에서 필요한 함수
interface ISettlementStakeholderNFT {
    // tokenId → (지갑주소, 역할, 비율, 이벤트ID) 반환
    // 예: getStakeholder(0) → (0xAAA, "organizer", 7200, 0)
    function getStakeholder(uint256 tokenId) external view returns (
        address wallet,         // 이해관계자 지갑 주소
        string memory role,     // "organizer" / "artist"
        uint256 shareBps,       // 수익 비율 (10000 = 100%)
        uint256 eventNftId      // 연결된 EventNFT ID
    );
}

// TicketNFT에서 필요한 함수 (원자적 환불용)
interface ISettlementTicketNFT {
    // 가격 조회 (on-chain에서 직접)
    function getPrice(uint256 tokenId) external view returns (uint256);

    // 티켓 정보 조회 (eventId 가져오기 위해)
    // → TicketNFT의 tickets public mapping 자동 getter 사용
    // → getTicket()은 struct 반환이라 ABI 불일치 발생하므로 tickets() 사용
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

    // NFT 소유자 조회 (환불 시 소유자 검증용)
    function ownerOf(uint256 tokenId) external view returns (address);

    // 티켓 상태를 REFUNDED로 변경
    // 3 = TicketStatus.REFUNDED
    function reclaimTicket(uint256 tokenId, uint8 newStatus) external;

    // NFT 소유권 이전 (구매자 → 플랫폼)
    function transferFrom(address from, address to, uint256 tokenId) external;

    // 보유 수 업데이트 (구매자 -1, 플랫폼 +1)
    function updateWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address from,
        address to
    ) external;
}

/**
 * @title Settlement (1차 구매 정산 — 기획안 5.5)
 * @notice 1차 구매 SSF를 직접 받아 보관, 공연 후 stakeholder에게 자동 분배
 *
 * [핵심 원리 — 기획안 5.5]
 * "판매 대금이 플랫폼 지갑을 거치지 않고 컨트랙트에 직접 예치.
 *  플랫폼을 포함한 누구도 finalizeSession() 전까지 자금에 접근 불가."
 *
 * [왜 이 구조가 중요한가?]
 * 기존:
 *   구매자 SSF → 플랫폼 지갑 → (나중에) → stakeholder
 *                ↑ 플랫폼이 중간에 돈을 가짐 → 횡령 가능
 *
 * CHEKET:
 *   구매자 SSF → Settlement 컨트랙트 (금고에 잠김)
 *                ↑ 플랫폼도 열쇠 없음 → 횡령 불가능
 *                finalizeSession() 호출 시
 *                → StakeholderNFT 비율대로만 분배
 *                → 플랫폼 8%도 컨트랙트가 계산해서 지급
 *
 * [기존 코드 대비 변경]
 * 이전: finalizeSession(wallets[], bpsArray[])
 *       → 백엔드가 지갑/비율을 파라미터로 전달
 *       → 백엔드가 비율을 조작할 수 있음
 *
 * 변경: finalizeSession(sessionId, eventId)
 *       → 컨트랙트가 EventNFT/StakeholderNFT에서 직접 읽음
 *       → 백엔드는 "트리거만" (sessionId, eventId만 전달)
 *       → 비율 조작 불가능 = CHEKET 투명성의 핵심
 *
 * [SSF 흐름]
 *
 * 1차 구매 시:
 *   구매자 지갑 ──SSF 전송──→ Settlement (금고에 잠김)
 *   → 플랫폼도 못 건드림 (코드로 막혀있음)
 *   → 공연 끝날 때까지 보관
 *
 * 환불 시:
 *   Settlement ──SSF 반환──→ 구매자 지갑 (on-chain 환불률로 자동 계산)
 *
 * 정산 시 (공연 다음날):
 *   Settlement [총액]
 *       ├── 72% → 주최자 지갑
 *       ├── 20% → 아티스트 지갑
 *       └──  8% → 플랫폼 지갑
 *       → 비율: StakeholderNFT에 적힌 그대로 (조작 불가)
 *       → 1원도 Settlement에 안 남음
 */
contract Settlement is Ownable {

    // ========== 상태 변수 (storage = 블록체인 영구 저장) ==========

    // SSF 토큰 컨트랙트 (SSAFY 네트워크에 이미 배포되어 있는 것)
    // IERC20 = ERC-20 인터페이스 → transfer(), balanceOf() 등 호출 가능
    IERC20 public ssfToken;

    // ========== 컨트랙트 주소 ==========

    // 배포 후 setContracts()로 설정
    // [왜 상태 변수로 저장?]
    // finalizeSession(), refund()에서 다른 컨트랙트 함수를 호출할 때
    // "어디에 있는 컨트랙트인지" 주소를 알아야 함
    address public eventNFTAddress;
    address public stakeholderNFTAddress;
    address public ticketNFTAddress;
    address public platformWallet;

    // PurchaseRouter 주소 (recordDeposit 호출 권한 제한용)
    // PurchaseRouter만 입금 기록을 남길 수 있음
    // 아무나 recordDeposit을 호출하면 가짜 입금 기록이 생김
    address public purchaseRouter;

    // ========== 회차별 입금 관리 ==========

    /**
     * 회차별 누적 입금액
     * sessionDeposits[sessionId] = 해당 회차에서 판매된 총 SSF
     *
     * 예: sessionId 0인 회차에서 5만 SSF 티켓 3장 판매
     *     sessionDeposits[0] = 150,000
     *
     * 구매할 때마다: sessionDeposits[sessionId] += price  (누적)
     * 환불할 때마다: sessionDeposits[sessionId] -= amount (차감)
     * 정산 시: 이 금액을 stakeholder들에게 분배
     */
    mapping(uint256 => uint256) public sessionDeposits;

    /**
     * 정산 완료 여부 (이중 정산 방지)
     * sessionFinalized[sessionId] = true → 다시 정산 불가
     *
     * [왜 필요한가?]
     * finalizeSession()이 2번 호출되면 돈이 2배로 나감
     * → true로 표시해서 2번째 호출 시 revert
     */
    mapping(uint256 => bool) public sessionFinalized;

    // ========== 정산 기록 ==========

    /**
     * 정산 이력을 on-chain에 영구 기록
     * 누구나 조회하여 "이 회차에서 얼마가 정산되었는지" 검증 가능
     * → 투명성: 감독기관, 아티스트, 관객 모두 확인 가능
     */
    struct SettlementRecord {
        uint256 eventId;         // 어떤 공연?
        uint256 sessionId;       // 어떤 회차?
        uint256 totalAmount;     // 총 정산 금액 (SSF)
        uint256 settledAt;       // 정산 시각 (unix timestamp)
    }

    // 다음 정산 기록 ID (0부터 자동 증가)
    uint256 private _nextSettlementId;

    // settlementId → 정산 기록
    mapping(uint256 => SettlementRecord) public settlements;

    /**
     * 각 stakeholder가 얼마를 받았는지 on-chain 영구 기록
     * distributions[정산ID][지갑주소] = 받은 SSF 금액
     *
     * 예: distributions[0][주최자지갑] = 108,000
     *     distributions[0][아티스트지갑] = 30,000
     *     distributions[0][플랫폼지갑] = 12,000
     *
     * 누구나 조회 가능 → "아티스트가 실제로 얼마 받았는지" 검증
     */
    mapping(uint256 => mapping(address => uint256)) public distributions;

    // ========== 이벤트 (블록체인 로그) ==========
    // emit으로 발생 → 백엔드 Event Listener가 감지 → DB 동기화
    // indexed = topics에 저장 → 필터링 가능 (최대 3개)

    // 구매 시 입금 기록
    // topics[1]: 어떤 회차?
    // data: 입금액
    // data: 누적 총액
    event DepositRecorded(
        uint256 indexed sessionId,  
        uint256 amount,             
        uint256 newTotal            
    );

    // 환불 시
    // topics[1]: 어떤 회차?
    // topics[2]: 누구에게 환불?
    // topics[3]: 어떤 티켓?
    // data: 환불액
    event Refunded(
        uint256 indexed sessionId,  
        address indexed buyer,  
        uint256 indexed tokenId,    
        uint256 amount              
    );

    // 정산 완료 시
    // topics[1]: 정산 기록 ID
    // topics[2]: 어떤 공연?
    // topics[3]: 어떤 회차?
    // data: 총 정산액
    event SessionFinalized(
        uint256 indexed settlementId,   
        uint256 indexed eventId,        
        uint256 indexed sessionId,      
        uint256 totalAmount             
    );

    // 각 stakeholder에게 분배 시
    // topics[1]: 정산 기록 ID
    // topics[2]: 누구에게?
    // data: 분배 금액
    // data: 분배 비율
    event Paid(
        uint256 indexed settlementId,   
        address indexed stakeholder,    
        uint256 amount,                 
        uint256 shareBps                
    );

    // ========== 생성자 (배포 시 1번만 실행) ==========

    /**
     * @param _ssfToken SSF 토큰 컨트랙트 주소 (SSAFY 네트워크)
     *
     * Ownable(msg.sender) → 배포한 사람(플랫폼 지갑)이 owner
     * SSF 주소는 SSAFY 네트워크에 이미 존재하므로 생성자에서 받을 수 있음
     *
     * 배포 후에는 constructor 다시 호출 불가 (1회성)
     */
    constructor(address _ssfToken) {
        // v4: Ownable() 자동으로 msg.sender가 owner
        require(_ssfToken != address(0), "Invalid SSF address");
        // address(0) = 0x0000...0000 (존재하지 않는 주소)
        // 실수로 빈 주소를 넣는 것 방지
        ssfToken = IERC20(_ssfToken);
        // IERC20(_ssfToken) = "이 주소의 컨트랙트를 ERC-20으로 취급하겠다"
        // → 이제 ssfToken.transfer(), ssfToken.balanceOf() 호출 가능
    }

    // ========== 주소 설정 (배포 후 1회) ==========

    /**
     * @notice EventNFT, StakeholderNFT, TicketNFT, 플랫폼 지갑 주소 설정
     *
     * [왜 생성자에서 안 넣나?]
     * 배포 순서:
     *   ① StakeholderNFT 배포 → 주소 확보
     *   ② EventNFT 배포 → 주소 확보
     *   ③ TicketNFT 배포 → 주소 확보
     *   ④ Settlement 배포 (SSF 주소만 필요)
     *   ⑤ setContracts(EventNFT, StakeholderNFT, TicketNFT, 플랫폼) ← 여기서 설정
     *
     * Settlement 배포 시점에 다른 컨트랙트가 아직 배포 안 됐을 수 있음
     * → 유연성을 위해 나중에 설정할 수 있도록 setter로 분리
     */
    function setContracts(
        address _eventNFT,
        address _stakeholderNFT,
        address _ticketNFT,
        address _platformWallet
    ) external onlyOwner {
        // onlyOwner = 플랫폼(owner)만 호출 가능
        // 해커가 가짜 주소로 바꿀 수 없음
        require(_eventNFT != address(0), "Invalid EventNFT");
        require(_stakeholderNFT != address(0), "Invalid StakeholderNFT");
        require(_ticketNFT != address(0), "Invalid TicketNFT");
        require(_platformWallet != address(0), "Invalid platform wallet");
        eventNFTAddress = _eventNFT;             // storage에 영구 저장
        stakeholderNFTAddress = _stakeholderNFT;  // storage에 영구 저장
        ticketNFTAddress = _ticketNFT;            // storage에 영구 저장
        platformWallet = _platformWallet;          // storage에 영구 저장
    }

    /**
     * @notice PurchaseRouter 주소 설정
     *
     * recordDeposit()은 PurchaseRouter만 호출 가능
     * → 아무나 가짜 입금 기록을 남기지 못하게 제한
     *
     * [왜 onlyAuthorized가 아닌가?]
     * onlyAuthorized로 하면 owner(플랫폼)도 recordDeposit 호출 가능
     * → 플랫폼이 SSF 전송 없이 가짜 입금 기록을 만들 수 있음
     * → "플랫폼도 조작 불가"를 위해 PurchaseRouter만 허용
     */
    function setPurchaseRouter(address _purchaseRouter) external onlyOwner {
        require(_purchaseRouter != address(0), "Invalid address");
        purchaseRouter = _purchaseRouter;
    }

    // ========== 입금 기록 (구매 시 — PurchaseRouter만 호출) ==========

    /**
     * @notice 구매 시 입금 금액을 장부에 기록
     *
     * [호출 흐름]
     * PurchaseRouter.purchaseTicket() 내부:
     *   ❶ SSF.transferFrom(구매자 → Settlement)    ← SSF가 실제로 이동
     *   ❷ Settlement.recordDeposit(sessionId, price) ← 이 함수 (장부만 기록)
     *   ❸ TicketNFT.transferFrom(플랫폼 → 구매자)    ← NFT 소유권 이전
     *
     * SSF는 ❶에서 이미 이 컨트랙트에 도착한 상태
     * 이 함수에서는 sessionDeposits에 금액만 누적 기록 (역할 분리)
     *
     * [왜 PurchaseRouter만?]
     * 아무나 recordDeposit(0, 999999)를 호출하면
     * → 실제 SSF 없이 가짜 입금 기록이 생김
     * → 정산 시 없는 돈을 분배하려다 실패
     * → PurchaseRouter만 호출 가능하도록 제한
     *
     * PurchaseRouter는 반드시 SSF 전송 후에 recordDeposit 호출
     * → "돈을 보낸 후 장부에 기록" 순서가 보장됨
     * → 가짜 입금 기록 불가
     */
    function recordDeposit(uint256 sessionId, uint256 amount) external {
        require(msg.sender == purchaseRouter, "Only PurchaseRouter");
        // msg.sender = 이 함수를 호출한 주소
        // purchaseRouter가 아닌 누군가가 호출하면 revert
        // onlyOwner가 아닌 이유: owner(플랫폼)가 아니라 PurchaseRouter(컨트랙트)가 호출

        require(amount > 0, "Amount must be > 0");

        sessionDeposits[sessionId] += amount;
        // storage 직접 수정: 기존 누적액에 + amount
        // 예: 50,000 SSF 티켓 3장 → sessionDeposits = 150,000

        emit DepositRecorded(sessionId, amount, sessionDeposits[sessionId]);
        // 로그: "sessionId X에 amount SSF 입금, 누적 newTotal"
        // 백엔드 Event Listener가 감지 → DB 동기화
    }

    // ========== 환불 (원자적 — 1TX로 전부 처리) ==========

    /**
     * @notice 환불 — SSF 반환 + 티켓 REFUNDED + NFT 회수를 1TX로 처리
     *
     * [왜 원자적이어야 하는가?]
     * 분리하면 (3TX):
     *   ① Settlement.refund() → 돈 반환 ✅
     *   ② TicketNFT.reclaimTicket() → 상태 변경 실패 ❌
     *   → 돈은 돌려줬는데 티켓은 여전히 VALID
     *   → 구매자가 티켓으로 입장도 가능 = 부정
     *
     * 원자적이면 (1TX):
     *   ❶~❻ 전부 성공 or 전부 실패
     *   → "돈 받고 티켓도 가짐" 불가능
     *
     * [환불률 — on-chain에서 자동 계산]
     * 이전: 백엔드가 amount를 계산해서 전달 → 조작 가능
     * 변경: 컨트랙트가 EventNFT.getRefundRate()로 직접 계산 → 조작 불가
     *
     * 예:
     *   티켓 가격: 50,000 SSF
     *   공연까지 5일 → getRefundRate() → 5000 (50%)
     *   환불액 = 50,000 × 5000 / 10000 = 25,000 SSF
     *
     * [실행 순서]
     * ❶ TicketNFT에서 가격 on-chain 조회
     * ❷ EventNFT에서 회차 정보 조회 (공연 시각)
     * ❸ EventNFT에서 환불률 on-chain 계산
     * ❹ SSF 환불 (Settlement → 구매자)
     * ❺ 티켓 상태 변경 (VALID → REFUNDED)
     * ❻ NFT 회수 (구매자 → 플랫폼)
     * ❼ 보유 수 업데이트 (구매자 -1, 플랫폼 +1)
     * → 하나라도 실패 → 전부 revert
     *
     * [사전 조건]
     * 구매자가 TicketNFT.approve(Settlement주소, tokenId) 필요
     * → Custodial 구조에서 백엔드가 구매자 Keystore로 대리 서명
     *
     * TicketNFT.setAuthorizedCaller(Settlement주소, true) 필요
     * → Settlement이 reclaimTicket(), updateWalletTicketCount() 호출 가능
     *
     * @param sessionId 회차 ID
     * @param buyer 환불받을 구매자 지갑
     * @param tokenId TicketNFT tokenId
     */
    function refund(
        uint256 sessionId,
        address buyer,
        uint256 tokenId
    ) external onlyOwner {
        require(!sessionFinalized[sessionId], "Already finalized");
        // 이미 정산 완료 → SSF가 stakeholder에게 분배됨 → 환불 불가

        require(buyer != address(0), "Invalid buyer");
        require(eventNFTAddress != address(0), "EventNFT not set");
        require(ticketNFTAddress != address(0), "TicketNFT not set");

        ISettlementEventNFT eventNFT = ISettlementEventNFT(eventNFTAddress);
        // "eventNFTAddress에 있는 컨트랙트를 ISettlementEventNFT 인터페이스로 사용하겠다"
        ISettlementTicketNFT ticketNFT = ISettlementTicketNFT(ticketNFTAddress);

        // ========== ❶ 티켓 소유자 검증 ==========
        require(ticketNFT.ownerOf(tokenId) == buyer, "Buyer is not ticket owner");
        // 실제 NFT 소유자가 환불 요청자와 일치하는지 온체인 검증
        // → Escrow에 예치된 티켓 환불 시도 차단 (소유자가 Escrow이므로 revert)

        // ========== ❷ on-chain에서 티켓 가격 조회 ==========
        uint256 ticketPrice = ticketNFT.getPrice(tokenId);
        // TicketNFT.tickets[tokenId].price를 직접 읽음
        // 백엔드가 가격을 파라미터로 넘기지 않음 → 조작 불가

        (uint256 eventId, uint256 ticketSessionId, , , , , , , ) = ticketNFT.tickets(tokenId);
        // eventId를 가져와서 환불 정책 조회에 사용

        // 전달된 sessionId와 티켓의 실제 sessionId 일치 검증
        // 불일치 시 잘못된 session의 deposit이 차감되는 것 방지
        require(ticketSessionId == sessionId, "Session mismatch");

        // ========== ❷ on-chain에서 회차 정보 조회 ==========
        (, uint256 sessionTimestamp, , ) = eventNFT.getSession(sessionId);
        // sessionTimestamp = 공연 일시 (unix timestamp)
        // 환불률 계산에 사용 (공연까지 며칠 남았는지)

        // ========== ❸ on-chain에서 환불률 계산 ==========
        uint256 refundRateBps = eventNFT.getRefundRate(eventId, sessionTimestamp);
        // 현재 시각 기준으로 환불 비율 조회
        //
        // EventNFT에 저장된 환불 정책:
        //   [{14, 10000}, {7, 7000}, {3, 5000}, {1, 0}]
        //
        // 공연까지 10일 남음:
        //   10 >= 14? NO → 10 >= 7? YES → 7000 (70%) 반환
        //
        // 공연까지 0일:
        //   0 >= 14? NO → ... → 0 >= 1? NO → 0 (환불 불가)
        //
        // 백엔드가 이 값을 전달하는 게 아님
        // → 컨트랙트가 직접 on-chain에서 계산 → 조작 불가 (투명성)

        require(refundRateBps > 0, "Refund not available");
        // 환불률이 0이면 환불 불가 (공연 임박 또는 공연 종료)

        // ========== ❹ 환불액 계산 ==========
        uint256 refundAmount = (ticketPrice * refundRateBps) / 10000;
        // 예: 50,000 × 7000 / 10000 = 35,000 SSF (70% 환불)
        // 백엔드가 이 금액을 조작할 수 없음 (on-chain 계산)

        require(sessionDeposits[sessionId] >= refundAmount, "Insufficient deposits");
        // 환불액이 누적 입금액보다 클 수 없음

        // ========== ❺ SSF 환불 ==========
        sessionDeposits[sessionId] -= refundAmount;
        // storage 직접 수정: 누적액에서 환불액 차감

        bool success = ssfToken.transfer(buyer, refundAmount);
        // Settlement 컨트랙트 → 구매자 지갑으로 SSF 전송
        //
        // transfer vs transferFrom 차이:
        //   transfer(to, amount)
        //     → "내가(이 컨트랙트가) 내 잔고에서 to에게 보냄"
        //     → approve 불필요 (내 돈이니까)
        //
        //   transferFrom(from, to, amount)
        //     → "from의 잔고에서 to에게 보냄"
        //     → from이 approve 해줘야 함 (남의 돈이니까)
        //
        // 여기서는 Settlement 자기 잔고에서 보내는 거라 transfer 사용
        require(success, "SSF transfer failed");

        // ========== ❻ 티켓 상태 변경 (VALID → REFUNDED) ==========
        ticketNFT.reclaimTicket(tokenId, 3);
        // 3 = TicketStatus.REFUNDED
        // VALID에서만 변경 가능
        // → 이미 USED(입장함)이면 여기서 revert → 환불 전체 취소
        // → 입장한 티켓은 환불 불가 (원자적으로 보장)

        // ========== ❼ NFT 회수 (구매자 → 플랫폼) ==========
        ticketNFT.transferFrom(buyer, platformWallet, tokenId);
        // 구매자가 가지고 있던 NFT를 플랫폼 지갑으로 회수
        // ERC721 내부:
        //   _owners[tokenId] = platformWallet (소유자 변경)
        //   _balances[buyer] -= 1
        //   _balances[platformWallet] += 1
        //
        // 사전 조건: 구매자가 approve(Settlement, tokenId) 해놔야 함
        // → Custodial 구조에서 백엔드가 구매자 Keystore로 대리 서명

        // ========== ❽ 보유 수 업데이트 ==========
        ticketNFT.updateWalletTicketCount(eventId, sessionId, buyer, platformWallet);
        // walletTicketCount[e][s][구매자]-- (-1)
        // walletTicketCount[e][s][플랫폼]++ (+1)
        // → 구매자의 보유 수가 줄어서 다른 티켓 구매 가능해짐

        emit Refunded(sessionId, buyer, tokenId, refundAmount);
        // 로그: "회차 X에서 구매자에게 tokenId Y 환불, 35,000 SSF"
        // 백엔드 Event Listener → DB 업데이트 + Push 알림
    }

    // ========== 정산 (D+1 스케줄러) — on-chain에서 비율 직접 읽기 ==========

    /**
     * @notice 회차별 정산 — EventNFT/StakeholderNFT에서 비율 직접 조회
     *
     * [기획안 5.5 핵심]
     * "서버는 트리거만 할 뿐, 분배 비율은 StakeholderNFT에 고정"
     *
     * [실행 순서 — 기획안 7.5]
     * ① sessionDeposits[sessionId] 총액 확인 (예: 1,250,000 SSF)
     * ② EventNFT.getStakeholderTokenIds(eventId) → [0, 1, 2]
     * ③ StakeholderNFT.getStakeholder(0) → (주최자지갑, 7200 bps)
     *    StakeholderNFT.getStakeholder(1) → (아티스트지갑, 2000 bps)
     *    StakeholderNFT.getStakeholder(2) → (플랫폼지갑, 800 bps)
     * ④ 비율대로 SSF 분배:
     *    주최자:   1,250,000 × 7200 / 10000 = 900,000 SSF
     *    아티스트: 1,250,000 × 2000 / 10000 = 250,000 SSF
     *    플랫폼:   나머지 = 100,000 SSF (반올림 오차 방지)
     * ⑤ EventNFT.finalizeSession(sessionId) → isFinalized = true
     *
     * [반올림 오차 처리]
     * 1,250,001 SSF를 72:20:8로 나누면:
     *   정수 계산: 900,000 + 250,000 + 100,000 = 1,250,000 → 1 SSF 남음!
     * 해결: 마지막 stakeholder에게 나머지 전부 몰아줌
     *   distributed = 900,000 + 250,000 = 1,150,000
     *   마지막 share = 1,250,001 - 1,150,000 = 100,001
     *   → 1 SSF도 컨트랙트에 안 남음
     *
     * [호출 시점]
     * 공연 종료 D+1 새벽, 백엔드 스케줄러가 호출
     * 백엔드는 sessionId, eventId만 전달 → 나머지는 컨트랙트가 처리
     *
     * [장기 공연 예시]
     * 10/5 공연 → 10/6 새벽 정산 (10/5 회차분)
     * 10/6 공연 → 10/7 새벽 정산 (10/6 회차분)
     * → 회차별 독립 정산
     */
    function finalizeSession(
        uint256 sessionId,      // 어떤 회차를 정산?
        uint256 eventId         // 어떤 공연? (stakeholder 조회용)
    ) external onlyOwner returns (uint256) {

        // ========== 검증 ==========
        require(!sessionFinalized[sessionId], "Already finalized");
        // 이미 정산된 회차 → 다시 정산 불가 (이중 정산 방지)

        require(eventNFTAddress != address(0), "EventNFT not set");
        require(stakeholderNFTAddress != address(0), "StakeholderNFT not set");
        // setContracts()가 호출되지 않았으면 정산 불가

        uint256 totalAmount = sessionDeposits[sessionId];
        // storage에서 읽기: 이 회차의 총 누적 입금액
        require(totalAmount > 0, "No deposits");
        // 판매된 티켓이 없으면 정산할 것도 없음

        require(ssfToken.balanceOf(address(this)) >= totalAmount, "Insufficient SSF balance");
        // 실제 SSF 잔액 확인
        // balanceOf(address(this)) = 이 Settlement 컨트랙트가 보유한 SSF
        // address(this) = 이 컨트랙트 자신의 주소
        // 환불 등으로 실제 잔액이 장부와 다를 수 있으므로 방어적 체크

        // ========== on-chain에서 stakeholder 정보 읽기 ==========

        // EventNFT 컨트랙트 연결
        ISettlementEventNFT eventNFT = ISettlementEventNFT(eventNFTAddress);

        // 이 공연의 stakeholder 토큰 ID 목록 조회 (on-chain)
        uint256[] memory stakeholderIds = eventNFT.getStakeholderTokenIds(eventId);
        // memory = 함수 실행 중에만 존재하는 임시 배열
        // 예: [0, 1, 2] (StakeholderNFT tokenId 목록)
        // → 백엔드가 전달한 것이 아님! on-chain에서 직접 읽음!
        require(stakeholderIds.length > 0, "No stakeholders");

        // StakeholderNFT 컨트랙트 연결
        ISettlementStakeholderNFT stakeholderNFT = ISettlementStakeholderNFT(stakeholderNFTAddress);

        // bps 합계 검증 (10000 = 100%)
        // 공연 등록 시 이미 검증되었지만, 방어적으로 한번 더 확인
        uint256 totalBps = 0;
        for (uint256 i = 0; i < stakeholderIds.length; i++) {
            (, , uint256 shareBps, ) = stakeholderNFT.getStakeholder(stakeholderIds[i]);
            // StakeholderNFT에서 직접 읽음 (on-chain)
            // 백엔드가 전달한 게 아님 → 조작 불가
            // (wallet, role, shareBps, eventNftId) 중 shareBps만 사용
            totalBps += shareBps;
        }
        require(totalBps == 10000, "Total bps must be 10000");
        // 합계가 100%가 아니면 뭔가 잘못된 것 → revert

        // ========== 정산 완료 표시 ==========
        sessionFinalized[sessionId] = true;
        // storage 쓰기: true로 변경
        // 이 이후 다시 finalizeSession() 호출하면 첫 번째 require에서 revert
        // → 이중 정산 불가

        // ========== 정산 기록 생성 (on-chain 영구 기록) ==========
        uint256 settlementId = _nextSettlementId++;
        // 현재 값 사용 후 +1 (0, 1, 2, 3...)

        settlements[settlementId] = SettlementRecord({
            eventId: eventId,
            sessionId: sessionId,
            totalAmount: totalAmount,
            settledAt: block.timestamp     // 현재 블록 시각 (초 단위)
        });
        // on-chain에 영구 기록
        // 누구나 getSettlement(0)으로 "이 회차에서 얼마가 정산되었는지" 확인 가능

        // ========== SSF 분배 실행 ==========
        uint256 distributed = 0;
        // 지금까지 분배한 SSF 누적 (반올림 오차 처리용)

        for (uint256 i = 0; i < stakeholderIds.length; i++) {
            // StakeholderNFT에서 지갑 주소와 비율을 on-chain에서 직접 읽기
            (address wallet, , uint256 shareBps, ) =
                stakeholderNFT.getStakeholder(stakeholderIds[i]);

            uint256 share = (totalAmount * shareBps) / 10000;
            // 예: 1,250,000 × 7200 / 10000 = 900,000

            // 마지막 stakeholder = 나머지 전부 (반올림 오차 방지)
            if (i == stakeholderIds.length - 1) {
                share = totalAmount - distributed;
                // 예: 1,250,001 - 1,150,000 = 100,001
                // → 1 SSF도 컨트랙트에 안 남음
            }

            // 분배 금액 기록 (on-chain 영구 보존)
            distributions[settlementId][wallet] = share;
            // 누구나 getDistribution(0, 주최자지갑)으로 조회 가능
            // → "주최자가 실제로 900,000 SSF 받았는지" 투명하게 검증

            // SSF 전송: Settlement → stakeholder 지갑
            bool success = ssfToken.transfer(wallet, share);
            // transfer = 이 컨트랙트 잔고에서 직접 보냄 (approve 불필요)
            require(success, "SSF transfer failed");
            // 전송 실패하면 전체 TX revert
            // → 일부만 분배되고 나머지는 안 되는 일 없음 (원자적)

            emit Paid(settlementId, wallet, share, shareBps);
            // 로그: "정산 #0에서 주최자에게 900,000 SSF (72%) 분배"
            // 백엔드 Event Listener → DB 기록 + Push 알림

            distributed += share;
            // 누적: 900,000 → 1,150,000 → 1,250,001
        }

        // ========== EventNFT 정산 완료 표시 ==========
        eventNFT.finalizeSession(sessionId);
        // EventNFT.sessions[sessionId].isFinalized = true
        // → on-chain에 정산 완료 기록 (이중 정산 추가 방어)
        // → Settlement + EventNFT 양쪽 모두 정산 완료 표시

        emit SessionFinalized(settlementId, eventId, sessionId, totalAmount);
        // 로그: "정산 #0 완료: 공연 0, 회차 0, 총 1,250,000 SSF"

        return settlementId;
        // 정산 기록 ID 반환 (백엔드가 DB에 저장)
    }

    // ========== 조회 함수 (view = 가스비 0, 누구나 무료 호출) ==========

    /**
     * 회차별 누적 입금액 조회
     * 누구나 호출 가능 → "이 회차에서 얼마나 팔렸는지" 투명 공개
     */
    function getSessionDeposits(uint256 sessionId) external view returns (uint256) {
        return sessionDeposits[sessionId];
    }

    /**
     * 회차 정산 완료 여부 조회
     */
    function isSessionFinalized(uint256 sessionId) external view returns (bool) {
        return sessionFinalized[sessionId];
    }

    /**
     * 정산 기록 조회
     * → "이 회차에서 언제, 얼마가 정산되었는지" 확인
     */
    function getSettlement(uint256 settlementId) external view returns (
        uint256 eventId,
        uint256 sessionId,
        uint256 totalAmount,
        uint256 settledAt
    ) {
        SettlementRecord storage s = settlements[settlementId];
        // storage = 블록체인에서 직접 참조 (복사 안 함)
        return (s.eventId, s.sessionId, s.totalAmount, s.settledAt);
        // 반환: storage → memory/스택으로 복사
    }

    /**
     * 특정 정산에서 특정 stakeholder가 받은 금액 조회
     * → "주최자가 이 회차에서 실제로 얼마 받았는지" 누구나 검증 가능
     * → 이것이 CHEKET 투명성의 핵심
     */
    function getDistribution(uint256 settlementId, address wallet) external view returns (uint256) {
        return distributions[settlementId][wallet];
    }

    /**
     * 총 정산 건수 조회
     */
    function totalSettlements() external view returns (uint256) {
        return _nextSettlementId;
    }
}
