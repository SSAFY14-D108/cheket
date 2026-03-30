const { ethers } = require("hardhat");

async function main() {
  const [owner, user1, user2] = await ethers.getSigners();

  // ========== 1) 컨트랙트 배포 ==========

  const TicketNFT = await ethers.getContractFactory("TicketNFT");
  const ticket = await TicketNFT.deploy();
  console.log("🎫 TicketNFT 배포:", await ticket.getAddress());

  // ========== 2) 단건 발행 테스트 ==========

  await ticket.mintTicket(
    user1.address, 0, 0, "A", 3, 15, "VIP", 5000
  );
  console.log("\n✅ 단건 발행 완료 (티켓 #0)");

  const t0 = await ticket.getTicket(0);
  console.log(`   구역: ${t0.section}, ${t0.row}열 ${t0.seat}번`);
  console.log(`   등급: ${t0.grade}, 가격: ${t0.price} SSF`);
  console.log(`   상태: ${["VALID", "USED", "EXPIRED", "REFUNDED"][Number(t0.status)]}`);

  // ========== 3) 일괄 발행 테스트 ==========

  // B구역 1열 1~3번, R석, 3000 SSF
  await ticket.batchMintTickets(
    user1.address, 0, 0,
    "B", 1, 1, 3,        // 구역, 열, 시작좌석, 수량
    "R", 3000             // 등급, 가격
  );
  console.log("\n✅ 일괄 발행 완료 (티켓 #1, #2, #3)");
  console.log(`   총 발행 수: ${await ticket.totalSupply()}`);
  console.log(`   user1 보유 수: ${await ticket.getWalletTicketCount(0, 0, user1.address)}`);

  // ========== 4) 입장 처리 테스트 ==========

  await ticket.checkIn(0);
  const status0 = await ticket.getTicketStatus(0);
  console.log(`\n✅ 티켓 #0 입장 처리 완료`);
  console.log(`   상태: ${["VALID", "USED", "EXPIRED", "REFUNDED"][Number(status0)]}`);

  // 이미 사용된 티켓 재입장 시도
  try {
    await ticket.checkIn(0);
    console.log("❌ 중복 입장 통과됨 (버그!)");
  } catch (e) {
    console.log("✅ 중복 입장 차단 정상");
  }

  // ========== 5) 환불 테스트 ==========

  await ticket.reclaimTicket(1, 3); // 3 = REFUNDED
  const status1 = await ticket.getTicketStatus(1);
  console.log(`\n✅ 티켓 #1 환불 처리 완료`);
  console.log(`   상태: ${["VALID", "USED", "EXPIRED", "REFUNDED"][Number(status1)]}`);

  // ========== 6) 만료 테스트 ==========

  await ticket.reclaimTicket(2, 2); // 2 = EXPIRED
  const status2 = await ticket.getTicketStatus(2);
  console.log(`\n✅ 티켓 #2 만료 처리 완료`);
  console.log(`   상태: ${["VALID", "USED", "EXPIRED", "REFUNDED"][Number(status2)]}`);

  // ========== 7) 잘못된 상태 변경 시도 ==========

  try {
    await ticket.reclaimTicket(3, 1); // 1 = USED → 불가
    console.log("❌ 잘못된 상태 변경 통과됨 (버그!)");
  } catch (e) {
    console.log("\n✅ 잘못된 상태(USED) 변경 차단 정상");
  }

  // ========== 8) 최종 상태 ==========

  console.log("\n📊 최종 티켓 상태:");
  for (let i = 0; i < 4; i++) {
    const s = await ticket.getTicketStatus(i);
    const t = await ticket.getTicket(i);
    console.log(`   #${i}: ${t.section}구역 ${t.row}열 ${t.seat}번 (${t.grade}) → ${["VALID", "USED", "EXPIRED", "REFUNDED"][Number(s)]}`);
  }
}

main().catch(console.error);
