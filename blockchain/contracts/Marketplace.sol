// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC721/IERC721.sol";

/**
 * @title Marketplace (티켓 재판매 마켓)
 * @notice 티켓 NFT의 재판매/양도를 처리
 *         EventNFT의 resaleCapBps로 가격 상한 제한 (암표 방지)
 *
 * [흐름 - 재판매]
 * 1. 판매자가 TicketNFT approve → listResale() → 마켓에 등록
 * 2. 구매자가 SSF approve → buyResale() → SSF는 판매자에게, 티켓은 구매자에게
 *
 * [흐름 - 양도]
 * 1. 소유자가 TicketNFT approve → directTransfer() → 무료 전송
 *
 * [가격 상한]
 * resaleCapBps = 10000 → 원가 100%까지 허용 (원가 이하로만 판매)
 * resaleCapBps = 5000  → 원가 50%까지 허용
 */
contract Marketplace is Ownable {

    // ========== 상태 변수 ==========

    IERC20 public ssfToken;
    IERC721 public ticketNFT;

    struct Listing {
        address seller;         // 판매자
        uint256 ticketId;       // 티켓 NFT ID
        uint256 originalPrice;  // 원래 구매 가격
        uint256 resalePrice;    // 재판매 가격
        bool isActive;          // 활성 상태
        uint256 listedAt;       // 등록 시각
    }

    uint256 private _nextListingId;

    // listingId => Listing
    mapping(uint256 => Listing) public listings;

    // ticketId => listingId (중복 등록 방지)
    mapping(uint256 => uint256) public ticketListing;

    // ticketId => 등록 여부
    mapping(uint256 => bool) public isListed;

    // ========== 이벤트 ==========

    event Listed(
        uint256 indexed listingId,
        address indexed seller,
        uint256 indexed ticketId,
        uint256 resalePrice
    );

    event Sold(
        uint256 indexed listingId,
        address indexed buyer,
        address indexed seller,
        uint256 ticketId,
        uint256 resalePrice
    );

    event ListingCancelled(
        uint256 indexed listingId,
        uint256 indexed ticketId
    );

    event DirectTransferred(
        uint256 indexed ticketId,
        address indexed from,
        address indexed to
    );

    // ========== 생성자 ==========

    /**
     * @param _ssfToken SSF 토큰 주소
     * @param _ticketNFT TicketNFT 컨트랙트 주소
     */
    constructor(
        address _ssfToken,
        address _ticketNFT
    ) Ownable(msg.sender) {
        require(_ssfToken != address(0), "Invalid SSF address");
        require(_ticketNFT != address(0), "Invalid TicketNFT address");
        ssfToken = IERC20(_ssfToken);
        ticketNFT = IERC721(_ticketNFT);
    }

    // ========== 재판매 등록 ==========

    /**
     * @notice 티켓을 재판매 등록
     * @dev 호출 전 판매자가 ticketNFT.approve(marketplace, ticketId) 필요
     * @param ticketId 티켓 NFT ID
     * @param originalPrice 원래 구매 가격 (백엔드에서 전달)
     * @param resalePrice 재판매 희망 가격
     * @param resaleCapBps 가격 상한 비율 (EventNFT에서 조회, 백엔드 전달)
     * @return listingId 등록 ID
     */
    function listResale(
        uint256 ticketId,
        uint256 originalPrice,
        uint256 resalePrice,
        uint256 resaleCapBps
    ) external returns (uint256) {
        // 티켓 소유자 확인
        require(ticketNFT.ownerOf(ticketId) == msg.sender, "Not ticket owner");
        // 중복 등록 방지
        require(!isListed[ticketId], "Already listed");
        // 가격 상한 검증: resalePrice <= originalPrice * resaleCapBps / 10000
        uint256 maxPrice = (originalPrice * resaleCapBps) / 10000;
        require(resalePrice <= maxPrice, "Exceeds resale cap");
        require(resalePrice > 0, "Price must be > 0");

        // 티켓을 마켓플레이스로 전송 (에스크로 역할)
        ticketNFT.transferFrom(msg.sender, address(this), ticketId);

        uint256 listingId = _nextListingId++;

        listings[listingId] = Listing({
            seller: msg.sender,
            ticketId: ticketId,
            originalPrice: originalPrice,
            resalePrice: resalePrice,
            isActive: true,
            listedAt: block.timestamp
        });

        ticketListing[ticketId] = listingId;
        isListed[ticketId] = true;

        emit Listed(listingId, msg.sender, ticketId, resalePrice);
        return listingId;
    }

    // ========== 재판매 구매 ==========

    /**
     * @notice 재판매 티켓 구매
     * @dev 호출 전 구매자가 ssfToken.approve(marketplace, resalePrice) 필요
     * @param listingId 등록 ID
     */
    function buyResale(uint256 listingId) external {
        Listing storage l = listings[listingId];
        require(l.isActive, "Not active listing");
        require(msg.sender != l.seller, "Cannot buy own ticket");

        l.isActive = false;
        isListed[l.ticketId] = false;

        // SSF: 구매자 → 판매자
        bool success = ssfToken.transferFrom(msg.sender, l.seller, l.resalePrice);
        require(success, "SSF transfer failed");

        // 티켓: 마켓플레이스 → 구매자
        ticketNFT.transferFrom(address(this), msg.sender, l.ticketId);

        emit Sold(listingId, msg.sender, l.seller, l.ticketId, l.resalePrice);
    }

    // ========== 등록 취소 ==========

    /**
     * @notice 재판매 등록 취소 (티켓 반환)
     * @param listingId 등록 ID
     */
    function cancelListing(uint256 listingId) external {
        Listing storage l = listings[listingId];
        require(l.isActive, "Not active listing");
        require(l.seller == msg.sender || owner() == msg.sender, "Not authorized");

        l.isActive = false;
        isListed[l.ticketId] = false;

        // 티켓: 마켓플레이스 → 판매자에게 반환
        ticketNFT.transferFrom(address(this), l.seller, l.ticketId);

        emit ListingCancelled(listingId, l.ticketId);
    }

    // ========== 양도 (무료 전송) ==========

    /**
     * @notice 티켓을 다른 사람에게 무료 양도
     * @dev 호출 전 ticketNFT.approve(marketplace, ticketId) 필요
     * @param ticketId 티켓 NFT ID
     * @param to 양도받을 지갑 주소
     */
    function directTransfer(uint256 ticketId, address to) external {
        require(ticketNFT.ownerOf(ticketId) == msg.sender, "Not ticket owner");
        require(!isListed[ticketId], "Ticket is listed");
        require(to != address(0), "Invalid address");
        require(to != msg.sender, "Cannot transfer to self");

        ticketNFT.transferFrom(msg.sender, to, ticketId);

        emit DirectTransferred(ticketId, msg.sender, to);
    }

    // ========== 조회 함수 ==========

    function getListing(uint256 listingId) external view returns (
        address seller,
        uint256 ticketId,
        uint256 originalPrice,
        uint256 resalePrice,
        bool isActive
    ) {
        Listing storage l = listings[listingId];
        return (l.seller, l.ticketId, l.originalPrice, l.resalePrice, l.isActive);
    }

    function totalListings() external view returns (uint256) {
        return _nextListingId;
    }
}
