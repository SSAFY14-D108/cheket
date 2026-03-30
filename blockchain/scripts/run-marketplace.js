const { ethers } = require("hardhat");

async function main() {
    const [owner, seller, buyer] = await ethers.getSigners();

    // ========== 1) 컨트랙트 배포 ==========

    const MockSSF = await ethers.getContractFactory("MockSSF");
    const ssf = await MockSSF.deploy();
    console.log("💰 MockSSF 배포:", await ssf.getAddress());

    const TicketNFT = await ethers.getContractFactory("TicketNFT");
    const ticket = await TicketNFT.deploy();
    console.log("🎫 TicketNFT 배포:", await ticket.getAddress());

    const Marketplace = await ethers.getContractFactory("Marketplace");
    const market = await Marketplace.deploy(
        await ssf.getAddress(),
        await ticket.getAddress()
    );
    console.log("🏪 Marketplace 배포:", await market.getAddress());

    // ========== 2) 초기 설정 ==========

    // 판매자에게 티켓 발행 (원가 1000 SSF)
    await ticket.mintTicket(seller.address, 0, 0, "A구역 3열 15번", 1000, "");
    console.log("\n🎫 판매자에게 티켓 #0 발행 (원가 1000 SSF)");

    // 구매자에게 SSF 지급
    await ssf.transfer(buyer.address, 5000);
    console.log(`💰 구매자 SSF 잔액: ${await ssf.balanceOf(buyer.address)}`);

    // ========== 3) 재판매 등록 테스트 ==========

    // 판매자가 마켓에 approve
    await ticket.connect(seller).approve(await market.getAddress(), 0);

    // 재판매 등록 (원가 1000, 재판매가 800, 상한 10000 bps = 100%)
    await market.connect(seller).listResale(0, 1000, 800, 10000);
    console.log("\n✅ 재판매 등록 완료 (800 SSF)");
    console.log(`   티켓 소유자: ${await ticket.ownerOf(0)} (= Marketplace)`);

    // ========== 4) 가격 상한 초과 테스트 ==========

    // 티켓 하나 더 발행
    await ticket.mintTicket(seller.address, 0, 0, "B구역 1열 5번", 1000, "");
    await ticket.connect(seller).approve(await market.getAddress(), 1);

    try {
        // 원가 1000, 상한 100%(10000 bps) = 1000 SSF까지 허용인데, 1200으로 등록 시도
        await market.connect(seller).listResale(1, 1000, 1200, 10000);
        console.log("\n❌ 가격 상한 초과 통과됨 (버그!)");
    } catch (e) {
        console.log("\n✅ 가격 상한 초과 차단 정상 (1200 > 원가 1000)");
    }

    // ========== 5) 구매 테스트 ==========

    // 구매자가 SSF approve
    await ssf.connect(buyer).approve(await market.getAddress(), 800);

    // 재판매 티켓 구매
    await market.connect(buyer).buyResale(0);
    console.log("\n✅ 재판매 구매 완료!");
    console.log(`   티켓 #0 소유자: ${await ticket.ownerOf(0)} (= buyer)`);
    console.log(`   판매자 SSF: ${await ssf.balanceOf(seller.address)}`);
    console.log(`   구매자 SSF: ${await ssf.balanceOf(buyer.address)}`);

    // ========== 6) 등록 취소 테스트 ==========

    // 원가 이하로 재등록 (800 SSF, 상한 100%)
    await ticket.connect(seller).approve(await market.getAddress(), 1);
    await market.connect(seller).listResale(1, 1000, 800, 10000);
    console.log("\n🏪 티켓 #1 재판매 등록 (800 SSF)");

    // 취소
    await market.connect(seller).cancelListing(1);
    console.log("✅ 등록 취소 완료!");
    console.log(`   티켓 #1 소유자: ${await ticket.ownerOf(1)} (= seller 반환)`);

    // ========== 7) 양도 테스트 ==========

    await ticket.connect(seller).approve(await market.getAddress(), 1);
    await market.connect(seller).directTransfer(1, buyer.address);
    console.log("\n✅ 무료 양도 완료!");
    console.log(`   티켓 #1 소유자: ${await ticket.ownerOf(1)} (= buyer)`);
}

main().catch(console.error);
