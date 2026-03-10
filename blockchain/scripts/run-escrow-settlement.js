const { ethers } = require("hardhat");

async function main() {
  const [owner, buyer, artist, venue, platform] = await ethers.getSigners();

  // ========== 1) 컨트랙트 배포 ==========

  const MockSSF = await ethers.getContractFactory("MockSSF");
  const ssf = await MockSSF.deploy();
  console.log("💰 MockSSF 배포:", await ssf.getAddress());

  const Escrow = await ethers.getContractFactory("Escrow");
  const escrow = await Escrow.deploy(await ssf.getAddress());
  console.log("🔒 Escrow 배포:", await escrow.getAddress());

  const Settlement = await ethers.getContractFactory("Settlement");
  const settlement = await Settlement.deploy(await ssf.getAddress());
  console.log("📊 Settlement 배포:", await settlement.getAddress());

  // ========== 2) 구매자에게 SSF 지급 ==========

  await ssf.transfer(buyer.address, 10000);
  console.log(`\n💰 구매자 SSF 잔액: ${await ssf.balanceOf(buyer.address)}`);

  // ========== 3) 에스크로 입금 테스트 ==========

  // 구매자가 Escrow에 approve
  await ssf.connect(buyer).approve(await escrow.getAddress(), 10000);
  console.log("✅ 구매자 → Escrow approve 완료");

  // 에스크로 입금 (티켓 10,000 SSF)
  await escrow.deposit(buyer.address, 10000, 0, 0, 0);
  console.log("✅ 에스크로 입금 완료 (10,000 SSF)");

  // 에스크로 상태 확인
  const e = await escrow.getEscrow(0);
  console.log(`   상태: ${["LOCKED", "RELEASED", "REFUNDED"][Number(e.status)]}`);
  console.log(`   에스크로 SSF 잔액: ${await ssf.balanceOf(await escrow.getAddress())}`);
  console.log(`   구매자 SSF 잔액: ${await ssf.balanceOf(buyer.address)}`);

  // ========== 4) 정산 테스트 (release → settle) ==========

  // Escrow → Settlement으로 SSF 전송
  await escrow.release(0, await settlement.getAddress());
  console.log("\n✅ 에스크로 → Settlement 릴리즈 완료");
  console.log(`   Settlement SSF 잔액: ${await ssf.balanceOf(await settlement.getAddress())}`);

  // 이해관계자별 정산 (bps 합계 = 10000)
  await settlement.settle(
    0,                                                        // eventId
    0,                                                        // sessionId
    10000,                                                    // 총 금액
    [owner.address, artist.address, venue.address, platform.address],  // 지갑들
    [7000, 2000, 500, 500]                                    // bps 비율
  );

  console.log("\n✅ 정산 완료!");
  console.log(`   주최자 (70%): ${await ssf.balanceOf(owner.address)} SSF`);
  console.log(`   아티스트 (20%): ${await ssf.balanceOf(artist.address)} SSF`);
  console.log(`   장소 (5%): ${await ssf.balanceOf(venue.address)} SSF`);
  console.log(`   플랫폼 (5%): ${await ssf.balanceOf(platform.address)} SSF`);

  // ========== 5) 환불 테스트 ==========

  // 새로운 에스크로 생성
  await ssf.transfer(buyer.address, 5000);
  await ssf.connect(buyer).approve(await escrow.getAddress(), 5000);
  await escrow.deposit(buyer.address, 5000, 0, 1, 1);
  console.log(`\n🔒 에스크로 #1 입금 (5,000 SSF)`);
  console.log(`   구매자 잔액: ${await ssf.balanceOf(buyer.address)}`);

  // 환불
  await escrow.refund(1);
  console.log("✅ 환불 완료!");
  console.log(`   구매자 잔액: ${await ssf.balanceOf(buyer.address)}`);

  const e1 = await escrow.getEscrow(1);
  console.log(`   상태: ${["LOCKED", "RELEASED", "REFUNDED"][Number(e1.status)]}`);
}

main().catch(console.error);
