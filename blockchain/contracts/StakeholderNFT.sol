// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title StakeholderNFT (Soulbound ERC-721)
 * @notice 이해관계자의 수익 분배 권한을 나타내는 NFT
 *         한번 발행되면 전송 불가 (Soulbound)
 *
 * [예시] 공연 수익 분배
 * - 주최자: 7000 bps (70%)
 * - 아티스트: 2000 bps (20%)
 * - 장소: 500 bps (5%)
 * - 플랫폼: 500 bps (5%) ← 고정
 * - 합계: 10000 bps (100%)
 */
contract StakeholderNFT is ERC721, Ownable {

    // ========== 상태 변수 ==========

    uint256 private _nextTokenId;

    struct Stakeholder {
        address wallet;       // 이해관계자 지갑 주소
        string role;          // 역할 ("organizer", "artist", "venue", "platform")
        uint256 shareBps;     // 수익 분배 비율 (10000 = 100%)
        uint256 eventNftId;   // 연결된 EventNFT ID
    }

    // tokenId => Stakeholder
    mapping(uint256 => Stakeholder) public stakeholders;

    // ========== 이벤트 ==========

    event StakeholderMinted(
        uint256 indexed tokenId,
        address indexed wallet,
        string role,
        uint256 shareBps,
        uint256 eventNftId
    );

    // ========== 생성자 ==========

    constructor() ERC721("CHEKET Stakeholder", "CSTK") Ownable(msg.sender) {}

    // ========== Soulbound: 전송 차단 ==========

    /**
     * @dev 민팅(from == address(0))만 허용, 일반 전송은 차단
     */
    function _update(
        address to,
        uint256 tokenId,
        address auth
    ) internal override returns (address) {
        address from = _ownerOf(tokenId);
        if (from != address(0) && to != address(0)) {
            revert("Soulbound: transfer not allowed");
        }
        return super._update(to, tokenId, auth);
    }

    // ========== 민팅 함수 ==========

    /**
     * @notice 이해관계자 NFT 발행
     * @param to 이해관계자 지갑 주소
     * @param role 역할 ("organizer", "artist", "venue", "platform")
     * @param shareBps 수익 분배 비율 (1~10000)
     * @param eventNftId 연결된 EventNFT ID
     * @return tokenId 발행된 토큰 ID
     */
    function mint(
        address to,
        string calldata role,
        uint256 shareBps,
        uint256 eventNftId
    ) external onlyOwner returns (uint256) {
        require(to != address(0), "Invalid address");
        require(shareBps > 0 && shareBps <= 10000, "Invalid shareBps");

        uint256 tokenId = _nextTokenId++;
        _safeMint(to, tokenId);

        stakeholders[tokenId] = Stakeholder({
            wallet: to,
            role: role,
            shareBps: shareBps,
            eventNftId: eventNftId
        });

        emit StakeholderMinted(tokenId, to, role, shareBps, eventNftId);
        return tokenId;
    }

    // ========== 조회 함수 ==========

    function getStakeholder(uint256 tokenId) external view returns (
        address wallet,
        string memory role,
        uint256 shareBps,
        uint256 eventNftId
    ) {
        Stakeholder storage s = stakeholders[tokenId];
        return (s.wallet, s.role, s.shareBps, s.eventNftId);
    }

    function totalSupply() external view returns (uint256) {
        return _nextTokenId;
    }
}
