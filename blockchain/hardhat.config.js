require("@nomicfoundation/hardhat-toolbox");
require("dotenv").config();

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
  solidity: {
    version: "0.8.28",
    settings: {
      evmVersion: "london",
    },
  },
  networks: {
    ssafy: {
      url: process.env.BLOCKCHAIN_RPC_URL || "https://rpc.ssafy-blockchain.com",
      chainId: Number(process.env.BLOCKCHAIN_CHAIN_ID) || 31221,
      accounts: [process.env.PRIVATE_KEY],
      gasPrice: 0,
    },
  },
};
