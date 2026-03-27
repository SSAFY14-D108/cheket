// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
// → ERC-721 NFT 표준 구현체
// → ownerOf(), transferFrom(), _safeMint() 등 제공
import "@openzeppelin/contracts/access/Ownable.sol";
// → onlyOwner modifier 제공
// → 배포한 사람(플랫폼 지갑)만 특정 함수 호출 가능

/**
 * @title StakeholderNFT (Soulbound ERC-721)
 * @notice 이해관계자의 수익 분배 권한을 나타내는 NFT
 *
 * [역할]
 * - 공연 수익을 누구에게, 몇 %씩 나눌지를 블록체인에 기록
 * - Soulbound = 한번 발행되면 전송 불가 (양도/거래 불가)
 * - 공연 등록 시(1단계) 즉시 발행 → 수익 비율 영구 확정
 *
 * [왜 Soulbound인가?]
 * 수익 분배 "권한증"이므로 거래되면 안 됨
 * 만약 전송 가능하다면:
 *   주최자(70%)가 NFT를 제3자에게 팔아버림
 *   → 계약 위반, 분쟁 발생
 * Soulbound로 만들면:
 *   발행된 지갑에 영구 고정 → 수익 구조 변경 불가
 *
 * [bps (Basis Points) = 만분율]
 * 10000 bps = 100%
 * 7000 bps = 70%
 * 800 bps = 8% (플랫폼 수수료)
 *
 * [예시]
 * Token #0: 주최자 0xAAA → 7200 bps (72%)
 * Token #1: 아티스트 0xBBB → 2000 bps (20%)
 * Token #2: 플랫폼 0xCCC → 800 bps (8%)
 * 합계: 10000 bps = 100%
 *
 * [Soulbound 구현 원리]
 * ERC-721 내부에서 NFT가 이동할 때 _update() 함수가 호출됨
 *   - mint:     from=0x0(zero address) → to=소유자   (새로 생성 → 허용) (mint는 무에서 생성)
 *   - transfer: from=A   → to=B        (전송 → 차단!)
 *   - burn:     from=A   → to=0x0(zero address)      (소각 → 허용)
 * → from과 to 모두 실제 주소면 = 전송이므로 revert
 */
contract StakeholderNFT is ERC721, Ownable {

    // ==================== storage ====================
    // 컨트랙트 상태 변수 = 블록체인에 영구 저장
    // 컨트랙트가 배포되는 순간부터 블록체인에 존재
    // 함수가 끝나도 사라지지 않음

    // 다음에 발행할 토큰 ID (0부터 시작, 발행할 때마다 +1)
    uint256 private _nextTokenId;

    // 이해관계자 정보 구조체
    // struct = 여러 변수를 하나로 묶은 사용자 정의 타입
    struct Stakeholder {
        address wallet;       // 이해관계자 지갑 주소
        string role;          // 역할 ("organizer", "artist")
        uint256 shareBps;     // 수익 분배 비율 (10000 = 100%)
        uint256 eventNftId;   // 연결된 EventNFT ID
    }

    // tokenId(uint256) → Stakeholder 정보 매핑
    // mapping = Solidity의 해시맵 (key → value)
    // 어떤 토큰이 어떤 이해관계자 정보를 가지는지 저장
    /**
     * public 이면 Solidity가 자동으로 getter를 만들어줌:
     */
    // stakeholders[토큰ID] = 해당 토큰의 이해관계자 정보를 저장하는 해시맵
    mapping(uint256 => Stakeholder) public stakeholders;

    // ========== 이벤트 ========== (별도의 로그 영역에 기록됨)
    // event = 블록체인 로그. 트랜잭션 실행 시 기록됨
    // 백엔드 Event Listener가 이 이벤트를 감지하여 DB 동기화
    // topics[0]은 이벤트 이름 해시로 자동 사용 → 남은 칸이 3개 → indexed 최대 3개
    /**
        ┌─────────────────────────────────────┐
        │ topics[] (최대 4칸)(검색 가능)        │
        │   [0] = 이벤트 종류 (자동, 고정)       │
        │   [1] = indexed 파라미터 #1          │
        │   [2] = indexed 파라미터 #2          │
        │   [3] = indexed 파라미터 #3          │
        │                                     │
        │ data (나머지 전부)                    │
        │   = indexed 아닌 파라미터들           │
        └─────────────────────────────────────┘
     */

    event StakeholderMinted(
        uint256 indexed tokenId, 
        address indexed wallet,
        string role,
        uint256 shareBps,
        uint256 eventNftId
    );

    // ========== 생성자 ==========
    // constructor = 컨트랙트 배포 시 1번만 실행되는 초기화 함수
    // ERC721("이름", "심볼") → NFT 컬렉션 이름과 티커 설정
    // Ownable(msg.sender) → 배포한 사람(= 플랫폼 지갑)이 owner

    constructor() ERC721("CHEKET Stakeholder", "CSTK") {}

    // ========== Soulbound: 전송 차단 ==========
    /**
     * @dev _update()는 ERC721 내부에서 mint/transfer/burn 시 자동 호출되는 훅(hook)
     * 이 함수는 우리가 직접 호출하는 게 아님 -> ERC721 내부에서 NFT가 이동할 때 자동으로 호출됨
     * override = 부모 컨트랙트(ERC721)의 함수를 덮어쓰기
     * NFT가 움직이는 "모든 경우"에 이 함수를 거침
     * internal = 컨트랙트 내부에서만 호출 가능 (외부에서 직접 호출 불가)
     *
     * 호출 케이스:
     *   mint:     from=0x0000  to=0xAAA  → 허용 ✅ (새로 생성)
     *   transfer: from=0xAAA   to=0xBBB  → 차단 ❌ (전송)
     *   burn:     from=0xAAA   to=0x0000 → 허용 ✅ (소각)
     */
    /**
     * @dev v4에서는 _beforeTokenTransfer()가 mint/transfer/burn 전에 호출되는 훅
     * _update()는 v5 전용이므로 v4에서는 이 함수를 사용
     *
     * 호출 케이스:
     *   mint:     from=0x0000  to=0xAAA  → 허용 ✅ (새로 생성)
     *   transfer: from=0xAAA   to=0xBBB  → 차단 ❌ (전송)
     *   burn:     from=0xAAA   to=0x0000 → 허용 ✅ (소각)
     */
    function _beforeTokenTransfer(
        address from,
        address to,
        uint256 tokenId,
        uint256 batchSize
    ) internal override {
        // from=실제주소 AND to=실제주소 → 전송이므로 차단
        if (from != address(0) && to != address(0)) {
            revert("Soulbound: transfer not allowed");
        }
        super._beforeTokenTransfer(from, to, tokenId, batchSize);
    }

    // ========== 민팅 함수 ==========

    /**
     * @notice 이해관계자 NFT 1개 발행
     *
     * external = 외부에서만 호출 가능 (가스비 절약)
     * onlyOwner = 플랫폼 서버(owner)만 호출 가능
     *   → 해커가 직접 호출하면 revert("Ownable: caller is not the owner")
     * returns (uint256) = 발행된 토큰 ID 반환
     *
     * calldata = 읽기 전용 파라미터 저장 위치
     *   - memory: 복사본 생성 (수정 가능, 가스비 더 높음)
     *   - calldata: 원본 참조 (수정 불가, 가스비 저렴)
     *   - external 함수의 string/array 파라미터에 사용
     */
    function mint(
        address to,           
        string calldata role, // 역할 (참조값이라서 calldata, 나머지는 스택)
        uint256 shareBps,     
        uint256 eventNftId    
    ) external onlyOwner returns (uint256) {

        // ---- 스택에서 읽기 ----
        require(to != address(0), "Invalid address");
        require(shareBps > 0 && shareBps <= 10000, "Invalid shareBps");

        // ---- storage 쓰기 (가장 비쌈) ----
        //  ① storage에서 _nextTokenId 읽음 (예: 0)
        //  ② tokenId에 0 대입 (스택)
        //  ③ storage의 _nextTokenId를 1로 업데이트
        uint256 tokenId = _nextTokenId++;  
        _safeMint(to, tokenId); // ERC721 표준 민팅 함수
        //  내부에서 storage 쓰기 발생:
        //  _owners[tokenId] = to     (NFT 소유자 기록)
        //  _balances[to] += 1        (보유 수 증가)

        // 토큰 ID에 이해관계자 정보 저장
        // ---- calldata → storage 저장 ----
        stakeholders[tokenId] = Stakeholder({
            wallet: to,            // 스택 → storage
            role: role,            // calldata → storage (가장 비싼 구간)
            shareBps: shareBps,    // 스택 → storage
            eventNftId: eventNftId // 스택 → storage
        });
        //  calldata의 "organizer" 문자열이
        //  블록체인(storage)에 영구 기록됨
        //  전 세계 노드에 복제됨 → 비쌈

        // ---- calldata에서 읽어서 로그 기록 ----
        // emit: "블록체인에 로그를 남겨라"(영수증): 해당 로그는 백엔드 Event Listener가 읽은 후 DB 동기화 
        emit StakeholderMinted(tokenId, to, role, shareBps, eventNftId);
        //  role은 여전히 calldata에서 읽음 (복사 아님)
        //  이벤트 로그에 기록됨 (storage보다 저렴)
        
        return tokenId; // 스택의 값 반환
    }

    // ========== 조회 함수 ==========
    // view = 블록체인 상태를 읽기만 함 (가스비 0, 무료 호출)
    // storage = 블록체인에 영구 저장된 데이터를 직접 참조 (복사 안 함)

   function getStakeholder(uint256 tokenId) external view returns (
        address wallet,
        string memory role, // 참조 타입 → memory (storage에서 복사해서 반환)
        uint256 shareBps,
        uint256 eventNftId
    ) {
        Stakeholder storage s = stakeholders[tokenId];
        //  storage 키워드 = 복사 안 함, 블록체인 데이터를 직접 가리킴

        return (s.wallet, s.role, s.shareBps, s.eventNftId);
        //  s.wallet    → storage에서 읽음 → 값 타입이라 스택으로 복사 (저렴)
        //  s.role      → storage에서 읽음 → string이라 memory로 복사 (비쌈)
        //  s.shareBps  → storage에서 읽음 → 값 타입이라 스택으로 복사 (저렴)
        //  s.eventNftId→ storage에서 읽음 → 값 타입이라 스택으로 복사 (저렴)
        //
        //  [왜 memory로 반환?]
        //  이건 TX에서 들어온 데이터가 아님 (calldata ❌)
        //  블록체인에서 꺼낸 데이터임 → memory에 복사해서 반환
    }

    function totalSupply() external view returns (uint256) {
        return _nextTokenId;
    }

    // ========== 지갑 주소 변경 ==========

    /**
     * @notice 이해관계자 지갑 주소 변경 (분실/교체 시)
     *
     * [왜 필요한가?]
     * Soulbound이라 토큰 이전 불가
     * → 지갑 키 분실 시 정산금이 접근 불가 지갑으로 영구 전송됨
     * → 플랫폼(owner)만 변경 가능하도록 제한 (신뢰 기반 운영)
     *
     * onlyOwner = 플랫폼만 호출 가능 (이해관계자 본인 신원 확인 후 처리)
     *
     * @param tokenId 변경할 StakeholderNFT tokenId
     * @param newWallet 새 지갑 주소
     */
    function updateWallet(uint256 tokenId, address newWallet) external onlyOwner {
        require(newWallet != address(0), "Invalid wallet address");
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        stakeholders[tokenId].wallet = newWallet;
    }
}
