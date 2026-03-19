// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
// → ERC-721 NFT 표준 구현체
// → ownerOf(), transferFrom(), _safeMint() 등 제공

import "@openzeppelin/contracts/access/Ownable.sol";
// → onlyOwner modifier 제공
// → 배포한 사람(플랫폼 지갑)만 특정 함수 호출 가능

/**
 * @title TicketNFT (기획안 반영)
 * @notice 공연 티켓을 나타내는 ERC-721 NFT
 *
 * [좌석 1개 = NFT 1개]
 * A구역 3열 15번 좌석 = tokenId 42번 NFT
 * 각 NFT에 구역/열/번호/등급/가격 정보가 저장됨
 * 
 * - getPrice(): PurchaseRouter가 가격 조회할 때 사용
 * - batchCheckIn(): 공연 후 입장 기록 배치 온체인 동기화
 * - setAuthorizedCaller(): PurchaseRouter에 함수 호출 권한 부여
 * - updateWalletTicketCount(): 구매 시 1인당 보유 수 업데이트
 * 
 * [소유권 흐름]
 * 배치 민팅 시: 플랫폼 지갑 소유 (재고)
 * 구매 시:     플랫폼 → 구매자 (PurchaseRouter.purchaseTicket)
 * 환불 시:     구매자 → 플랫폼 (백엔드가 transferFrom)
 * 리세일 시:   구매자 → Escrow → 새 구매자
 * 양도 시:     Marketplace.directTransfer(소유자→받는사람)
 * 
 * [배치 민팅 — 기획안 7.2]
 * 개별 발행 5,000 TX → 배치 50 TX (99% 감소)
 * 100개씩 묶어서 1 TX로 처리
 * SSAFY 네트워크 gasPrice=0이므로 비용 부담 없음
 */
contract TicketNFT is ERC721, Ownable {
    // is ERC721 = ERC721을 상속 (Java의 extends)
    // is Ownable = Ownable을 상속 (onlyOwner 사용 가능)
    // Solidity는 다중 상속 가능

    // ========== 상태 변수 (전부 storage = 블록체인 영구 저장) ==========

    /**
     * 다음에 발행할 토큰 ID
     * 0부터 시작, 민팅할 때마다 +1 (DB의 AUTO_INCREMENT와 동일)
     * private = 이 컨트랙트 내부에서만 접근 가능
     *
     * 민팅 시:
     *   tokenId = _nextTokenId++  → 현재 값 사용 후 +1
     *   1번째: tokenId=0, _nextTokenId→1
     *   2번째: tokenId=1, _nextTokenId→2
     *   → 외부에서 넣는 게 아니라 컨트랙트가 자동 발급
     */
    uint256 private _nextTokenId; // 외부 컨트랙트에서 접근 불가

    /**
     * 티켓 상태 열거형
     * enum = 정해진 값 중 하나만 가질 수 있는 타입
     * 내부적으로 uint8 (0, 1, 2, 3)으로 저장됨
     *
     * 상태 전이 규칙:
     *   VALID → USED     (입장 시: checkIn / batchCheckIn)
     *   VALID → EXPIRED  (공연 종료 후: reclaimTicket)
     *   VALID → REFUNDED (환불 시: reclaimTicket)
     *   USED/EXPIRED/REFUNDED → 변경 불가 (최종 상태)
     */
    enum TicketStatus {
        VALID,      // 0: 유효 (사용 가능)
        USED,       // 1: 사용됨 (입장 완료)
        EXPIRED,    // 2: 만료됨 (공연 종료 후 미사용)
        REFUNDED    // 3: 환불됨 (SSF 반환 완료)
    }

    // 각 NFT(tokenId)마다 이 구조체 1개가 storage에 저장됨
    struct TicketInfo {
        uint256 eventId;        // 이벤트 ID (EventNFT의 eventId)
        uint256 sessionId;      // 회차 ID (EventNFT의 sessionId)
        string section;         // 구역 (예: "A", "VIP")
        uint256 row;            // 열
        uint256 seat;           // 좌석 번호
        string grade;           // 등급 (예: "VIP", "R", "S", "A")
        uint256 price;          // 티켓 가격 (SSF, decimals=0이므로 정수)
        TicketStatus status;    // 티켓 상태
        uint256 mintedAt;       // 발행 시각 (unix timestamp, 초 단위)
    }

    /**
     * tokenId → TicketInfo 매핑
     * = Java의 HashMap<Long, TicketInfo>
     * = "토큰 ID를 넣으면 그 좌석의 정보가 나오는 해시맵"
     * storage에 영구 저장 (블록체인)
     *
     * tickets[0] = {eventId:0, section:"A", row:1, seat:15, price:50000, ...}
     * tickets[1] = {eventId:0, section:"A", row:1, seat:16, price:50000, ...}
     *
     * public이므로 Solidity가 자동 getter 생성:
     *   function tickets(uint256 tokenId) external view returns (...)
     *   → 누구나 무료로 티켓 정보 조회 가능 (투명성)
     */
    mapping(uint256 => TicketInfo) public tickets;

    /**
     * 3중 매핑: eventId → sessionId → 지갑주소 → 보유 수
     * = "이 공연, 이 회차에서 이 지갑이 몇 장 가지고 있는지"
     *
     * [왜 필요한가?]
     * EventNFT에 maxPerWallet = 4 (1인당 최대 4장)
     * 구매자가 5번째 장을 사려고 할 때:
     *   walletTicketCount[0][0][구매자] = 4 → 4 < 4 = false → 구매 불가!
     *
     * [ERC-721 표준에는 없는 커스텀 mapping]
     * ERC-721의 balanceOf()는 전체 NFT 보유 수만 알려줌
     * "이 공연의 이 회차에서 몇 장?"은 알 수 없음
     * → 우리가 직접 만듦
     *
     * 예: walletTicketCount[0][0][0xAAA] = 3
     *     → eventId 0, sessionId 0에서 0xAAA가 3장 보유
     */
    mapping(uint256 => mapping(uint256 => mapping(address => uint256))) public walletTicketCount;

    /**
     * 권한 부여된 컨트랙트 목록
     * = "onlyOwner가 아니어도 특정 함수를 호출할 수 있는 주소"
     *
     * [왜 필요한가?]
     * PurchaseRouter가 구매 처리 시 updateWalletTicketCount()를 호출해야 함
     * 하지만 PurchaseRouter는 owner가 아님
     * → authorizedCallers[PurchaseRouter주소] = true 로 등록
     * → onlyAuthorized modifier가 통과시켜줌
     *
     * authorizedCallers[PurchaseRouter주소] = true  → 호출 가능
     * authorizedCallers[해커주소] = false (기본값)   → 호출 불가
     */
    mapping(address => bool) public authorizedCallers;

    // ========== 이벤트 (블록체인 로그) ==========
    // emit으로 발생시키면 블록체인에 로그로 기록됨
    // 백엔드 Event Listener가 감지하여 DB 동기화
    // indexed = topics에 저장 → 필터링 가능 (최대 3개)

    // 티켓 민팅 시 발생
    event TicketMinted(
        uint256 indexed tokenId,    
        address indexed to,         
        uint256 eventId,            
        uint256 sessionId           
    );

    // 단건 입장 처리 시 발생 (holder: 소유자)
    event TicketCheckedIn(
        uint256 indexed tokenId,    
        address indexed holder      
    );

    // 환불/만료 시 발생
    // data: 새 상태 (EXPIRED or REFUNDED)
    event TicketReclaimed(
        uint256 indexed tokenId,    
        TicketStatus newStatus      
    );

    // 배치 입장 처리 시 발생
    // 배열(uint256[])은 topics에 넣을 수 없음 (고정 크기만 가능)
    // → indexed 없이 data에 저장
    event BatchCheckedIn(uint256[] tokenIds);

    // ========== modifier (함수 실행 전 검문소) ==========

    /**
     * onlyAuthorized = owner 또는 authorizedCallers만 호출 가능
     *
     * onlyOwner:      owner(플랫폼)만 통과
     * onlyAuthorized: owner + PurchaseRouter도 통과
     *
     * msg.sender = 이 함수를 호출한 주소
     * owner() = 컨트랙트 배포자 (플랫폼 지갑)
     *
     * _; = 검사 통과 시 함수 본문이 여기서 실행됨
     *
     * 비유:
     *   경비원이 출입문 앞에 서있음
     *   → 사원증(owner) 또는 방문증(authorizedCallers) 확인
     *   → 통과하면 문 열어줌 (_; = 함수 실행)
     *   → 아니면 돌려보냄 (revert)
     */
    modifier onlyAuthorized() {
        require(
            msg.sender == owner() || authorizedCallers[msg.sender],
            "Not authorized"
        );
        _;
    }

    // ========== 생성자 (배포 시 1번만 실행) ==========

    // ERC721("CHEKET Ticket", "CTKT")
    //   → NFT 컬렉션 이름: "CHEKET Ticket"
    //   → 심볼(티커): "CTKT"
    // Ownable(msg.sender)
    //   → 배포한 사람(플랫폼 지갑)이 owner
    constructor() ERC721("CHEKET Ticket", "CTKT") Ownable(msg.sender) {}


    // ========== 권한 관리 ==========

    /**
     * @notice 외부 컨트랙트에 함수 호출 권한 부여/해제
     *
     * [사용 시점] 배포 후 1회 호출
     *   ticketNFT.setAuthorizedCaller(PurchaseRouter주소, true)
     *   → PurchaseRouter가 updateWalletTicketCount() 호출 가능해짐
     *
     * [권한 해제]
     *   ticketNFT.setAuthorizedCaller(PurchaseRouter주소, false)
     *   → 다시 호출 불가
     *
     * onlyOwner = 플랫폼(owner)만 권한 관리 가능
     *             해커가 자기 자신을 등록할 수 없음
     */
     function setAuthorizedCaller(
        address caller,         // 권한을 부여할 컨트랙트 주소
        bool authorized         // true=부여, false=해제
    ) external onlyOwner {
        require(caller != address(0), "Invalid address");
        // address(0) = 0x0000...0000 (존재하지 않는 주소)
        // 실수로 빈 주소에 권한 부여하는 것 방지
        authorizedCallers[caller] = authorized;
        // storage에 영구 저장
    }

    // ========== 단건 발행 ==========

    /**
     * @notice 티켓 NFT 1장 발행
     *
     * [호출 주체] 백엔드(owner)가 플랫폼 개인키로 Raw TX 서명
     * [사용 시점] 테스트 또는 개별 좌석 추가 발행 시
     *
     * [실행 순서]
     * 1. _nextTokenId로 고유 ID 자동 할당 (0, 1, 2...)
     * 2. _safeMint로 NFT 발행 (to 지갑에 소유권 부여)
     * 3. tickets 매핑에 좌석 정보 저장 (calldata → storage)
     * 4. walletTicketCount 증가 (1인당 보유 수 추적)
     * 5. TicketMinted 이벤트 발생 (백엔드 Event Listener 감지용)
     */
    function mintTicket(
        address to,                  // 발행 대상 (보통 플랫폼 지갑) → 값 타입 → 스택
        uint256 eventId,             // EventNFT의 이벤트 ID → 값 타입 → 스택
        uint256 sessionId,           // EventNFT의 회차 ID → 값 타입 → 스택
        string calldata section,     // 구역 → 참조 타입 → calldata (TX에서 직접 읽음)
        uint256 row,                 // 열 → 값 타입 → 스택
        uint256 seat,                // 좌석 번호 → 값 타입 → 스택
        string calldata grade,       // 등급 → 참조 타입 → calldata
        uint256 price                // 가격(SSF) → 값 타입 → 스택
    ) external onlyOwner returns (uint256) {
        // external = 외부에서만 호출 (calldata 사용, 가스비 절약)
        // → calldata는 TX data를 복사 없이 참조
        // → 복사 안 하니까 가스비 절약

        // public 함수는 내부에서도 호출 가능해야 함
        // → 내부 호출 시에는 TX data(calldata)가 없음
        // → 그래서 파라미터를 memory에 복사해놔야 함
        // → 복사 비용 발생

        // onlyOwner = owner(플랫폼)만 호출 가능
        // returns (uint256) = 발행된 tokenId 반환
        uint256 tokenId = _nextTokenId++;
        /**
         * ① _nextTokenId를 storage에서 읽음 (예: 0)
         * ② 읽은 값 0을 스택에 있는 tokenId에 대입
         * ③ _nextTokenId를 storage에 1로 업데이트 (0→1)
         */
        // storage 읽기 + 쓰기
        // _nextTokenId가 0이면 → tokenId=0, _nextTokenId→1
        // _nextTokenId가 1이면 → tokenId=1, _nextTokenId→2

        _safeMint(to, tokenId);
        // ERC721 내부 함수: NFT 발행
        // storage 쓰기:
        //   _owners[tokenId] = to     (소유자 기록)
        //   _balances[to] += 1        (보유 수 증가)
        //
        // _safeMint vs _mint:
        //   _safeMint = 받는 주소가 컨트랙트면 onERC721Received 확인
        //               → 받을 수 있는 컨트랙트인지 안전 검사
        //   _mint = 검사 없이 바로 발행 (약간 더 저렴)

        // calldata → storage 저장 (블록체인에 영구 기록)
        // 가장 비싼 구간 (전 세계 노드에 복제됨)
        tickets[tokenId] = TicketInfo({
            eventId: eventId,
            sessionId: sessionId,
            section: section, // calldata에서 읽어서 storage에 저장
            row: row,
            seat: seat,
            grade: grade, // calldata에서 읽어서 storage에 저장
            price: price,
            status: TicketStatus.VALID, // 발행 시 항상 VALID
            mintedAt: block.timestamp // 현재 블록 시각 (unix timestamp, 초)
        });

        // 보유 수 증가 (1인당 구매 제한 체크용)
        walletTicketCount[eventId][sessionId][to]++;
        // storage 쓰기: 기존 값에 +1

        // 블록체인 로그 기록 → 백엔드 Event Listener가 감지
        emit TicketMinted(tokenId, to, eventId, sessionId);

        return tokenId;
        // 값 타입(uint256)이라 memory 명시 불필요
    }

    // ========== 일괄 발행 (기획안 7.2 배치 민팅) ==========

    /**
     * @notice 같은 구역/열의 연속 좌석 티켓 일괄 발행
     *
     * [사용 시점] 예매 오픈 D-1, 스케줄러가 자동 실행
     *
     * [기획안 7.2]
     * "개별 발행 (mintTicket × 5,000): TX 5,000개 → 수십 분
     *  배치 발행 (batchMintTickets, 100개씩): TX 50개 → 수 분"
     *  → TX 수 99% 감소
     *
     * [예시]
     * batchMintTickets(플랫폼, 0, 0, "A", 1, 1, 5, "VIP", 50000)
     * → A구역 1열 1번 좌석: tokenId 0
     * → A구역 1열 2번 좌석: tokenId 1
     * → A구역 1열 3번 좌석: tokenId 2
     * → A구역 1열 4번 좌석: tokenId 3
     * → A구역 1열 5번 좌석: tokenId 4
     * → 5장이 1 TX로 발행됨
     *
     * [주의]
     * 한번에 너무 많이 발행하면 block gas limit 초과
     * → 100개씩 나눠서 배치 발행
     * → 5,000석 = 50 TX
     *
     * @param to 발행 대상 (플랫폼 지갑)
     * @param startSeat 시작 좌석 번호
     * @param count 발행 수량
     * @return startTokenId 첫 번째 토큰 ID (이후 count개 연속)
     */
    function batchMintTickets(
        address to,
        uint256 eventId,
        uint256 sessionId,
        string calldata section,
        uint256 row,
        uint256 startSeat, // 시작 좌석 번호
        uint256 count, // 발행 수량
        string calldata grade,
        uint256 price
    ) external onlyOwner returns (uint256) {
        require(count > 0, "Count must be > 0");
        // require = 조건 불만족 시 TX 전체 revert (변경사항 취소)

        uint256 startTokenId = _nextTokenId;
        // 현재 _nextTokenId를 기억 (첫 번째 토큰 ID)
        // storage에서 1번만 읽음

        // count만큼 반복하여 민팅
        for (uint256 i = 0; i < count; i++) {
            uint256 tokenId = startTokenId + i;
            _safeMint(to, tokenId);

            tickets[tokenId] = TicketInfo({
                eventId: eventId,
                sessionId: sessionId,
                section: section,
                row: row,
                seat: startSeat + i, // 좌석 번호 자동 증가
                grade: grade,
                price: price,
                status: TicketStatus.VALID,
                mintedAt: block.timestamp
            });

            // 배치에서도 개별 이벤트 발행 → 백엔드 Event Listener가 DB 동기화 가능
            emit TicketMinted(tokenId, to, eventId, sessionId);
        }

        _nextTokenId = startTokenId + count;
        // 루프 안에서 _nextTokenId++를 안 하고 마지막에 한번에 더함
        // → storage 쓰기 1번으로 줄임 (가스비 절약)
        // 루프 안에서 매번 쓰면 count번 storage 쓰기 발생

        walletTicketCount[eventId][sessionId][to] += count;
        // 보유 수를 count만큼 한번에 증가
        // 루프 안에서 1씩 안 더함 → storage 쓰기 1번 (가스비 절약)

        return startTokenId;
        // 첫 번째 토큰 ID 반환
        // 백엔드: startTokenId ~ (startTokenId + count - 1) 로 전체 범위 파악

    }

    // ========== 가격 조회 (기획안 5.5 PurchaseRouter용) ==========

    /**
     * @notice 티켓 가격을 on-chain에서 직접 조회
     *
     * [누가 사용?]
     * PurchaseRouter.purchaseTicket() 내부에서:
     *   uint256 price = ticketNFT.getPrice(ticketId);
     *   ssf.transferFrom(buyer, settlement, price);
     *
     * [왜 on-chain에서 읽는가? — 기획안 핵심]
     * 백엔드가 가격을 파라미터로 넘기면:
     *   → 백엔드가 가격을 조작할 수 있음 (50000원 티켓을 100원으로)
     * 컨트랙트가 직접 읽으면:
     *   → TicketNFT.price는 MINTED 이후 변경 불가
     *   → 조작 불가능 = 투명성 보장
     *
     * view = 블록체인 상태를 읽기만 함 (가스비 0, 무료 호출)
     */
    function getPrice(uint256 tokenId) external view returns (uint256) {
        return tickets[tokenId].price;
        // storage에서 읽음 → uint256은 값 타입 → 스택으로 반환
    }

    // ========== 구매 시 walletTicketCount 업데이트 ==========

    /**
     * @notice 구매/환불 시 지갑별 보유 수 업데이트
     *
     * [왜 필요한가?]
     * ERC721의 transferFrom()은 _owners와 _balances만 변경함
     * walletTicketCount는 우리가 만든 커스텀 mapping이라
     * transferFrom()이 자동으로 업데이트하지 않음
     * → 구매/환불 시 이 함수로 수동 업데이트해야 함
     *
     * [호출 주체]
     * PurchaseRouter.purchaseTicket() 내부에서 호출
     *   → msg.sender = PurchaseRouter 주소
     *   → authorizedCallers[PurchaseRouter] = true
     *   → onlyAuthorized 통과 ✅
     *
     * [구매 시]
     * updateWalletTicketCount(eventId, sessionId, 플랫폼, 구매자)
     *   → walletTicketCount[e][s][플랫폼]-- (플랫폼 보유 -1)
     *   → walletTicketCount[e][s][구매자]++ (구매자 보유 +1)
     *
     * [환불 시]
     * updateWalletTicketCount(eventId, sessionId, 구매자, 플랫폼)
     *   → walletTicketCount[e][s][구매자]-- (구매자 보유 -1)
     *   → walletTicketCount[e][s][플랫폼]++ (플랫폼 보유 +1)
     */
    function updateWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address from,       // 보유 수 줄어드는 지갑
        address to          // 보유 수 늘어나는 지갑
    ) external onlyAuthorized {
        // onlyAuthorized: owner 또는 PurchaseRouter만 호출 가능

        // from이 실제 주소면 보유 수 감소
        if (from != address(0)) {
            walletTicketCount[eventId][sessionId][from]--;
        }

        // to가 실제 주소면 보유 수 증가
        if (to != address(0)) {
            walletTicketCount[eventId][sessionId][to]++;
        }

        // [address(0) 체크 이유]
        // 민팅 시 from = 0x0 (무에서 생성) → from 감소 안 함
        // 소각 시 to = 0x0 (소멸) → to 증가 안 함
        // 0x0 주소의 카운트를 건드리면 underflow/overflow 위험
    }


    // ========== 입장 처리 (단건) ==========

    /**
     * @notice QR 스캔 시 입장 처리 (VALID → USED)
     *
     * [하이브리드 입장 — 기획안 7.8]
     * 이 함수는 "단건 온체인 입장" (테스트용)
     * 실제 운영에서는:
     *   공연 당일 → 오프체인(DB) isUsed=true → 즉시 입장 (~50ms)
     *   공연 후 → batchCheckIn()으로 배치 온체인 기록
     */
    function checkIn(uint256 tokenId) external onlyOwner {
        require(ownerOf(tokenId) != address(0), "Ticket does not exist");
        // ownerOf = ERC721 함수: tokenId의 소유자 조회
        // 소유자가 0x0 = 발행되지 않은 토큰

        require(tickets[tokenId].status == TicketStatus.VALID, "Ticket not valid");
        // VALID가 아니면 입장 불가
        // USED(이미 입장), EXPIRED(만료), REFUNDED(환불) → 전부 차단

        tickets[tokenId].status = TicketStatus.USED;
        // storage 쓰기: VALID(0) → USED(1)

        emit TicketCheckedIn(tokenId, ownerOf(tokenId));
    }

    // ========== 입장 처리 (배치) — 기획안 7.8 핵심 ==========

    /**
     * @notice 공연 종료 후 입장 기록을 온체인에 배치 동기화
     *
     * [기획안 7.8 — 하이브리드 입장]
     * "입장 시점에는 오프체인 DB 검증으로 즉시 승인
     *  온체인 기록은 공연 후 배치 처리하여
     *  블록체인의 영구 기록 가치는 유지하면서 실시간 성능을 확보"
     *
     * [왜 입장 시 온체인 안 하나?]
     * 수천 명이 동시 입장 → 수천 개 TX → nonce 충돌, 네트워크 병목
     * 오프체인으로 즉시 처리 → 사용자 대기 없음
     * 공연 후 한번에 배치 TX → 온체인 기록은 유지
     *
     * [checkIn vs batchCheckIn 차이]
     * checkIn:      VALID 아니면 revert (TX 전체 실패)
     * batchCheckIn: VALID 아니면 건너뜀 (다른 티켓은 정상 처리)
     *   → 배치 중 1장이 이미 USED여도 나머지 999장은 처리됨
     *
     * [사용 시점]
     * 공연 종료 후 스케줄러:
     *   DB에서 isUsed=true인 ticketId 목록 조회
     *   → batchCheckIn([id1, id2, id3, ...])
     *   → 100개씩 나눠서 TX 전송
     */
     function batchCheckIn(uint256[] calldata tokenIds) external onlyOwner {
        // calldata = 배열도 참조 타입이므로 calldata 명시
        // TX data에서 직접 읽음 (memory로 복사 안 함, 가스비 절약)

        for (uint256 i = 0; i < tokenIds.length; i++) {
            uint256 tokenId = tokenIds[i];
            // 스택에 임시 저장 (값 타입)

            // VALID인 티켓만 USED로 변경, 나머지는 건너뜀
            if (tickets[tokenId].status == TicketStatus.VALID) {
                tickets[tokenId].status = TicketStatus.USED;
                // storage 쓰기: VALID(0) → USED(1)
            }
            // USED, EXPIRED, REFUNDED → if문 false → 아무것도 안 함
            // revert 안 함 → 일부 실패해도 나머지는 처리됨
        }

        emit BatchCheckedIn(tokenIds);
    }


        // ========== 환불/회수 ==========
    /**
     * @notice 티켓 상태를 REFUNDED 또는 EXPIRED로 변경
     *
     * [환불 흐름 — 원자적 처리 (Settlement.refund() 1TX)]
     * Settlement.refund() 내부에서 원자적으로 실행:
     *   ❶ on-chain 환불률 계산 (EventNFT.getRefundRate)
     *   ❷ SSF 환불 (Settlement → 구매자)
     *   ❸ TicketNFT.reclaimTicket(REFUNDED) ← 이 함수 (Settlement이 호출)
     *   ❹ TicketNFT.transferFrom(구매자 → 플랫폼) → NFT 회수
     *   ❺ walletTicketCount 업데이트
     *   → 하나라도 실패 → 전부 revert (원자적)
     *
     * [만료 흐름]
     * 공연 종료 후 미입장 티켓 → 백엔드(owner)가 reclaimTicket(tokenId, EXPIRED)
     *
     * [접근 제어]
     * onlyAuthorized = owner(플랫폼) + authorizedCallers(Settlement) 호출 가능
     *   - 환불 시: Settlement 컨트랙트가 호출 (authorizedCallers에 등록)
     *   - 만료 시: 백엔드(owner)가 직접 호출
     *
     * [보안]
     * VALID에서만 변경 가능
     * → USED(입장 후) → REFUNDED 같은 부정 전이 방지
     * → 입장한 티켓은 환불 불가
     */
    function reclaimTicket(
        uint256 tokenId,
        TicketStatus newStatus      // EXPIRED(2) 또는 REFUNDED(3)만 가능
    ) external onlyAuthorized {
        // onlyAuthorized: owner(플랫폼) 또는 Settlement(authorizedCallers) 호출 가능
        require(
            newStatus == TicketStatus.EXPIRED || newStatus == TicketStatus.REFUNDED,
            "Invalid status"
        );
        // USED(1)로 변경 시도 → revert (checkIn 함수를 써야 함)
        // VALID(0)로 변경 시도 → revert (되돌리기 불가)
        require(tickets[tokenId].status == TicketStatus.VALID, "Ticket not valid");
        // 이미 USED/EXPIRED/REFUNDED → 다시 변경 불가
        tickets[tokenId].status = newStatus;
        // storage 쓰기: VALID → EXPIRED or VALID → REFUNDED
        emit TicketReclaimed(tokenId, newStatus);
    }



    // ========== 조회 함수 (view = 가스비 0, 무료 호출) ==========

    /**
     * @notice 티켓 정보 전체 조회
     * returns (TicketInfo memory):
     *   struct는 참조 타입 → storage에서 memory로 복사해서 반환
     *   TX data에서 온 게 아니므로 calldata 아님 → memory
     */
    function getTicket(uint256 tokenId) external view returns (TicketInfo memory) {
        return tickets[tokenId];
    }

    /**
     * @notice 티켓 상태만 조회
     * returns (TicketStatus):
     *   enum은 내부적으로 uint8 → 값 타입 → memory 명시 불필요
     */
    function getTicketStatus(uint256 tokenId) external view returns (TicketStatus) {
        return tickets[tokenId].status;
    }

    /**
     * @notice 특정 지갑의 티켓 보유 수 조회
     *
     * 사용 예: 구매 전 maxPerWallet 체크
     *   count = getWalletTicketCount(0, 0, 구매자)
     *   require(count < maxPerWallet, "구매 한도 초과")
     */
    function getWalletTicketCount(
        uint256 eventId,
        uint256 sessionId,
        address wallet
    ) external view returns (uint256) {
        return walletTicketCount[eventId][sessionId][wallet];
    }

    /**
     * @notice 총 발행된 NFT 수 조회
     * 누구나 조회 가능 → 실제 발행 수량 투명하게 검증
     * 기획안: "누구나 totalSupply를 조회하여 실제 발행 수량을 독립 검증"
     */
    function totalSupply() external view returns (uint256) {
        return _nextTokenId;
    }
}