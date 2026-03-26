// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/access/Ownable.sol";
// → onlyOwner modifier 제공 (배포한 사람만 특정 함수 호출 가능)

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
// → ERC-20 토큰(SSF)의 인터페이스
// → transferFrom(), transfer(), balanceOf() 등 호출 가능

import "@openzeppelin/contracts/token/ERC721/IERC721.sol";
// → ERC-721 NFT(TicketNFT)의 인터페이스
// → transferFrom(), ownerOf() 등 호출 가능

/**
 * @title Escrow (리세일 전용 — 기획안 5.7, 7.6)
 * @notice 리세일 시 SSF와 TicketNFT를 동시에 예치하고 원자적으로 정산
 *
 * [1차 구매 vs 리세일 — 에스크로 역할 차이]
 * 1차 구매: PurchaseRouter → Settlement 직접 전송 (Escrow 안 거침)
 *           → 플랫폼이 NFT를 가지고 있으므로 신뢰 가능
 * 리세일:   Escrow가 NFT+SSF 중개 (개인 간 거래라 에스크로 필요)
 *           → 서로 모르는 사이라 제3자(Escrow)가 보관 후 교환
 *
 * [에스크로(Escrow)란?]
 * 거래 중개소. 돈+물건을 제3자가 보관하다가 조건 충족 시 교환.
 *
 * 비유: 당근마켓 안전결제
 *   구매자 돈 → 에스크로 보관 → 물건 수령 확인 → 판매자에게 송금
 *   → "돈 먼저 보내면 물건 안 보내면?" 사기 방지
 *
 * 우리 시스템:
 *   판매자 TicketNFT → Escrow에 예치
 *   구매자 SSF 지불 → 즉시 정산 (SSF→판매자, NFT→구매자)
 *   → "NFT 보내고 돈 안 받으면?" 사기 방지
 *   → 원자적 교환으로 부분 실패 불가
 *
 * [리세일 가격 상한 — 기획안 5.6]
 * "resaleCapBps = 10000 (정가 100%) → 정가 이하만 리세일 가능"
 * → 암표 방지: 원가보다 비싸게 팔 수 없음
 * → 컨트랙트 레벨 강제 (우회 불가)
 *
 * [리세일 수익 — 기획안 5.8]
 * "판매 대금 전액 → 판매자 (플랫폼 수수료 없음)
 *  정가 이하 양도만 허용 → 초과 수익 발생 불가
 *  → Stakeholder 추가 분배 불필요"
 *
 * [흐름 — 기획안 7.6]
 * ① 판매자: TicketNFT → Escrow에 예치 (createDeal)
 * ② 구매자: SSF 지불 → 즉시 정산 (buyAndSettle)
 *    → SSF → 판매자 전액, NFT → 구매자 (원자적)
 * ③ 취소: 판매자가 취소 → NFT 반환 (cancelDeal)
 * ④ 만료: deadline 초과 → NFT 판매자 반환 (refundExpiredDeal)
 *
 * [deadline — 기획안 5.7]
 * "deadline 초과 시 자동 환불"
 * → 구매자가 안 나타나도 판매자가 deadline 후 NFT 회수 가능
 * → 교착 상태 방지
 *
 * [사전 조건 — 리세일 등록 시]
 * 판매자가 TicketNFT.approve(Escrow주소, ticketId) 필요
 * → Custodial 구조에서 백엔드가 판매자 Keystore로 대리 서명
 *
 * [사전 조건 — 리세일 구매 시]
 * 구매자가 SSF.approve(Escrow주소, ssfAmount) 필요
 * → Custodial 구조에서 백엔드가 구매자 Keystore로 대리 서명
 */
contract Escrow is Ownable {

    // ========== 상태 변수 (storage = 블록체인 영구 저장) ==========

    // SSF 토큰 컨트랙트
    IERC20 public ssfToken;
    // TicketNFT 컨트랙트
    IERC721 public ticketNFT;

    // ========== 거래(Deal) 구조체 ==========

    /**
     * Deal = 하나의 리세일 거래 정보
     *
     * 판매자가 TicketNFT를 Escrow에 예치하면 Deal 생성 (AWAITING_BUYER)
     * 구매자가 SSF를 지불하면 즉시 정산 (SETTLED)
     * 판매자가 취소하면 NFT 반환 (CANCELLED)
     * deadline 초과하면 NFT 반환 (EXPIRED)
     */
    struct Deal {
        address seller;         // 판매자 지갑
        address buyer;          // 구매자 지갑 (구매 전에는 address(0) = 비어있음)
        uint256 ticketId;       // 예치된 TicketNFT tokenId
        uint256 ssfAmount;      // 리세일 가격 (SSF)
        uint256 originalPrice;  // 원가 (가격 상한 검증용)
        uint256 deadline;       // 거래 만료 시각 (unix timestamp)
        DealStatus status;      // 거래 상태
    }

    /**
     * 거래 상태 열거형
     *
     * [상태 전이]
     * AWAITING_BUYER → SETTLED    (구매자가 구매 완료)
     * AWAITING_BUYER → CANCELLED  (판매자가 취소)
     * AWAITING_BUYER → EXPIRED    (deadline 초과)
     * SETTLED/CANCELLED/EXPIRED → 변경 불가 (최종 상태)
     */
    enum DealStatus {
        AWAITING_BUYER,     // 0: 판매자가 NFT 예치 완료, 구매자 대기 중
        SETTLED,            // 1: 정산 완료 (SSF→판매자, NFT→구매자)
        CANCELLED,          // 2: 판매자가 취소 (NFT 반환)
        EXPIRED             // 3: deadline 초과 (NFT 반환)
    }

    // 다음 거래 ID (0부터 자동 증가)
    uint256 private _nextDealId;

    // dealId → Deal 정보
    // deals[0] = 첫 번째 리세일 거래 정보
    mapping(uint256 => Deal) public deals;

    // ticketId → dealId (같은 티켓 중복 등록 방지)
    // "이 티켓이 현재 어떤 Deal에 연결되어 있는지"
    mapping(uint256 => uint256) public ticketDeal;

    // ticketId → 현재 Escrow에 예치 중인지
    // true = 이 티켓이 Escrow 컨트랙트에 보관 중
    // false = Escrow에 없음 (판매자 또는 구매자가 보유)
    mapping(uint256 => bool) public isEscrowed;

    // ========== 이벤트 (블록체인 로그) ==========
    // emit으로 발생 → 백엔드 Event Listener가 감지 → DB 동기화

    // 리세일 등록 시 발생
    // topics[1]: 거래 ID
    // topics[2]: 판매자
    // topics[3]: 어떤 티켓?
    // data: 리세일 가격
    // data: 만료 시각
    event DealCreated(
        uint256 indexed dealId,         
        address indexed seller,         
        uint256 indexed ticketId,       
        uint256 ssfAmount,              
        uint256 deadline                
    );

    // 리세일 정산 완료 시 발생
    // topics[1]: 거래 ID
    // topics[2]: 구매자
    // topics[3]: 판매자
    // data: 티켓 ID
    // data: 거래 금액
    event DealSettled(
        uint256 indexed dealId,         
        address indexed buyer,          
        address indexed seller,         
        uint256 ticketId,               
        uint256 ssfAmount               
    );

    // 리세일 취소 시 발생
    // topics[1]: 거래 ID
    // topics[2]: 티켓 ID
    event DealCancelled(
        uint256 indexed dealId,         
        uint256 indexed ticketId        
    );

    // 만료 환불 시 발생
    // topics[1]: 거래 ID
    // topics[2]: 구매자 (없으면 0x0)
    // topics[3]: 판매자
    // data: 티켓 ID
    event DealExpiredRefund(
        uint256 indexed dealId,         
        address indexed buyer,          
        address indexed seller,         
        uint256 ticketId                
    );

    // ========== 생성자 (배포 시 1번만 실행) ==========

    /**
     * @param _ssfToken SSF 토큰 컨트랙트 주소
     * @param _ticketNFT TicketNFT 컨트랙트 주소
     *
     * Escrow는 SSF와 TicketNFT 둘 다 다루므로
     * 생성자에서 두 컨트랙트 주소를 모두 받음
     *
     * [왜 Settlement처럼 setContracts()로 안 하나?]
     * SSF와 TicketNFT는 Escrow보다 먼저 배포됨
     * → 배포 시점에 주소를 이미 알고 있음
     * → 생성자에서 바로 설정 가능
     */
    constructor(
        address _ssfToken,
        address _ticketNFT
    ) {
        // v4: Ownable() 자동으로 msg.sender가 owner
        require(_ssfToken != address(0), "Invalid SSF");
        require(_ticketNFT != address(0), "Invalid TicketNFT");
        ssfToken = IERC20(_ssfToken);
        // "이 주소의 컨트랙트를 ERC-20으로 취급"
        // → ssfToken.transfer(), ssfToken.transferFrom() 호출 가능
        ticketNFT = IERC721(_ticketNFT);
        // "이 주소의 컨트랙트를 ERC-721로 취급"
        // → ticketNFT.transferFrom(), ticketNFT.ownerOf() 호출 가능
    }

    // ========== 리세일 등록 (판매자 → NFT 예치) ==========

    /**
     * @notice 판매자가 TicketNFT를 Escrow에 예치하고 리세일 등록
     *
     * [기획안 7.6]
     * "판매자: 리세일 등록
     *  → Marketplace.listTicket(ticketId, resalePrice)
     *  → require(resalePrice <= originalPrice) — 컨트랙트 검증
     *  → TicketNFT → Escrow 선예치"
     *
     * [가격 상한 검증 — 기획안 5.6]
     * resaleCapBps = 10000 → 원가의 100%까지 (원가 이하로만)
     * 예: 원가 50,000, resaleCapBps 10000
     *     → maxPrice = 50,000 × 10000 / 10000 = 50,000
     *     → 50,000 이하로만 등록 가능
     *     → 50,001으로 등록 시도 → revert
     * → 암표 방지: 컨트랙트 레벨 강제 (우회 불가)
     *
     * [사전 조건]
     * 판매자가 TicketNFT.approve(Escrow주소, ticketId) 필요
     * → "Escrow가 내 NFT를 가져가도 됨"
     * → Custodial 구조에서 백엔드가 판매자 Keystore로 대리 서명
     *
     * [왜 NFT를 Escrow에 예치하는가?]
     * 판매자가 NFT를 가지고 있으면:
     *   → 리세일 등록 후 다른 사람에게 양도할 수 있음
     *   → 구매자가 돈을 냈는데 NFT가 없음 = 사기
     * Escrow에 예치하면:
     *   → 판매자도 NFT를 못 건드림
     *   → 구매자가 돈을 내면 확실하게 NFT를 받을 수 있음
     *
     * @param seller 판매자 지갑
     * @param ticketId TicketNFT tokenId
     * @param ssfAmount 리세일 희망 가격 (SSF)
     * @param originalPrice 원가 (TicketNFT.price, 가격 상한 기준)
     * @param resaleCapBps 가격 상한 (EventNFT.resaleCapBps)
     * @param deadline 거래 만료 시각 (unix timestamp)
     * @return dealId 생성된 거래 ID
     */
    function createDeal(
        address seller,
        uint256 ticketId,
        uint256 ssfAmount,
        uint256 originalPrice,
        uint256 resaleCapBps,
        uint256 deadline
    ) external onlyOwner returns (uint256) {
        // onlyOwner = 플랫폼(백엔드)만 호출 가능
        require(seller != address(0), "Invalid seller");
        require(!isEscrowed[ticketId], "Already escrowed");
        // 같은 티켓이 이미 Escrow에 있으면 중복 등록 방지
        require(ssfAmount > 0, "Price must be > 0");
        require(deadline > block.timestamp, "Deadline must be future");
        // deadline이 현재보다 과거이면 안 됨
        // block.timestamp = 현재 블록 시각

        // ========== 가격 상한 검증 (컨트랙트 레벨 강제) ==========

        uint256 maxPrice = (originalPrice * resaleCapBps) / 10000;
        // 예: 50,000 × 10000 / 10000 = 50,000 (원가 이하)
        // 예: 50,000 × 5000 / 10000 = 25,000 (원가 50% 이하)
        require(ssfAmount <= maxPrice, "Exceeds resale cap");
        // 희망 가격이 상한을 초과하면 revert
        // → 암표 방지: "정가 이상으로 팔 수 없음"

        // ========== TicketNFT를 Escrow에 예치 ==========

        ticketNFT.transferFrom(seller, address(this), ticketId);
        // seller → Escrow 컨트랙트로 NFT 전송
        // address(this) = 이 Escrow 컨트랙트 자신의 주소
        //
        // ERC721 내부:
        //   _owners[ticketId] = Escrow주소 (소유자 변경)
        //   _balances[seller] -= 1
        //   _balances[Escrow] += 1
        //
        // 사전 조건: 판매자가 approve(Escrow, ticketId) 해놔야 함
        // → Custodial: 백엔드가 판매자 Keystore로 대리 서명

        // ========== Deal 생성 ==========

        uint256 dealId = _nextDealId++;
        // 현재 값 사용 후 +1 (0, 1, 2, 3...)

        deals[dealId] = Deal({
            seller: seller,
            buyer: address(0),        // 아직 구매자 없음 (비어있음)
            ticketId: ticketId,
            ssfAmount: ssfAmount,
            originalPrice: originalPrice,
            deadline: deadline,
            status: DealStatus.AWAITING_BUYER
            // 구매자 대기 상태로 시작
        });

        ticketDeal[ticketId] = dealId;
        // "이 티켓은 현재 dealId번 거래에 연결되어 있다"
        // → 같은 티켓으로 다른 Deal 생성 방지

        isEscrowed[ticketId] = true;
        // "이 티켓은 현재 Escrow에 보관 중이다"

        emit DealCreated(dealId, seller, ticketId, ssfAmount, deadline);
        // 로그: "거래 #0 생성: 판매자 0xAAA, 티켓 #42, 가격 45,000 SSF"
        // 백엔드 Event Listener → Resale DB 레코드 생성 (ACTIVE)

        return dealId;
    }

    // ========== 리세일 구매 (구매자 → SSF 지불 + 즉시 정산) ==========

    /**
     * @notice 구매자가 SSF를 지불하고 즉시 정산 (원자적)
     *
     * [기획안 7.6]
     * "구매자: 마켓에서 구매
     *  → SSF → Escrow 예치
     *  → Escrow._settle()
     *      SSF → 판매자 전액 지급
     *      TicketNFT → 구매자 소유권 이전"
     *
     * [원자적 정산]
     * SSF 전송과 NFT 이전이 하나의 TX에서 실행
     * 하나라도 실패 → 전부 revert
     * → "돈만 내고 티켓 못 받음" 불가능
     * → "티켓만 받고 돈 안 냄" 불가능
     *
     * [리세일 수익 분배 — 기획안 5.8]
     * "판매 대금 전액 → 판매자 (플랫폼 수수료 없음)"
     * → 정가 이하 양도이므로 판매자에게 추가 수수료를 부과하지 않음
     * → SSF가 판매자에게 직접 전송 (Escrow를 거치지 않음)
     *
     * [사전 조건]
     * 구매자가 SSF.approve(Escrow주소, ssfAmount) 필요
     * → "Escrow가 내 SSF를 가져가도 됨"
     * → Custodial: 백엔드가 구매자 Keystore로 대리 서명
     *
     * @param buyer 구매자 지갑
     * @param dealId 거래 ID
     */
    function buyAndSettle(
        address buyer,
        uint256 dealId
    ) external onlyOwner {
        Deal storage deal = deals[dealId];
        // storage = 블록체인에서 직접 참조 (복사 안 함)
        // → deal.status 등을 변경하면 바로 storage에 반영됨

        require(deal.status == DealStatus.AWAITING_BUYER, "Not awaiting buyer");
        // AWAITING_BUYER가 아니면 구매 불가
        // → SETTLED(이미 판매됨), CANCELLED(취소됨), EXPIRED(만료됨) 전부 차단

        require(buyer != deal.seller, "Cannot buy own ticket");
        // 자기 자신의 티켓을 사는 것 방지 (의미 없는 거래)

        require(block.timestamp <= deal.deadline, "Deal expired");
        // deadline 초과 → 구매 불가
        // → refundExpiredDeal()로 NFT 회수해야 함

        // ========== 상태 변경 (정산 완료) ==========

        deal.buyer = buyer;
        // 구매자 기록 (address(0) → 실제 구매자 주소)
        deal.status = DealStatus.SETTLED;
        // AWAITING_BUYER → SETTLED (정산 완료)
        isEscrowed[deal.ticketId] = false;
        // 이 티켓은 더 이상 Escrow에 없음

        // ========== ❶ SSF: 구매자 → 판매자 직접 전송 (전액, 수수료 없음) ==========

        bool success = ssfToken.transferFrom(buyer, deal.seller, deal.ssfAmount);
        // transferFrom(from, to, amount)
        //   from: buyer (구매자)
        //   to: deal.seller (판매자)
        //   amount: deal.ssfAmount (리세일 가격)
        //
        // SSF가 Escrow를 거치지 않고 구매자 → 판매자로 직접 전송
        // → Escrow에 돈이 잠기지 않음 (즉시 정산)
        //
        // [기획안 5.8]
        // "판매 대금 전액 → 판매자 (플랫폼 수수료 없음)"
        //
        // 사전 조건: 구매자가 SSF.approve(Escrow, ssfAmount) 해놔야 함
        require(success, "SSF transfer failed");

        // ========== ❷ TicketNFT: Escrow → 구매자 ==========

        ticketNFT.transferFrom(address(this), buyer, deal.ticketId);
        // Escrow 컨트랙트 → 구매자로 NFT 전송
        // address(this) = 이 Escrow 컨트랙트 (현재 NFT 소유자)
        //
        // ERC721 내부:
        //   _owners[ticketId] = buyer (소유자: Escrow → 구매자)
        //   _balances[Escrow] -= 1
        //   _balances[buyer] += 1
        //
        // Escrow가 NFT 소유자이므로 approve 불필요 (자기 NFT를 보내는 것)

        // ========== 원자적 보장 ==========
        // ❶ 또는 ❷ 중 하나라도 실패 → 전부 revert
        // → 상태 변경(SETTLED)도 취소됨
        // → "돈은 냈는데 티켓 못 받음" 불가능

        emit DealSettled(dealId, buyer, deal.seller, deal.ticketId, deal.ssfAmount);
        // 로그: "거래 #0 정산: 구매자 0xBBB, 판매자 0xAAA, 티켓 #42, 45,000 SSF"
        // 백엔드 Event Listener:
        //   → Resale.status = SOLD
        //   → 양측 Push 알림: "리세일 거래 완료!"
    }

    // ========== 등록 취소 (판매자가 취소) ==========

    /**
     * @notice 리세일 등록 취소 — TicketNFT 판매자에게 반환
     *
     * [언제 취소하는가?]
     * → 판매자가 마음이 바뀌어서 리세일을 취소하고 싶을 때
     * → 가격을 변경하고 싶을 때 (취소 후 새로 등록)
     *
     * [조건]
     * → AWAITING_BUYER 상태에서만 취소 가능
     * → 이미 구매자가 있으면 (SETTLED) 취소 불가
     *
     * @param dealId 거래 ID
     */
    function cancelDeal(uint256 dealId) external onlyOwner {
        Deal storage deal = deals[dealId];

        require(deal.status == DealStatus.AWAITING_BUYER, "Not awaiting buyer");
        // 구매자 대기 중일 때만 취소 가능
        // SETTLED/CANCELLED/EXPIRED → 이미 끝난 거래 → 취소 불가

        deal.status = DealStatus.CANCELLED;
        // AWAITING_BUYER → CANCELLED
        isEscrowed[deal.ticketId] = false;
        // 이 티켓은 더 이상 Escrow에 없음

        // TicketNFT: Escrow → 판매자 반환
        ticketNFT.transferFrom(address(this), deal.seller, deal.ticketId);
        // Escrow가 NFT 소유자이므로 approve 불필요

        emit DealCancelled(dealId, deal.ticketId);
        // 로그: "거래 #0 취소: 티켓 #42 판매자에게 반환"
        // 백엔드 Event Listener:
        //   → Resale.status = CANCELLED
        //   → 판매자 Push 알림: "리세일 등록이 취소되었습니다"
    }

    // ========== 만료 환불 (deadline 초과) ==========

    /**
     * @notice deadline 초과 시 NFT를 판매자에게 반환
     *
     * [기획안 5.7]
     * "deadline 초과 시 자동 환불"
     *
     * [왜 deadline이 필요한가?]
     * 판매자가 NFT를 Escrow에 예치했는데 구매자가 안 나타남
     * → NFT가 Escrow에 영원히 잠김 = 교착 상태
     * → deadline 지나면 판매자가 NFT를 회수할 수 있음
     *
     * [누가 호출하는가?]
     * 백엔드 스케줄러가 주기적으로 만료된 Deal을 조회하여 호출
     * 또는 판매자가 앱에서 "만료된 리세일 회수" 버튼 클릭
     *
     * [AWAITING_BUYER 상태에서만]
     * 구매자 없이 deadline 초과한 경우
     * → NFT만 판매자에게 반환 (SSF 없으니 SSF 환불 없음)
     *
     * @param dealId 거래 ID
     */
    function refundExpiredDeal(uint256 dealId) external onlyOwner {
        Deal storage deal = deals[dealId];

        require(deal.status == DealStatus.AWAITING_BUYER, "Not awaiting buyer");
        // 구매자 대기 중인 상태에서만 만료 처리 가능

        require(block.timestamp > deal.deadline, "Not expired yet");
        // 현재 시각이 deadline을 지나야 함
        // deadline 전이면 revert → 아직 구매 가능한 상태

        deal.status = DealStatus.EXPIRED;
        // AWAITING_BUYER → EXPIRED
        isEscrowed[deal.ticketId] = false;
        // 이 티켓은 더 이상 Escrow에 없음

        // TicketNFT: Escrow → 판매자 반환
        ticketNFT.transferFrom(address(this), deal.seller, deal.ticketId);
        // 판매자에게 NFT 돌려줌

        emit DealExpiredRefund(dealId, deal.buyer, deal.seller, deal.ticketId);
        // 로그: "거래 #0 만료: 티켓 #42 판매자에게 반환"
        // deal.buyer = address(0) (구매자 없었으므로)
        // 백엔드 Event Listener:
        //   → Resale.status = EXPIRED
        //   → 판매자 Push 알림: "리세일 등록이 만료되었습니다"
    }

    // ========== 조회 함수 (view = 가스비 0, 누구나 무료 호출) ==========

    /**
     * @notice 거래 정보 조회
     * 누구나 호출 가능 → 리세일 거래 내역 투명하게 공개
     */
    function getDeal(uint256 dealId) external view returns (
        address seller,
        address buyer,
        uint256 ticketId,
        uint256 ssfAmount,
        uint256 originalPrice,
        uint256 deadline,
        DealStatus status
    ) {
        Deal storage d = deals[dealId];
        // storage = 블록체인에서 직접 참조 (복사 안 함)
        return (d.seller, d.buyer, d.ticketId, d.ssfAmount,
                d.originalPrice, d.deadline, d.status);
        // 반환: storage → memory/스택으로 복사
    }

    /**
     * @notice 티켓에 연결된 거래 ID 조회
     * "이 티켓이 현재 어떤 리세일 거래에 등록되어 있는지"
     */
    function getDealByTicket(uint256 ticketId) external view returns (uint256) {
        return ticketDeal[ticketId];
    }

    /**
     * @notice 총 거래 건수 조회
     */
    function totalDeals() external view returns (uint256) {
        return _nextDealId;
    }
}