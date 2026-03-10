// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";

/**
 * @title MockSSF (테스트용 SSF 토큰)
 * @notice 로컬 테스트에서만 사용. 실제 배포 시 사용 X
 */
contract MockSSF is ERC20 {
    constructor() ERC20("Mock SSF", "SSF") {
        _mint(msg.sender, 200000); // 배포자에게 200,000 SSF
    }

    // SSF는 decimals: 0
    function decimals() public pure override returns (uint8) {
        return 0;
    }
}
