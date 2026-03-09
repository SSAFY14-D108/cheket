// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

// OpenZeppelin ERC-721 기본 구현체 (NFT 표준 기능 제공)
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
// 토큰 ID별 메타데이터 URI를 개별 설정할 수 있는 확장
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
// 소유자만 특정 함수를 실행할 수 있도록 제한하는 접근 제어
import "@openzeppelin/contracts/access/Ownable.sol";

contract TicketNFT is ERC721, ERC721URIStorage, Ownable {

    // ========== 상태 변수 ==========

    // 다음에 발행될 토큰의 ID (0부터 시작, 발행할 때마다 1씩 증가)
    uint256 private _nextTokenId;

    // 티켓 정보를 담는 구조체
    struct TicketInfo {
        uint256 showId;      // 공연 ID (백엔드 DB의 show 테이블과 매핑)
        uint256 sessionId;   // 회차 ID (백엔드 DB의 session 테이블과 매핑)
        string seatInfo;     // 좌석 정보 (예: "A구역 3열 15번")
        uint256 price;       // 티켓 가격 (wei 단위)
        bool isUsed;         // 입장 사용 여부 (true면 이미 사용된 티켓)
    }

    // tokenId => TicketInfo 매핑 (각 NFT의 티켓 정보 저장)
    mapping(uint256 => TicketInfo) public tickets;

    // ========== 이벤트 ==========

    // 티켓이 발행(민팅)되었을 때 발생하는 이벤트
    event TicketMinted(uint256 indexed tokenId, address indexed to, uint256 showId, uint256 sessionId);

    // 티켓이 사용(입장)되었을 때 발생하는 이벤트
    event TicketUsed(uint256 indexed tokenId, address indexed user);

    // ========== 생성자 ==========

    // 컨트랙트 배포 시 실행 (NFT 이름: "ChekeTicket", 심볼: "CTKT")
    // msg.sender(배포자)가 Ownable의 owner가 됨
    constructor() ERC721("ChekeTicket", "CTKT") Ownable(msg.sender) {}

    // ========== 민팅 함수 ==========

    // 티켓 NFT 발행 (owner만 실행 가능)
    // to: 티켓을 받을 지갑 주소
    // showId: 공연 ID
    // sessionId: 회차 ID
    // seatInfo: 좌석 정보
    // price: 티켓 가격
    // uri: 메타데이터 URI (티켓 이미지, 공연 정보 등이 담긴 JSON 주소)
    function mintTicket(
        address to,
        uint256 showId,
        uint256 sessionId,
        string memory seatInfo,
        uint256 price,
        string memory uri
    ) external onlyOwner returns (uint256) {
        // 현재 tokenId를 가져오고, 다음을 위해 1 증가
        uint256 tokenId = _nextTokenId++;

        // ERC-721의 민팅 함수 호출 (to 주소에 tokenId NFT 발행)
        _safeMint(to, tokenId);

        // 토큰의 메타데이터 URI 설정
        _setTokenURI(tokenId, uri);

        // 티켓 정보 저장
        tickets[tokenId] = TicketInfo({
            showId: showId,
            sessionId: sessionId,
            seatInfo: seatInfo,
            price: price,
            isUsed: false
        });

        // 민팅 이벤트 발생 (프론트엔드/백엔드에서 감지 가능)
        emit TicketMinted(tokenId, to, showId, sessionId);

        return tokenId;
    }

    // ========== 티켓 사용 (입장) ==========

    // 티켓 사용 처리 (owner만 실행 가능 - QR 스캐너에서 호출)
    // tokenId: 사용할 티켓의 ID
    function useTicket(uint256 tokenId) external onlyOwner {
        // 티켓이 실제로 존재하는지 확인
        require(ownerOf(tokenId) != address(0), "Ticket does not exist");

        // 이미 사용된 티켓인지 확인
        require(!tickets[tokenId].isUsed, "Ticket already used");

        // 사용 처리
        tickets[tokenId].isUsed = true;

        // 사용 이벤트 발생
        emit TicketUsed(tokenId, ownerOf(tokenId));
    }

    // ========== 조회 함수 ==========

    // 티켓 사용 여부 조회
    function isTicketUsed(uint256 tokenId) external view returns (bool) {
        return tickets[tokenId].isUsed;
    }

    // 현재까지 발행된 총 티켓 수 조회
    function totalSupply() external view returns (uint256) {
        return _nextTokenId;
    }

    // ========== 오버라이드 (필수) ==========

    // ERC721과 ERC721URIStorage 둘 다 tokenURI를 구현하므로
    // 어떤 걸 쓸지 명시해야 함 → ERC721URIStorage 사용
    function tokenURI(uint256 tokenId)
        public
        view
        override(ERC721, ERC721URIStorage)
        returns (string memory)
    {
        return super.tokenURI(tokenId);
    }

    // 인터페이스 지원 여부 확인 (ERC-165)
    // 다른 컨트랙트가 "이 컨트랙트가 ERC-721을 지원하는지" 물어볼 때 사용
    function supportsInterface(bytes4 interfaceId)
        public
        view
        override(ERC721, ERC721URIStorage)
        returns (bool)
    {
        return super.supportsInterface(interfaceId);
    }
}
