const hre = require("hardhat");

async function main() {
  const [deployer] = await hre.ethers.getSigners();
  const SSF_ADDRESS = process.env.BLOCKCHAIN_SSF_CONTRACT;
  const PLATFORM_WALLET = deployer.address;

  console.log("=== CHEKET 스마트 컨트랙트 전체 배포 ===");
  console.log("배포자 (플랫폼 지갑):", PLATFORM_WALLET);
  console.log("SSF 토큰:", SSF_ADDRESS);
  console.log("");

  // 1. StakeholderNFT (의존성 없음)
  const StakeholderNFT = await hre.ethers.getContractFactory("StakeholderNFT");
  const stakeholderNFT = await StakeholderNFT.deploy();
  await stakeholderNFT.waitForDeployment();
  const stakeholderAddr = await stakeholderNFT.getAddress();
  console.log("1. StakeholderNFT:", stakeholderAddr);

  // 2. EventNFT (의존성 없음)
  const EventNFT = await hre.ethers.getContractFactory("EventNFT");
  const eventNFT = await EventNFT.deploy();
  await eventNFT.waitForDeployment();
  const eventAddr = await eventNFT.getAddress();
  console.log("2. EventNFT:", eventAddr);

  // 3. TicketNFT (의존성 없음)
  const TicketNFT = await hre.ethers.getContractFactory("TicketNFT");
  const ticketNFT = await TicketNFT.deploy();
  await ticketNFT.waitForDeployment();
  const ticketAddr = await ticketNFT.getAddress();
  console.log("3. TicketNFT:", ticketAddr);

  // 4. Settlement (SSF 필요)
  const Settlement = await hre.ethers.getContractFactory("Settlement");
  const settlement = await Settlement.deploy(SSF_ADDRESS);
  await settlement.waitForDeployment();
  const settlementAddr = await settlement.getAddress();
  console.log("4. Settlement:", settlementAddr);

  // 5. PurchaseRouter (SSF + 플랫폼 지갑 필요)
  const PurchaseRouter = await hre.ethers.getContractFactory("PurchaseRouter");
  const purchaseRouter = await PurchaseRouter.deploy(SSF_ADDRESS, PLATFORM_WALLET);
  await purchaseRouter.waitForDeployment();
  const purchaseRouterAddr = await purchaseRouter.getAddress();
  console.log("5. PurchaseRouter:", purchaseRouterAddr);

  // 6. Marketplace (TicketNFT 필요)
  const Marketplace = await hre.ethers.getContractFactory("Marketplace");
  const marketplace = await Marketplace.deploy(ticketAddr);
  await marketplace.waitForDeployment();
  const marketplaceAddr = await marketplace.getAddress();
  console.log("6. Marketplace:", marketplaceAddr);

  // 7. Escrow (SSF + TicketNFT 필요)
  const Escrow = await hre.ethers.getContractFactory("Escrow");
  const escrow = await Escrow.deploy(SSF_ADDRESS, ticketAddr);
  await escrow.waitForDeployment();
  const escrowAddr = await escrow.getAddress();
  console.log("7. Escrow:", escrowAddr);

  // ========== 컨트랙트 간 연결 ==========
  console.log("\n=== 컨트랙트 간 권한 설정 ===");

  // PurchaseRouter.setContracts(ticketNFT, settlement, eventNFT)
  await purchaseRouter.setContracts(ticketAddr, settlementAddr, eventAddr);
  console.log("PurchaseRouter.setContracts 완료");

  // Settlement.setContracts(eventNFT, stakeholderNFT, ticketNFT, platformWallet)
  await settlement.setContracts(eventAddr, stakeholderAddr, ticketAddr, PLATFORM_WALLET);
  console.log("Settlement.setContracts 완료");

  // Settlement.setPurchaseRouter(purchaseRouter)
  await settlement.setPurchaseRouter(purchaseRouterAddr);
  console.log("Settlement.setPurchaseRouter 완료");

  // TicketNFT.setAuthorizedCaller — PurchaseRouter, Marketplace, Escrow, Settlement
  await ticketNFT.setAuthorizedCaller(purchaseRouterAddr, true);
  console.log("TicketNFT.setAuthorizedCaller(PurchaseRouter) 완료");
  await ticketNFT.setAuthorizedCaller(marketplaceAddr, true);
  console.log("TicketNFT.setAuthorizedCaller(Marketplace) 완료");
  await ticketNFT.setAuthorizedCaller(escrowAddr, true);
  console.log("TicketNFT.setAuthorizedCaller(Escrow) 완료");
  await ticketNFT.setAuthorizedCaller(settlementAddr, true);
  console.log("TicketNFT.setAuthorizedCaller(Settlement) 완료");

  // 플랫폼 지갑이 PurchaseRouter/Marketplace/Escrow/Settlement에 NFT 전송 권한 부여
  await ticketNFT.setApprovalForAll(purchaseRouterAddr, true);
  console.log("TicketNFT.setApprovalForAll(PurchaseRouter) 완료");
  await ticketNFT.setApprovalForAll(marketplaceAddr, true);
  console.log("TicketNFT.setApprovalForAll(Marketplace) 완료");
  await ticketNFT.setApprovalForAll(escrowAddr, true);
  console.log("TicketNFT.setApprovalForAll(Escrow) 완료");
  await ticketNFT.setApprovalForAll(settlementAddr, true);
  console.log("TicketNFT.setApprovalForAll(Settlement) 완료");


  // ========== 결과 출력 ==========
  console.log("\n=== 배포 완료 — .env에 복사하세요 ===");
  console.log(`BLOCKCHAIN_STAKEHOLDER_NFT=${stakeholderAddr}`);
  console.log(`BLOCKCHAIN_EVENT_NFT=${eventAddr}`);
  console.log(`BLOCKCHAIN_TICKET_NFT=${ticketAddr}`);
  console.log(`BLOCKCHAIN_SETTLEMENT=${settlementAddr}`);
  console.log(`BLOCKCHAIN_PURCHASE_ROUTER=${purchaseRouterAddr}`);
  console.log(`BLOCKCHAIN_MARKETPLACE=${marketplaceAddr}`);
  console.log(`BLOCKCHAIN_ESCROW=${escrowAddr}`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
