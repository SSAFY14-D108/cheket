/**
 * CHEKET 7-Contract 전체 배포 + 상호 주소 설정 스크립트
 *
 * 실행: npx hardhat run scripts/deploy-all.js --network ssafy
 *
 * 배포 순서:
 *   1. StakeholderNFT (파라미터 없음)
 *   2. EventNFT (파라미터 없음)
 *   3. TicketNFT (파라미터 없음)
 *   4. Settlement (_ssfToken)
 *   5. PurchaseRouter (_ssfToken, _platformWallet)
 *   6. Marketplace (_ticketNFT)
 *   7. Escrow (_ssfToken, _ticketNFT)
 *
 * 상호 주소 설정:
 *   - PurchaseRouter.setContracts(ticketNFT, settlement, eventNFT)
 *   - Settlement.setContracts(eventNFT, stakeholderNFT, ticketNFT, platformWallet)
 *   - Settlement.setPurchaseRouter(purchaseRouter)
 *   - Escrow.setEventNFT(eventNFT)
 *   - TicketNFT.setAuthorizedCaller(purchaseRouter, true)
 *   - TicketNFT.setAuthorizedCaller(settlement, true)
 *   - TicketNFT.setAuthorizedCaller(escrow, true)
 *   - TicketNFT.setAuthorizedCaller(marketplace, true)
 *   - TicketNFT.setApprovalForAll(purchaseRouter, true)  ← 플랫폼 NFT 전송 허가
 */

const hre = require("hardhat");

async function main() {
  const [deployer] = await hre.ethers.getSigners();
  const platformWallet = deployer.address;
  const ssfToken = process.env.BLOCKCHAIN_SSF_CONTRACT;

  console.log("========================================");
  console.log("CHEKET 스마트 컨트랙트 전체 배포");
  console.log("========================================");
  console.log("배포자 (플랫폼 지갑):", platformWallet);
  console.log("SSF 토큰:", ssfToken);
  console.log("네트워크:", hre.network.name);
  console.log("========================================\n");

  // ========== 1. StakeholderNFT ==========
  console.log("1/7 StakeholderNFT 배포 중...");
  const StakeholderNFT = await hre.ethers.getContractFactory("StakeholderNFT");
  const stakeholderNFT = await StakeholderNFT.deploy();
  await stakeholderNFT.waitForDeployment();
  const stakeholderNFTAddress = await stakeholderNFT.getAddress();
  console.log("    ✅ StakeholderNFT:", stakeholderNFTAddress);

  // ========== 2. EventNFT ==========
  console.log("2/7 EventNFT 배포 중...");
  const EventNFT = await hre.ethers.getContractFactory("EventNFT");
  const eventNFT = await EventNFT.deploy();
  await eventNFT.waitForDeployment();
  const eventNFTAddress = await eventNFT.getAddress();
  console.log("    ✅ EventNFT:", eventNFTAddress);

  // ========== 3. TicketNFT ==========
  console.log("3/7 TicketNFT 배포 중...");
  const TicketNFT = await hre.ethers.getContractFactory("TicketNFT");
  const ticketNFT = await TicketNFT.deploy();
  await ticketNFT.waitForDeployment();
  const ticketNFTAddress = await ticketNFT.getAddress();
  console.log("    ✅ TicketNFT:", ticketNFTAddress);

  // ========== 4. Settlement ==========
  console.log("4/7 Settlement 배포 중...");
  const Settlement = await hre.ethers.getContractFactory("Settlement");
  const settlement = await Settlement.deploy(ssfToken);
  await settlement.waitForDeployment();
  const settlementAddress = await settlement.getAddress();
  console.log("    ✅ Settlement:", settlementAddress);

  // ========== 5. PurchaseRouter ==========
  console.log("5/7 PurchaseRouter 배포 중...");
  const PurchaseRouter = await hre.ethers.getContractFactory("PurchaseRouter");
  const purchaseRouter = await PurchaseRouter.deploy(ssfToken, platformWallet);
  await purchaseRouter.waitForDeployment();
  const purchaseRouterAddress = await purchaseRouter.getAddress();
  console.log("    ✅ PurchaseRouter:", purchaseRouterAddress);

  // ========== 6. Marketplace ==========
  console.log("6/7 Marketplace 배포 중...");
  const Marketplace = await hre.ethers.getContractFactory("Marketplace");
  const marketplace = await Marketplace.deploy(ticketNFTAddress);
  await marketplace.waitForDeployment();
  const marketplaceAddress = await marketplace.getAddress();
  console.log("    ✅ Marketplace:", marketplaceAddress);

  // ========== 7. Escrow ==========
  console.log("7/7 Escrow 배포 중...");
  const Escrow = await hre.ethers.getContractFactory("Escrow");
  const escrow = await Escrow.deploy(ssfToken, ticketNFTAddress);
  await escrow.waitForDeployment();
  const escrowAddress = await escrow.getAddress();
  console.log("    ✅ Escrow:", escrowAddress);

  console.log("\n========================================");
  console.log("상호 주소 설정");
  console.log("========================================\n");

  // ========== PurchaseRouter.setContracts ==========
  console.log("PurchaseRouter.setContracts...");
  let tx = await purchaseRouter.setContracts(ticketNFTAddress, settlementAddress, eventNFTAddress);
  await tx.wait();
  console.log("    ✅ 완료");

  // ========== Settlement.setContracts ==========
  console.log("Settlement.setContracts...");
  tx = await settlement.setContracts(eventNFTAddress, stakeholderNFTAddress, ticketNFTAddress, platformWallet);
  await tx.wait();
  console.log("    ✅ 완료");

  // ========== Settlement.setPurchaseRouter ==========
  console.log("Settlement.setPurchaseRouter...");
  tx = await settlement.setPurchaseRouter(purchaseRouterAddress);
  await tx.wait();
  console.log("    ✅ 완료");

  // ========== Escrow.setEventNFT ==========
  console.log("Escrow.setEventNFT...");
  tx = await escrow.setEventNFT(eventNFTAddress);
  await tx.wait();
  console.log("    ✅ 완료");

  // ========== TicketNFT.setAuthorizedCaller (4개) ==========
  console.log("TicketNFT.setAuthorizedCaller(PurchaseRouter)...");
  tx = await ticketNFT.setAuthorizedCaller(purchaseRouterAddress, true);
  await tx.wait();
  console.log("    ✅ 완료");

  console.log("TicketNFT.setAuthorizedCaller(Settlement)...");
  tx = await ticketNFT.setAuthorizedCaller(settlementAddress, true);
  await tx.wait();
  console.log("    ✅ 완료");

  console.log("TicketNFT.setAuthorizedCaller(Escrow)...");
  tx = await ticketNFT.setAuthorizedCaller(escrowAddress, true);
  await tx.wait();
  console.log("    ✅ 완료");

  console.log("TicketNFT.setAuthorizedCaller(Marketplace)...");
  tx = await ticketNFT.setAuthorizedCaller(marketplaceAddress, true);
  await tx.wait();
  console.log("    ✅ 완료");

  // ========== TicketNFT.setApprovalForAll (PurchaseRouter가 플랫폼 NFT 전송 가능) ==========
  console.log("TicketNFT.setApprovalForAll(PurchaseRouter)...");
  tx = await ticketNFT.setApprovalForAll(purchaseRouterAddress, true);
  await tx.wait();
  console.log("    ✅ 완료");

  // ========== 결과 출력 ==========
  console.log("\n========================================");
  console.log("배포 완료! 컨트랙트 주소");
  console.log("========================================");
  console.log(`STAKEHOLDER_NFT=${stakeholderNFTAddress}`);
  console.log(`EVENT_NFT=${eventNFTAddress}`);
  console.log(`TICKET_NFT=${ticketNFTAddress}`);
  console.log(`SETTLEMENT=${settlementAddress}`);
  console.log(`PURCHASE_ROUTER=${purchaseRouterAddress}`);
  console.log(`MARKETPLACE=${marketplaceAddress}`);
  console.log(`ESCROW=${escrowAddress}`);
  console.log("========================================");
  console.log("\n이 주소들을 백엔드 .env에 추가하세요.");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("배포 실패:", error);
    process.exit(1);
  });
