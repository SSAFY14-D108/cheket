const { ethers } = require("hardhat");

async function main() {
  const [owner, artist, venue, platform] = await ethers.getSigners();

  // 1) 컨트랙트 배포
  const StakeholderNFT = await ethers.getContractFactory("StakeholderNFT");
  const stakeholderNFT = await StakeholderNFT.deploy();
  console.log("📄 StakeholderNFT 배포:", await stakeholderNFT.getAddress());

  const EventNFT = await ethers.getContractFactory("EventNFT");
  const eventNFT = await EventNFT.deploy();
  console.log("📄 EventNFT 배포:", await eventNFT.getAddress());

  // 2) 이해관계자 발행
  await stakeholderNFT.mint(owner.address, "organizer", 7000, 0);
  await stakeholderNFT.mint(artist.address, "artist", 2000, 0);
  await stakeholderNFT.mint(venue.address, "venue", 500, 0);
  await stakeholderNFT.mint(platform.address, "platform", 500, 0);
  console.log("\n✅ 이해관계자 4명 발행 완료");
  console.log(`   주최자: ${owner.address} (70%)`);
  console.log(`   아티스트: ${artist.address} (20%)`);
  console.log(`   장소: ${venue.address} (5%)`);
  console.log(`   플랫폼: ${platform.address} (5%)`);

  // 3) Soulbound 전송 차단 확인
  try {
    await stakeholderNFT.transferFrom(owner.address, artist.address, 0);
    console.log("\n❌ Soulbound 전송 차단 실패!");
  } catch (e) {
    console.log("\n✅ Soulbound 전송 차단 정상 작동");
  }

  // 4) 이벤트 생성
  const now = Math.floor(Date.now() / 1000);
  await eventNFT.createEvent(
    [0, 1, 2, 3], "QmExampleCID123",
    100, 4, 5000, now, now + 86400
  );
  console.log("\n✅ 이벤트 생성 완료");

  // 5) 회차 추가 (2회차)
  await eventNFT.addSession(0, now + 3600, 50);
  await eventNFT.addSession(0, now + 7200, 50);
  console.log("✅ 2회차 추가 완료");

  // 6) 결과 조회
  const info = await eventNFT.getEventInfo(0);
  console.log(`\n📊 이벤트 정보:`);
  console.log(`   총 티켓: ${info.totalSupply}장`);
  console.log(`   1인당 최대: ${info.maxPerWallet}장`);
  console.log(`   재판매 상한: ${Number(info.resaleCapBps) / 100}%`);
  console.log(`   예매 상태: ${info.isActive ? "활성" : "비활성"}`);

  // 7) 예매 가능 여부
  const isOpen = await eventNFT.isBookingOpen(0);
  console.log(`   예매 오픈: ${isOpen ? "✅ YES" : "❌ NO"}`);

  // 8) 정산 처리
  await eventNFT.finalizeSession(0);
  const session = await eventNFT.getSession(0);
  console.log(`\n✅ 1회차 정산 완료: ${session.isFinalized}`);
}

main().catch(console.error);
