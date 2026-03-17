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

    private const val P = "file:///android_asset/posters"

    val mockShows = listOf(
        Show(
            id = "evt_001",
            name = "AESPA WORLD TOUR 2026",
            artistName = "aespa",
            date = "2026.04.12 (토) ~ 04.13 (일)",
            dates = listOf(
                ShowDate("evt_001_d1", "2026.04.12 (토) 18:00", "DAY 1"),
                ShowDate("evt_001_d2", "2026.04.12 (토) 21:00", "DAY 1 (야간)"),
                ShowDate("evt_001_d3", "2026.04.13 (일) 18:00", "DAY 2"),
            ),
            venue = "올림픽체조경기장, 서울",
            region = "서울",
            poster = "$P/aespa.webp",
            status = ShowStatus.ON_SALE,
            maxPerUser = 4,
            openDate = "2026-04-12",
            grades = listOf(
                Grade("VIP", 180000, 12, "#f59e0b"),
                Grade("R석", 140000, 34, "#ef4444"),
                Grade("S석", 110000, 87, "#3b82f6"),
                Grade("A석", 88000, 152, "#22c55e"),
            ),
            description = "aespa의 첫 번째 월드 투어. 서울 단독 공연으로 펼쳐지는 화려한 무대.",
            refundRules = listOf(
                RefundRule("rr_001_1", 7, 0f, "공연 7일 전까지"),
                RefundRule("rr_001_2", 3, 0.1f, "공연 3일 전까지"),
                RefundRule("rr_001_3", 1, 0.3f, "공연 1일 전까지"),
                RefundRule("rr_001_4", 0, 1f, "공연 당일 이후"),
            ),
        ),
        Show(
            id = "evt_002",
            name = "METALLICA M72 WORLD TOUR",
            artistName = "Metallica",
            date = "2026.05.24 (토) ~ 05.25 (일)",
            dates = listOf(
                ShowDate("evt_002_d1", "2026.05.24 (토) 18:00", "DAY 1"),
                ShowDate("evt_002_d2", "2026.05.25 (일) 18:00", "DAY 2"),
            ),
            venue = "고척스카이돔, 서울",
            region = "서울",
            poster = "$P/metallica.webp",
            status = ShowStatus.ON_SALE,
            maxPerUser = 2,
            openDate = "2026-05-24",
            grades = listOf(
                Grade("VIP PIT", 250000, 0, "#f59e0b"),
                Grade("GA PIT", 180000, 5, "#ef4444"),
                Grade("FLOOR", 150000, 44, "#3b82f6"),
                Grade("STAND", 120000, 103, "#22c55e"),
            ),
            description = "메탈리카의 M72 월드투어 한국 공연. 역사적인 무대를 경험하세요.",
            refundRules = listOf(
                RefundRule("rr_002_1", 10, 0f, "공연 10일 전까지"),
                RefundRule("rr_002_2", 5, 0.1f, "공연 5일 전까지"),
                RefundRule("rr_002_3", 2, 0.3f, "공연 2일 전까지"),
                RefundRule("rr_002_4", 0, 1f, "공연 1일 전 및 당일"),
            ),
        ),
        Show(
            id = "evt_003",
            artistName = "Seoul Philharmonic Orchestra",
            name = "서울 필하모닉 뉴이어 콘서트",
            date = "2026.01.01 (수) 15:00",
            venue = "예술의전당 콘서트홀, 서울",
            region = "서울",
            poster = "$P/philharmonic.webp",
            status = ShowStatus.SOLD_OUT,
            maxPerUser = 6,
            openDate = "2026-01-01",
            grades = listOf(
                Grade("VIP", 120000, 0, "#f59e0b"),
                Grade("R석", 90000, 0, "#ef4444"),
                Grade("S석", 70000, 0, "#3b82f6"),
            ),
            description = "새해를 여는 서울 필하모닉의 특별 갈라 콘서트.",
            refundRules = listOf(
                RefundRule("rr_003_1", 14, 0f, "공연 14일 전까지"),
                RefundRule("rr_003_2", 7, 0.1f, "공연 7일 전까지"),
                RefundRule("rr_003_3", 1, 0.3f, "공연 1일 전까지"),
                RefundRule("rr_003_4", 0, 1f, "공연 당일 이후"),
            ),
        ),
        Show(
            id = "evt_004",
            name = "ULTRA KOREA 2026",
            artistName = "Martin Garrix, Armin van Buuren 외",
            date = "2026.06.07 (토) ~ 06.08 (일)",
            dates = listOf(
                ShowDate("evt_004_d1", "2026.06.07 (토) 14:00", "DAY 1"),
                ShowDate("evt_004_d2", "2026.06.08 (일) 14:00", "DAY 2"),
            ),
            venue = "잠실종합운동장, 서울",
            region = "서울",
            poster = "$P/ultrakorea.webp",
            status = ShowStatus.ON_SALE,
            maxPerUser = 4,
            openDate = "2026-06-07",
            grades = listOf(
                Grade("VIP", 300000, 8, "#f59e0b"),
                Grade("GA", 220000, 210, "#3b82f6"),
            ),
            description = "세계 최대 EDM 페스티벌 울트라코리아 2026.",
            refundRules = listOf(
                RefundRule("rr_004_1", 14, 0f, "공연 14일 전까지"),
                RefundRule("rr_004_2", 7, 0.1f, "공연 7일 전까지"),
                RefundRule("rr_004_3", 3, 0.3f, "공연 3일 전까지"),
                RefundRule("rr_004_4", 0, 1f, "공연 2일 전 및 당일"),
            ),
        ),
        Show(
            id = "evt_005",
            name = "JARASUM JAZZ FESTIVAL",
            artistName = "Pat Metheny, Snarky Puppy 외",
            date = "2026.10.05 (토) 12:00",
            venue = "자라섬, 가평",
            region = "경기",
            poster = "$P/jarasum.webp",
            status = ShowStatus.SOLD_OUT,
            maxPerUser = 8,
            openDate = "2026-10-05",
            grades = listOf(
                Grade("1일권", 88000, 0, "#22c55e"),
                Grade("2일권", 150000, 0, "#3b82f6"),
            ),
            description = "아시아 최대 재즈 축제 자라섬 국제 재즈 페스티벌.",
            refundRules = listOf(
                RefundRule("rr_005_1", 7, 0f, "공연 7일 전까지"),
                RefundRule("rr_005_2", 3, 0.1f, "공연 3일 전까지"),
                RefundRule("rr_005_3", 1, 0.3f, "공연 1일 전까지"),
                RefundRule("rr_005_4", 0, 1f, "공연 당일 이후"),
            ),
        ),
    )

    // ── Home screen data ──

    val bannerSlides = listOf(
        BannerSlide("b1", "evt_004", "$P/ultrakorea.webp",
            "2026 울트라코리아", "ULTRA KOREA IS COMING", "잠실종합운동장, 서울", "2026.6.7 - 2026.6.8"),
        BannerSlide("b2", "evt_001", "$P/aespa.webp",
            "2026 aespa 월드 투어", "AESPA WORLD TOUR", "올림픽체조경기장, 서울", "2026.4.12"),
        BannerSlide("b3", "evt_002", "$P/metallica.webp",
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
        RankingItem(1, "evt_001", "AESPA WORLD TOUR 2026", "올림픽체조경기장", "$P/aespa.webp", "콘서트"),
        RankingItem(2, "evt_002", "METALLICA M72 WORLD TOUR", "고척스카이돔", "$P/metallica.webp", "콘서트"),
        RankingItem(3, "evt_004", "ULTRA KOREA 2026", "잠실종합운동장", "$P/ultrakorea.webp", "콘서트"),
        RankingItem(4, "evt_003", "서울 필하모닉 뉴이어 콘서트", "예술의전당", "$P/philharmonic.webp", "클래식"),
        RankingItem(5, "evt_005", "JARASUM JAZZ FESTIVAL", "자라섬, 가평", "$P/jarasum.webp", "콘서트"),
    )

    val openSchedule = listOf(
        OpenScheduleItem("op_001", "evt_001", "AESPA WORLD TOUR 2026",
            "오늘 18:00", "일반예매", listOf("단독판매"), "$P/aespa.webp", true),
        OpenScheduleItem("op_002", "evt_002", "METALLICA M72 WORLD TOUR",
            "내일 20:00", "멤버십 선구매", listOf("단독판매"), "$P/metallica.webp", false),
        OpenScheduleItem("op_003", "evt_003", "서울 필하모닉 뉴이어 콘서트",
            "03.03(화) 18:00", "일반예매", listOf("HOT", "단독판매"), "$P/philharmonic.webp", false),
        OpenScheduleItem("op_004", "evt_004", "ULTRA KOREA 2026",
            "03.10(화) 10:00", "얼리버드", listOf("단독판매"), "$P/ultrakorea.webp", false),
    )

    val discountItems = listOf(
        DiscountItem("dc_001", "evt_005", "JARASUM JAZZ FESTIVAL",
            "자라섬, 가평", "2026.10.5 ~ 10.7", "전석 할인", 40, 88000, "00:00:00", true, "$P/jarasum.webp"),
        DiscountItem("dc_002", "evt_003", "서울 필하모닉 뉴이어 콘서트",
            "예술의전당 콘서트홀", "2026.1.1 ~ 1.3", "R석 할인", 50, 45000, "D-10 10:24:33", true, "$P/philharmonic.webp"),
        DiscountItem("dc_003", "evt_001", "AESPA WORLD TOUR 2026",
            "올림픽체조경기장", "2026.4.12", "S석 할인", 20, 88000, "10:24:33", false, "$P/aespa.webp"),
    )

    // ── Tickets ──

    val mockTickets = listOf(
        Ticket("tkt_001", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa.webp", "evt_001_C3", "C열 3번", "R석", 140000, TicketStatus.SOLD),
        Ticket("tkt_002", "evt_002", "METALLICA M72 WORLD TOUR", "2025.05.24 (토) 18:00",
            "고척스카이돔, 서울", "$P/metallica.webp", "evt_002_B7", "B열 7번", "FLOOR", 150000, TicketStatus.LISTED, resalePrice = 145000),
        Ticket("tkt_003", "evt_005", "JARASUM JAZZ FESTIVAL", "2024.10.05 (토) 12:00",
            "자라섬, 가평", "$P/jarasum.webp", "evt_005_A2", "A열 2번", "2일권", 150000, TicketStatus.USED, attendedDate = "2024.10.05"),
        Ticket("tkt_004", "evt_003", "서울 필하모닉 뉴이어 콘서트", "2025.01.01 (수) 15:00",
            "예술의전당 콘서트홀, 서울", "$P/philharmonic.webp", "evt_003_D5", "D열 5번", "R석", 90000, TicketStatus.EXPIRED),
        Ticket("tkt_005", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa.webp", "evt_001_A1", "A열 1번", "VIP", 180000, TicketStatus.USED, attendedDate = "2025.04.12"),
        Ticket("tkt_006", "evt_002", "METALLICA M72 WORLD TOUR", "2025.05.24 (토) 18:00",
            "고척스카이돔, 서울", "$P/metallica.webp", "evt_002_F11", "F열 11번", "GA PIT", 180000, TicketStatus.USED, attendedDate = "2025.05.24"),
    )

    // ── Resale ──

    val mockResaleItems = listOf(
        ResaleItem("rs_001", "tkt_002", "evt_002", "METALLICA M72 WORLD TOUR", "2025.05.24 (토) 18:00",
            "고척스카이돔, 서울", "$P/metallica.webp", "B열 7번", "FLOOR", 150000, 145000, "user_001"),
        ResaleItem("rs_002", "tkt_ext_001", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa.webp", "E열 4번", "S석", 110000, 105000, "user_042"),
        ResaleItem("rs_003", "tkt_ext_002", "evt_004", "ULTRA KOREA 2025", "2025.06.07 (토) 14:00",
            "잠실종합운동장, 서울", "$P/ultrakorea.webp", "GA 구역 215번", "GA", 220000, 210000, "user_088"),
        ResaleItem("rs_004", "tkt_ext_003", "evt_001", "AESPA WORLD TOUR 2025", "2025.04.12 (토) 19:00",
            "올림픽체조경기장, 서울", "$P/aespa.webp", "A열 1번", "VIP", 180000, 175000, "user_019"),
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

    fun generateSeats(showId: String, gradeName: String? = null): List<Seat> {
        val show = mockShows.find { it.id == showId } ?: return emptyList()
        val grades = show.grades
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
                        id = "${showId}_${grade.name}_${rowLetters[r]}$num",
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

    // ── Venue presets for seat map demo ──

    data class VenueInfo(val name: String, val icon: String)

    val venuePresets = listOf(
        VenueInfo("올림픽체조경기장", "\uD83C\uDFDF\uFE0F"),
        VenueInfo("블루스퀘어", "\uD83C\uDFAD"),
        VenueInfo("세종문화회관", "\uD83C\uDFB5"),
        VenueInfo("고척스카이돔", "\u26BE"),
        VenueInfo("야외원형극장", "\uD83C\uDF3F"),
    )

    fun generateVenuePreset(index: Int): List<SeatMapSection> = when (index) {
        0 -> venueOlympic()
        1 -> venueBlueSquare()
        2 -> venueSejong()
        3 -> venueGocheok()
        4 -> venueOutdoor()
        else -> venueOlympic()
    }

    /** 기존 API 호환용 */
    fun generateSeatMapSections(showId: String): List<SeatMapSection> = venueOlympic()

    // ── Section definition helpers ──

    private data class SecDef(
        val name: String,
        val grade: String,
        val price: Int,
        val color: String,
        val rowDefs: List<Triple<Int, Int, Int>>,  // (globalRow, colStart, colEnd)
    )

    /** 단일 행 정의 */
    private fun r(row: Int, cs: Int, ce: Int) = Triple(row, cs, ce)

    /** 연속 행 정의 (동일 열 범위) */
    private fun rr(rs: Int, re: Int, cs: Int, ce: Int) = (rs..re).map { Triple(it, cs, ce) }

    private fun buildSections(defs: List<SecDef>): List<SeatMapSection> {
        var seatId = 1L
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return defs.mapIndexed { idx, def ->
            val seats = mutableListOf<SectionSeat>()
            val sortedRows = def.rowDefs.map { it.first }.distinct().sorted()
            for ((globalRow, colStart, colEnd) in def.rowDefs) {
                val localRowIdx = sortedRows.indexOf(globalRow)
                val rowLetter = letters[localRowIdx.coerceIn(0, 25)]
                var seatNum = 1
                for (col in colStart..colEnd) {
                    val m7 = (seatId % 7).toInt()
                    val status = when {
                        m7 == 2 || m7 == 5 -> "SOLD"
                        (seatId % 11).toInt() == 3 -> "LOCKED"
                        else -> "AVAILABLE"
                    }
                    seats.add(SectionSeat(seatId, seatId, globalRow, col, "$rowLetter$seatNum", status))
                    seatId++
                    seatNum++
                }
            }
            SeatMapSection((idx + 1).toLong(), def.name, def.grade, def.price, def.color, seats)
        }
    }

    // ── Venue 0: 올림픽체조경기장 ──
    //  VIP 중앙 좁은 구역, R석 좌/우 분리, S석 3분할 (좌측 뒷줄 좁아짐), A석 4분할
    private fun venueOlympic() = buildSections(listOf(
        SecDef("VIP", "VIP", 180000, "#f59e0b",
            rr(1, 3, 10, 26)),
        SecDef("R석 좌", "R석", 140000, "#ef4444",
            rr(5, 8, 1, 12)),
        SecDef("R석 우", "R석", 140000, "#ef4444",
            rr(5, 8, 24, 35)),
        SecDef("S석 좌", "S석", 110000, "#3b82f6",
            rr(10, 12, 1, 10) + listOf(r(13, 3, 10), r(14, 3, 10))),
        SecDef("S석 중앙", "S석", 110000, "#3b82f6",
            rr(10, 14, 13, 23)),
        SecDef("S석 우", "S석", 110000, "#3b82f6",
            rr(10, 12, 26, 35) + listOf(r(13, 26, 33), r(14, 26, 33))),
        SecDef("A석 좌", "A석", 88000, "#22c55e",
            rr(16, 19, 1, 8) + listOf(r(20, 3, 8), r(21, 3, 8))),
        SecDef("A석 중앙좌", "A석", 88000, "#22c55e",
            rr(16, 21, 10, 17)),
        SecDef("A석 중앙우", "A석", 88000, "#22c55e",
            rr(16, 21, 19, 26)),
        SecDef("A석 우", "A석", 88000, "#22c55e",
            rr(16, 19, 28, 35) + listOf(r(20, 28, 33), r(21, 28, 33))),
    ))

    // ── Venue 1: 블루스퀘어 (소극장) ──
    //  소규모 공연장. 부채꼴로 뒤로 갈수록 넓어짐.
    private fun venueBlueSquare() = buildSections(listOf(
        SecDef("VIP", "VIP", 150000, "#f59e0b",
            rr(1, 3, 8, 18)),
        SecDef("R석", "R석", 120000, "#ef4444",
            listOf(r(5, 10, 16), r(6, 9, 17), r(7, 8, 18), r(8, 7, 19), r(9, 6, 20))),
        SecDef("S석", "S석", 90000, "#3b82f6",
            listOf(r(11, 6, 20), r(12, 5, 21), r(13, 4, 22), r(14, 3, 23), r(15, 3, 23), r(16, 2, 24))),
    ))

    // ── Venue 2: 세종문화회관 (2층 구조) ──
    //  1층 VIP + 좌/우 쐐기형 사이드 + 후방, 2층 3분할. 층간 갭 존재.
    private fun venueSejong() = buildSections(listOf(
        SecDef("1층 VIP", "VIP", 150000, "#f59e0b",
            rr(1, 5, 8, 22)),
        SecDef("1층 좌", "R석", 120000, "#ef4444",
            listOf(r(2, 5, 6), r(3, 4, 6), r(4, 3, 6), r(5, 2, 6), r(6, 1, 6), r(7, 1, 6))),
        SecDef("1층 우", "R석", 120000, "#ef4444",
            listOf(r(2, 24, 25), r(3, 24, 26), r(4, 24, 27), r(5, 24, 28), r(6, 24, 29), r(7, 24, 29))),
        SecDef("1층 후방", "S석", 110000, "#3b82f6",
            rr(7, 12, 6, 24)),
        SecDef("2층 좌", "A석", 80000, "#22c55e",
            rr(15, 19, 1, 8)),
        SecDef("2층 중앙", "A석", 80000, "#22c55e",
            rr(15, 19, 10, 20)),
        SecDef("2층 우", "A석", 80000, "#22c55e",
            rr(15, 19, 22, 29)),
    ))

    // ── Venue 3: 고척스카이돔 ──
    //  대형 돔. FLOOR 3분할, 1층 스탠드 4분할 (사이드 뒤로 확장), 2층 3분할.
    private fun venueGocheok() = buildSections(listOf(
        SecDef("FLOOR 중앙", "FLOOR", 180000, "#f59e0b",
            rr(1, 3, 12, 24)),
        SecDef("FLOOR 좌", "FLOOR", 180000, "#f59e0b",
            rr(1, 3, 4, 10)),
        SecDef("FLOOR 우", "FLOOR", 180000, "#f59e0b",
            rr(1, 3, 26, 32)),
        SecDef("1층 좌", "1층", 140000, "#ef4444",
            listOf(r(5, 3, 8), r(6, 3, 8), r(7, 2, 8), r(8, 1, 8), r(9, 1, 8), r(10, 1, 8))),
        SecDef("1층 중좌", "1층", 140000, "#ef4444",
            rr(5, 10, 10, 17)),
        SecDef("1층 중우", "1층", 140000, "#ef4444",
            rr(5, 10, 19, 26)),
        SecDef("1층 우", "1층", 140000, "#ef4444",
            listOf(r(5, 28, 33), r(6, 28, 33), r(7, 28, 34), r(8, 28, 35), r(9, 28, 35), r(10, 28, 35))),
        SecDef("2층 좌", "2층", 100000, "#3b82f6",
            rr(12, 16, 1, 6)),
        SecDef("2층 중앙", "2층", 100000, "#3b82f6",
            rr(12, 16, 8, 28)),
        SecDef("2층 우", "2층", 100000, "#3b82f6",
            rr(12, 16, 30, 35)),
    ))

    // ── Venue 4: 야외원형극장 (부채꼴) ──
    //  행이 뒤로 갈수록 양쪽으로 넓어지는 부채꼴. 좌/중앙/우 분리.
    private fun venueOutdoor() = buildSections(listOf(
        SecDef("VIP", "VIP", 160000, "#f59e0b",
            listOf(r(1, 14, 22), r(2, 13, 23), r(3, 12, 24))),
        SecDef("R석 좌", "R석", 130000, "#ef4444",
            listOf(r(5, 10, 14), r(6, 9, 14), r(7, 8, 14))),
        SecDef("R석 중앙", "R석", 130000, "#ef4444",
            rr(5, 7, 15, 21)),
        SecDef("R석 우", "R석", 130000, "#ef4444",
            listOf(r(5, 22, 26), r(6, 22, 27), r(7, 22, 28))),
        SecDef("S석 좌", "S석", 100000, "#3b82f6",
            listOf(r(9, 7, 14), r(10, 6, 14), r(11, 5, 14), r(12, 4, 14), r(13, 3, 14))),
        SecDef("S석 중앙", "S석", 100000, "#3b82f6",
            rr(9, 13, 15, 21)),
        SecDef("S석 우", "S석", 100000, "#3b82f6",
            listOf(r(9, 22, 29), r(10, 22, 30), r(11, 22, 31), r(12, 22, 32), r(13, 22, 33))),
        SecDef("A석 좌", "A석", 70000, "#22c55e",
            listOf(r(15, 3, 14), r(16, 2, 14), r(17, 1, 14), r(18, 1, 14), r(19, 1, 14))),
        SecDef("A석 중앙", "A석", 70000, "#22c55e",
            rr(15, 19, 15, 21)),
        SecDef("A석 우", "A석", 70000, "#22c55e",
            listOf(r(15, 22, 33), r(16, 22, 34), r(17, 22, 35), r(18, 22, 35), r(19, 22, 35))),
    ))

    // ── 좌석 위치 + 구역 경계 계산 (순수 그리드, 워프 없음) ──

    /**
     * rowNum/colNum 기반 순수 그리드 배치:
     *   - 같은 colNum → 정확히 같은 X 좌표
     *   - 같은 rowNum → 정확히 같은 Y 좌표
     * 구역 경계는 convex hull로 자동 계산.
     */
    fun calculateLayout(
        sections: List<SeatMapSection>,
    ): Pair<Map<Long, SeatPosition>, Map<Long, SectionBounds>> {
        if (sections.isEmpty()) return Pair(emptyMap(), emptyMap())

        val cellSize = SEAT_RADIUS * 2 + SEAT_GAP
        val canvasCx = CANVAS_W / 2f
        val startY = 170f

        val allSeats = sections.flatMap { it.seats }
        if (allSeats.isEmpty()) return Pair(emptyMap(), emptyMap())

        val minCol = allSeats.minOf { it.colNum }
        val maxCol = allSeats.maxOf { it.colNum }
        val totalCols = maxCol - minCol + 1
        val gridWidth = totalCols * cellSize
        val minRow = allSeats.minOf { it.rowNum }

        // 순수 그리드 배치: 같은 rowNum → 같은 Y, 같은 colNum → 같은 X
        val seatPositions = mutableMapOf<Long, SeatPosition>()
        for (section in sections) {
            for (seat in section.seats) {
                val x = canvasCx - gridWidth / 2f + (seat.colNum - minCol + 0.5f) * cellSize
                val y = startY + (seat.rowNum - minRow + 0.5f) * cellSize
                seatPositions[seat.sessionSeatId] = SeatPosition(x, y)
            }
        }

        // 각 구역의 convex hull → SectionBounds
        val pad = SEAT_RADIUS + 5f
        val sectionBounds = mutableMapOf<Long, SectionBounds>()
        for (section in sections) {
            val points = mutableListOf<Pair<Float, Float>>()
            for (seat in section.seats) {
                val pos = seatPositions[seat.sessionSeatId] ?: continue
                points.add(Pair(pos.cx - pad, pos.cy - pad))
                points.add(Pair(pos.cx + pad, pos.cy - pad))
                points.add(Pair(pos.cx - pad, pos.cy + pad))
                points.add(Pair(pos.cx + pad, pos.cy + pad))
            }
            if (points.isEmpty()) continue
            val hull = convexHull(points)
            sectionBounds[section.sectionId] = polygonToBounds(hull)
        }

        return Pair(seatPositions, sectionBounds)
    }

    // ── Convex hull (Andrew's monotone chain) ──

    private fun convexHull(points: List<Pair<Float, Float>>): List<Pair<Float, Float>> {
        val sorted = points.sortedWith(compareBy({ it.first }, { it.second }))
        if (sorted.size <= 2) return sorted

        fun cross(o: Pair<Float, Float>, a: Pair<Float, Float>, b: Pair<Float, Float>): Float =
            (a.first - o.first) * (b.second - o.second) -
                    (a.second - o.second) * (b.first - o.first)

        val lower = mutableListOf<Pair<Float, Float>>()
        for (p in sorted) {
            while (lower.size >= 2 && cross(lower[lower.size - 2], lower.last(), p) <= 0)
                lower.removeAt(lower.lastIndex)
            lower.add(p)
        }
        val upper = mutableListOf<Pair<Float, Float>>()
        for (p in sorted.reversed()) {
            while (upper.size >= 2 && cross(upper[upper.size - 2], upper.last(), p) <= 0)
                upper.removeAt(upper.lastIndex)
            upper.add(p)
        }
        lower.removeAt(lower.lastIndex)
        upper.removeAt(upper.lastIndex)
        return lower + upper
    }

    /** polygon 꼭짓점 → SectionBounds (bounding box + polygon) */
    private fun polygonToBounds(polygon: List<Pair<Float, Float>>): SectionBounds {
        val xs = polygon.map { it.first }
        val ys = polygon.map { it.second }
        val minX = xs.min()
        val maxX = xs.max()
        val minY = ys.min()
        val maxY = ys.max()
        return SectionBounds(
            left = minX,
            top = minY,
            width = maxX - minX,
            height = maxY - minY,
            polygon = polygon,
        )
    }

    // 캔버스 상수 (SeatMapScreen과 동일)
    private const val CANVAS_W = 1000f
    private const val SEAT_RADIUS = 10f
    private const val SEAT_GAP = 4f

    fun getResaleGrouped(): List<ResaleGroupItem> =
        mockResaleItems.groupBy { it.showId }.map { (showId, items) ->
            ResaleGroupItem(
                showId = showId,
                showName = items.first().showName,
                poster = items.first().poster,
                venue = items.first().venue,
                showDate = items.first().showDate,
                count = items.size,
                minPrice = items.minOf { it.resalePrice },
                items = items,
            )
        }
}
