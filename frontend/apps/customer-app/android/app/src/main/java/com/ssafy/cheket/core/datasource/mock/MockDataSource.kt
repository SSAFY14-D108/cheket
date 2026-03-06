package com.ssafy.cheket.core.datasource.mock

import com.ssafy.cheket.core.model.*

object MockDataSource {

    val mockUser = User(
        id = "user_001",
        name = "김민준",
        phone = "010-1234-5678",
        email = "minjun.kim@cheket.app",
        walletAddress = "0x3a9F...dE42",
        ctkBalance = 2400,
    )

    private const val P = "https://picsum.photos/seed"

    val mockEvents = listOf(
        Event(
            id = "evt_001",
            name = "AESPA WORLD TOUR 2026",
            artistName = "aespa",
            date = "2026.04.12 (토) ~ 04.13 (일)",
            dates = listOf(
                EventDate("evt_001_d1", "2026.04.12 (토) 18:00", "DAY 1"),
                EventDate("evt_001_d2", "2026.04.12 (토) 21:00", "DAY 1 (야간)"),
                EventDate("evt_001_d3", "2026.04.13 (일) 18:00", "DAY 2"),
            ),
            venue = "올림픽체조경기장, 서울",
            region = "서울",
            poster = "$P/aespa/400/600",
            status = EventStatus.ON_SALE,
            maxPerUser = 4,
            openDate = "2026-04-12",
            grades = listOf(
                Grade("VIP", 180000, 12, "#f59e0b"),
                Grade("R석", 140000, 34, "#ef4444"),
                Grade("S석", 110000, 87, "#3b82f6"),
                Grade("A석", 88000, 152, "#22c55e"),
            ),
            description = "aespa의 첫 번째 월드 투어. 서울 단독 공연으로 펼쳐지는 화려한 무대.",
        ),
        Event(
            id = "evt_002",
            name = "METALLICA M72 WORLD TOUR",
            artistName = "Metallica",
            date = "2026.05.24 (토) ~ 05.25 (일)",
            dates = listOf(
                EventDate("evt_002_d1", "2026.05.24 (토) 18:00", "DAY 1"),
                EventDate("evt_002_d2", "2026.05.25 (일) 18:00", "DAY 2"),
            ),
            venue = "고척스카이돔, 서울",
            region = "서울",
            poster = "$P/metallica/400/600",
            status = EventStatus.ON_SALE,
            maxPerUser = 2,
            openDate = "2026-05-24",
            grades = listOf(
                Grade("VIP PIT", 250000, 0, "#f59e0b"),
                Grade("GA PIT", 180000, 5, "#ef4444"),
                Grade("FLOOR", 150000, 44, "#3b82f6"),
                Grade("STAND", 120000, 103, "#22c55e"),
            ),
            description = "메탈리카의 M72 월드투어 한국 공연. 역사적인 무대를 경험하세요.",
        ),
        Event(
            id = "evt_003",
            artistName = "Seoul Philharmonic Orchestra",
            name = "서울 필하모닉 뉴이어 콘서트",
            date = "2026.01.01 (수) 15:00",
            venue = "예술의전당 콘서트홀, 서울",
            region = "서울",
            poster = "$P/philharmonic/400/600",
            status = EventStatus.SOLD_OUT,
            maxPerUser = 6,
            openDate = "2026-01-01",
            grades = listOf(
                Grade("VIP", 120000, 0, "#f59e0b"),
                Grade("R석", 90000, 0, "#ef4444"),
                Grade("S석", 70000, 0, "#3b82f6"),
            ),
            description = "새해를 여는 서울 필하모닉의 특별 갈라 콘서트.",
        ),
        Event(
            id = "evt_004",
            name = "ULTRA KOREA 2026",
            artistName = "Martin Garrix, Armin van Buuren 외",
            date = "2026.06.07 (토) ~ 06.08 (일)",
            dates = listOf(
                EventDate("evt_004_d1", "2026.06.07 (토) 14:00", "DAY 1"),
                EventDate("evt_004_d2", "2026.06.08 (일) 14:00", "DAY 2"),
            ),
            venue = "잠실종합운동장, 서울",
            region = "서울",
            poster = "$P/ultrakorea/400/600",
            status = EventStatus.ON_SALE,
            maxPerUser = 4,
            openDate = "2026-06-07",
            grades = listOf(
                Grade("VIP", 300000, 8, "#f59e0b"),
                Grade("GA", 220000, 210, "#3b82f6"),
            ),
            description = "세계 최대 EDM 페스티벌 울트라코리아 2026.",
        ),
        Event(
            id = "evt_005",
            name = "JARASUM JAZZ FESTIVAL",
            artistName = "Pat Metheny, Snarky Puppy 외",
            date = "2026.10.05 (토) 12:00",
            venue = "자라섬, 가평",
            region = "경기",
            poster = "$P/jarasum/400/600",
            status = EventStatus.SOLD_OUT,
            maxPerUser = 8,
            openDate = "2026-10-05",
            grades = listOf(
                Grade("1일권", 88000, 0, "#22c55e"),
                Grade("2일권", 150000, 0, "#3b82f6"),
            ),
            description = "아시아 최대 재즈 축제 자라섬 국제 재즈 페스티벌.",
        ),
    )

    // ── Home screen data ──

    val bannerSlides = listOf(
        BannerSlide("b1", "evt_004", "$P/ultrakorea/800/400",
            "2026 울트라코리아", "ULTRA KOREA IS COMING", "잠실종합운동장, 서울", "2026.6.7 - 2026.6.8"),
        BannerSlide("b2", "evt_001", "$P/aespa/800/400",
            "2026 aespa 월드 투어", "AESPA WORLD TOUR", "올림픽체조경기장, 서울", "2026.4.12"),
        BannerSlide("b3", "evt_002", "$P/metallica/800/400",
            "메탈리카 내한 공연", "METALLICA M72 WORLD TOUR", "고척스카이돔, 서울", "2026.5.24"),
    )

    val categories = listOf(
        CategoryIcon("musical", "뮤지컬", "🎭"),
        CategoryIcon("concert", "콘서트", "🎤"),
        CategoryIcon("sports", "스포츠", "🏀"),
        CategoryIcon("classic", "클래식/무용", "🎹"),
        CategoryIcon("play", "연극", "🎬"),
        CategoryIcon("leisure", "레저/캠핑", "⛺"),
        CategoryIcon("family", "아동/가족", "👨\u200D👩\u200D👧"),
        CategoryIcon("exhibit", "전시/행사", "🖼️"),
        CategoryIcon("special", "특별공연", "✨"),
        CategoryIcon("benefit", "이달의혜택", "🎁"),
    )

    val rankingItems = listOf(
        RankingItem(1, "evt_001", "AESPA WORLD TOUR 2026", "올림픽체조경기장", "$P/aespa/400/600", "콘서트"),
        RankingItem(2, "evt_002", "METALLICA M72 WORLD TOUR", "고척스카이돔", "$P/metallica/400/600", "콘서트"),
        RankingItem(3, "evt_004", "ULTRA KOREA 2026", "잠실종합운동장", "$P/ultrakorea/400/600", "콘서트"),
        RankingItem(4, "evt_003", "서울 필하모닉 뉴이어 콘서트", "예술의전당", "$P/philharmonic/400/600", "클래식"),
        RankingItem(5, "evt_005", "JARASUM JAZZ FESTIVAL", "자라섬, 가평", "$P/jarasum/400/600", "콘서트"),
    )

    val openSchedule = listOf(
        OpenScheduleItem("op_001", "evt_001", "AESPA WORLD TOUR 2026",
            "오늘 18:00", "일반예매", listOf("단독판매"), "$P/aespa/400/600", true),
        OpenScheduleItem("op_002", "evt_002", "METALLICA M72 WORLD TOUR",
            "내일 20:00", "멤버십 선구매", listOf("단독판매"), "$P/metallica/400/600", false),
        OpenScheduleItem("op_003", "evt_003", "서울 필하모닉 뉴이어 콘서트",
            "03.03(화) 18:00", "일반예매", listOf("HOT", "단독판매"), "$P/philharmonic/400/600", false),
        OpenScheduleItem("op_004", "evt_004", "ULTRA KOREA 2026",
            "03.10(화) 10:00", "얼리버드", listOf("단독판매"), "$P/ultrakorea/400/600", false),
    )

    val discountItems = listOf(
        DiscountItem("dc_001", "evt_005", "JARASUM JAZZ FESTIVAL",
            "자라섬, 가평", "2026.10.5 ~ 10.7", "전석 할인", 40, 88000, "00:00:00", true, "$P/jarasum/400/600"),
        DiscountItem("dc_002", "evt_003", "서울 필하모닉 뉴이어 콘서트",
            "예술의전당 콘서트홀", "2026.1.1 ~ 1.3", "R석 할인", 50, 45000, "D-10 10:24:33", true, "$P/philharmonic/400/600"),
        DiscountItem("dc_003", "evt_001", "AESPA WORLD TOUR 2026",
            "올림픽체조경기장", "2026.4.12", "S석 할인", 20, 88000, "10:24:33", false, "$P/aespa/400/600"),
    )

    // ── Tickets ──

    val mockTickets = listOf(
        Ticket("tkt_001", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa/400/600", "evt_001_C3", "C열 3번", "R석", 140000, TicketStatus.SOLD),
        Ticket("tkt_002", "evt_002", "METALLICA M72 WORLD TOUR", "2025.05.24 (토) 18:00",
            "고척스카이돔, 서울", "$P/metallica/400/600", "evt_002_B7", "B열 7번", "FLOOR", 150000, TicketStatus.LISTED, resalePrice = 145000),
        Ticket("tkt_003", "evt_005", "JARASUM JAZZ FESTIVAL", "2024.10.05 (토) 12:00",
            "자라섬, 가평", "$P/jarasum/400/600", "evt_005_A2", "A열 2번", "2일권", 150000, TicketStatus.USED, attendedDate = "2024.10.05"),
        Ticket("tkt_004", "evt_003", "서울 필하모닉 뉴이어 콘서트", "2025.01.01 (수) 15:00",
            "예술의전당 콘서트홀, 서울", "$P/philharmonic/400/600", "evt_003_D5", "D열 5번", "R석", 90000, TicketStatus.EXPIRED),
        Ticket("tkt_005", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa/400/600", "evt_001_A1", "A열 1번", "VIP", 180000, TicketStatus.USED, attendedDate = "2025.04.12"),
        Ticket("tkt_006", "evt_002", "METALLICA M72 WORLD TOUR", "2025.05.24 (토) 18:00",
            "고척스카이돔, 서울", "$P/metallica/400/600", "evt_002_F11", "F열 11번", "GA PIT", 180000, TicketStatus.USED, attendedDate = "2025.05.24"),
    )

    // ── Resale ──

    val mockResaleItems = listOf(
        ResaleItem("rs_001", "tkt_002", "evt_002", "METALLICA M72 WORLD TOUR", "2025.05.24 (토) 18:00",
            "고척스카이돔, 서울", "$P/metallica/400/600", "B열 7번", "FLOOR", 150000, 145000, "user_001"),
        ResaleItem("rs_002", "tkt_ext_001", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa/400/600", "E열 4번", "S석", 110000, 105000, "user_042"),
        ResaleItem("rs_003", "tkt_ext_002", "evt_004", "ULTRA KOREA 2025", "2025.06.07 (토) 14:00",
            "잠실종합운동장, 서울", "$P/ultrakorea/400/600", "GA 구역 215번", "GA", 220000, 210000, "user_088"),
        ResaleItem("rs_004", "tkt_ext_003", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa/400/600", "A열 1번", "VIP", 180000, 175000, "user_019"),
    )

    // ── Wallet / TX mock data ──

    val mockWalletTxs = listOf(
        WalletTx("wtx_001", WalletTxType.CHARGE, "CTK 충전", 5000, 7400, System.currentTimeMillis() - 86400000 * 30),
        WalletTx("wtx_002", WalletTxType.PURCHASE, "AESPA WORLD TOUR 2025 - R석", -140000, 2400, System.currentTimeMillis() - 86400000 * 20),
        WalletTx("wtx_003", WalletTxType.CHARGE, "CTK 충전", 3000, 142400, System.currentTimeMillis() - 86400000 * 15),
        WalletTx("wtx_004", WalletTxType.RESALE_SELL, "METALLICA FLOOR - 리세일 수익", 145000, 5400, System.currentTimeMillis() - 86400000 * 10),
        WalletTx("wtx_005", WalletTxType.PURCHASE, "ULTRA KOREA 2025 - VIP", -300000, 2400, System.currentTimeMillis() - 86400000 * 5),
    )

    val mockTxRecords = listOf(
        TxRecord("txr_001", "0x1a2b3c...4d5e", TxType.PURCHASE, TxStatus.CONFIRMED,
            "AESPA WORLD TOUR 2025 티켓 구매", 140000, System.currentTimeMillis() - 86400000 * 20,
            confirmedAt = System.currentTimeMillis() - 86400000 * 20 + 60000, confirmations = 12),
        TxRecord("txr_002", "0x6f7e8d...9c0b", TxType.RESALE_LIST, TxStatus.CONFIRMED,
            "METALLICA FLOOR 리세일 등록", null, System.currentTimeMillis() - 86400000 * 15,
            confirmedAt = System.currentTimeMillis() - 86400000 * 15 + 45000, confirmations = 12),
        TxRecord("txr_003", "0xab12cd...ef34", TxType.PURCHASE, TxStatus.CONFIRMING,
            "ULTRA KOREA 2025 티켓 구매", 300000, System.currentTimeMillis() - 86400000 * 5,
            confirmations = 7),
        TxRecord("txr_004", "0xde56fg...hi78", TxType.TRANSFER, TxStatus.PENDING,
            "JARASUM JAZZ 양도", null, System.currentTimeMillis() - 3600000),
    )

    // ── Phone book for transfer ──

    val phoneBook = mapOf(
        "010-9876-5432" to "박지연",
        "010-1234-5678" to "김민준",
        "010-5555-4444" to "이수진",
    )

    // ── Seat generation (matches v0 logic) ──

    fun generateSeats(eventId: String, gradeName: String? = null): List<Seat> {
        val event = mockEvents.find { it.id == eventId } ?: return emptyList()
        val grades = event.grades
        val cols = 10
        val rowsPerGrade = 4
        val seats = mutableListOf<Seat>()
        val rowLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        grades.forEachIndexed { gradeIdx, grade ->
            if (gradeName != null && grade.name != gradeName) return@forEachIndexed

            for (r in 0 until rowsPerGrade) {
                val rowLabel = "${grade.name}-${rowLetters[r]}"
                for (num in 1..cols) {
                    val seatIndex = gradeIdx * rowsPerGrade * cols + r * cols + num
                    val rand = seatIndex % 7
                    val status = when {
                        grade.remaining == 0 -> SeatStatus.SOLD
                        rand == 2 || rand == 5 -> SeatStatus.SOLD
                        rand == 3 -> SeatStatus.LOCKED
                        else -> SeatStatus.AVAILABLE
                    }
                    seats.add(Seat(
                        id = "${eventId}_${grade.name}_${rowLetters[r]}$num",
                        row = rowLabel,
                        number = num,
                        grade = grade.name,
                        price = grade.price,
                        status = status,
                    ))
                }
            }
        }
        return seats
    }

    fun getResaleGrouped(): List<ResaleGroupItem> =
        mockResaleItems.groupBy { it.eventId }.map { (eventId, items) ->
            ResaleGroupItem(
                eventId = eventId,
                eventName = items.first().eventName,
                poster = items.first().poster,
                venue = items.first().venue,
                eventDate = items.first().eventDate,
                count = items.size,
                minPrice = items.minOf { it.resalePrice },
                items = items,
            )
        }
}
