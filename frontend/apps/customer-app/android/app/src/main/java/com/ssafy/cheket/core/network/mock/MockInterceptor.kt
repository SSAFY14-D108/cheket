package com.ssafy.cheket.core.network.mock

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * HTTP 레벨에서 API 응답을 mock하는 Interceptor.
 * 실제 서버 없이 전체 Retrofit 파이프라인(Service → DTO → 역직렬화)을 테스트할 수 있다.
 * 서버 준비되면 이 Interceptor만 제거하면 됨.
 */
class MockInterceptor : Interceptor {

    private val json = "application/json".toMediaType()

    companion object {
        private const val P = "file:///android_asset/posters"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        val mockBody = route(method, path)
            ?: return chain.proceed(request) // 매칭 안 되면 실제 서버로

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(mockBody.toResponseBody(json))
            .build()
    }

    private fun route(method: String, path: String): String? {
        return when {
            // ── Auth ──
            method == "POST" && path.endsWith("/auth/login") -> authLogin()
            method == "POST" && path.endsWith("/auth/logout") -> ok("로그아웃 성공")
            method == "POST" && path.endsWith("/auth/reissue") -> authLogin() // same shape
            method == "POST" && path.endsWith("/users") && !path.contains("likes") && !path.contains("notifications") -> created("회원가입 성공")
            method == "POST" && path.endsWith("/auth/sms/send") -> ok("인증번호 발송 완료")
            method == "POST" && path.endsWith("/auth/sms/verify") -> smsVerify()
            method == "POST" && path.endsWith("/auth/password") -> ok("인증코드 발송")
            method == "PATCH" && path.endsWith("/auth/password") -> ok("비밀번호 재설정 완료")
            method == "POST" && path.endsWith("/auth/email") -> emailFind()
            method == "GET" && path.endsWith("/auth/search") -> userSearch()

            // ── User ──
            method == "GET" && path.endsWith("/users") -> userInfo()
            method == "DELETE" && path.endsWith("/users") -> ok("회원 탈퇴 완료")
            method == "GET" && path.endsWith("/users/likes") -> likedShows()
            method == "PUT" && path.endsWith("/users/notifications") -> ok("알림 설정 변경 완료")

            // ── Show ──
            method == "GET" && path.endsWith("/shows/upcoming") -> upcomingShows()
            method == "GET" && path.matches(Regex(".*/shows/\\d+/sessions/\\d+/seats")) -> seats()
            method == "GET" && path.matches(Regex(".*/shows/\\d+/sessions")) -> sessions()
            method == "GET" && path.matches(Regex(".*/shows/\\d+/refund")) -> refundPolicy()
            method == "GET" && path.matches(Regex(".*/shows/\\d+")) && !path.contains("sessions") && !path.contains("likes") && !path.contains("refund") -> showDetail()
            method == "GET" && path.endsWith("/shows") -> showList()
            method == "POST" && path.matches(Regex(".*/shows/\\d+/likes")) -> created("찜 추가 완료")
            method == "DELETE" && path.matches(Regex(".*/shows/\\d+/likes")) -> ok("찜 해제 완료")

            // ── Ticket ──
            method == "POST" && path.matches(Regex(".*/sessions/\\d+/purchase")) -> purchase()
            method == "POST" && path.matches(Regex(".*/sessions/\\d+/seats")) -> ok("좌석 선점 완료")
            method == "GET" && path.endsWith("/tickets/upcoming") -> upcomingTickets()
            method == "GET" && path.endsWith("/tickets/collection") -> collectionTickets()
            method == "POST" && path.matches(Regex(".*/tickets/\\d+/transfer")) -> ok("양도 완료")
            method == "POST" && path.matches(Regex(".*/tickets/\\d+/qr")) -> qrCode()
            method == "POST" && path.matches(Regex(".*/tickets/\\d+/refund")) -> ok("환불 완료")

            // ── Queue ──
            method == "POST" && path.matches(Regex(".*/queue/enter")) -> queueEnter()
            method == "POST" && path.matches(Regex(".*/queue")) && !path.contains("enter") -> queueEntry()
            method == "GET" && path.matches(Regex(".*/queue/status")) -> queueStatus()
            method == "DELETE" && path.matches(Regex(".*/queue")) -> ok("대기열 이탈 완료")

            // ── Wallet ──
            method == "GET" && path.endsWith("/wallets/balance") -> walletBalance()
            method == "GET" && path.matches(Regex(".*/wallets/transactions/\\d+")) -> txStatus()
            method == "GET" && path.endsWith("/wallets/transactions") -> walletTransactions()

            // ── Resale ──
            method == "GET" && path.matches(Regex(".*/resales/\\d+")) -> resaleTickets()
            method == "GET" && path.endsWith("/resales") -> resaleShows()
            method == "POST" && path.matches(Regex(".*/resales/\\d+/purchase")) -> ok("2차 거래 티켓 구매 완료")
            method == "POST" && path.matches(Regex(".*/resales/\\d+")) -> ok("2차 거래 등록 완료")
            method == "DELETE" && path.matches(Regex(".*/resales/\\d+")) -> ok("2차 거래 등록 취소 완료")

            else -> null
        }
    }

    // ══════════════════════════════════════════
    //  Auth
    // ══════════════════════════════════════════

    private fun authLogin() = wrap("""
        {"accessToken":"mock-access-token-abc123","refreshToken":"mock-refresh-token-xyz789"}
    """)

    private fun smsVerify() = wrap("""{"verified":true}""")

    private fun emailFind() = wrap("""{"email":"minjun.kim@cheket.app"}""")

    private fun userSearch() = wrap("""{"userId":1,"name":"박지연","phone":"010-9876-5432"}""")

    // ══════════════════════════════════════════
    //  User
    // ══════════════════════════════════════════

    private fun userInfo() = wrap("""
        {"userId":1,"username":"김민준","phoneNumber":"010-1234-5678","email":"minjun.kim@cheket.app"}
    """)

    private fun likedShows() = wrap("""[
        {"showId":1,"title":"AESPA WORLD TOUR 2026","posterUrl":"$P/aespa.webp","venue":"올림픽체조경기장","showDate":"2026-04-12","status":"ON_SALE"},
        {"showId":4,"title":"ULTRA KOREA 2026","posterUrl":"$P/ultrakorea.webp","venue":"잠실종합운동장","showDate":"2026-06-07","status":"ON_SALE"}
    ]""")

    // ══════════════════════════════════════════
    //  Show
    // ══════════════════════════════════════════

    private fun showList() = wrap("""{
        "shows":[
            {"showId":1,"title":"AESPA WORLD TOUR 2026","posterUrl":"$P/aespa.webp","venue":"올림픽체조경기장, 서울","showStartDate":"2026-04-12","showEndDate":"2026-04-13","region":"SEOUL","showStatus":"ON_SALE"},
            {"showId":2,"title":"METALLICA M72 WORLD TOUR","posterUrl":"$P/metallica.webp","venue":"고척스카이돔, 서울","showStartDate":"2026-05-24","showEndDate":"2026-05-25","region":"SEOUL","showStatus":"ON_SALE"},
            {"showId":3,"title":"서울 필하모닉 뉴이어 콘서트","posterUrl":"$P/philharmonic.webp","venue":"예술의전당 콘서트홀, 서울","showStartDate":"2026-01-01","showEndDate":null,"region":"SEOUL","showStatus":"SOLD_OUT"},
            {"showId":4,"title":"ULTRA KOREA 2026","posterUrl":"$P/ultrakorea.webp","venue":"잠실종합운동장, 서울","showStartDate":"2026-06-07","showEndDate":"2026-06-08","region":"SEOUL","showStatus":"ON_SALE"},
            {"showId":5,"title":"JARASUM JAZZ FESTIVAL","posterUrl":"$P/jarasum.webp","venue":"자라섬, 가평","showStartDate":"2026-10-05","showEndDate":null,"region":"GYEONGGI","showStatus":"SOLD_OUT"}
        ],
        "page":0,"size":20,"totalElements":5,"totalPages":1
    }""")

    private fun showDetail() = wrap("""{
        "showId":1,
        "title":"AESPA WORLD TOUR 2026",
        "posterUrl":"$P/aespa.webp",
        "description":"aespa의 첫 번째 월드 투어. 서울 단독 공연으로 펼쳐지는 화려한 무대.",
        "artist":"aespa",
        "venue":"올림픽체조경기장, 서울",
        "showStartDate":"2026-04-12",
        "showEndDate":"2026-04-13",
        "region":"SEOUL",
        "showStatus":"ON_SALE",
        "isLiked":true,
        "likeCount":1234,
        "grade":[
            {"gradeId":1,"gradeName":"VIP","price":180000},
            {"gradeId":2,"gradeName":"R석","price":140000},
            {"gradeId":3,"gradeName":"S석","price":110000},
            {"gradeId":4,"gradeName":"A석","price":88000}
        ],
        "refundPolicy":[
            {"daysRemaining":7,"refundRate":100},
            {"daysRemaining":3,"refundRate":50},
            {"daysRemaining":1,"refundRate":0}
        ]
    }""")

    private fun sessions() = wrap("""[
        {"sessionId":1,"sessionDate":"2026-04-12","sessionStartTime":"18:00:00","remainingSeats":150,"totalSeats":500},
        {"sessionId":2,"sessionDate":"2026-04-12","sessionStartTime":"21:00:00","remainingSeats":280,"totalSeats":500},
        {"sessionId":3,"sessionDate":"2026-04-13","sessionStartTime":"18:00:00","remainingSeats":420,"totalSeats":500}
    ]""")

    private fun seats() = wrap("""{
        "sections":[
            {"sectionId":1,"sectionName":"가","seats":[
                {"seatId":1,"seatNo":"A-1","grade":"VIP","price":180000,"status":"AVAILABLE"},
                {"seatId":2,"seatNo":"A-2","grade":"VIP","price":180000,"status":"SOLD"},
                {"seatId":3,"seatNo":"A-3","grade":"VIP","price":180000,"status":"AVAILABLE"},
                {"seatId":4,"seatNo":"A-4","grade":"VIP","price":180000,"status":"HELD"},
                {"seatId":5,"seatNo":"A-5","grade":"VIP","price":180000,"status":"AVAILABLE"},
                {"seatId":6,"seatNo":"B-1","grade":"R석","price":140000,"status":"AVAILABLE"},
                {"seatId":7,"seatNo":"B-2","grade":"R석","price":140000,"status":"AVAILABLE"},
                {"seatId":8,"seatNo":"B-3","grade":"R석","price":140000,"status":"SOLD"},
                {"seatId":9,"seatNo":"B-4","grade":"R석","price":140000,"status":"AVAILABLE"},
                {"seatId":10,"seatNo":"B-5","grade":"R석","price":140000,"status":"PENDING_TX"}
            ]},
            {"sectionId":2,"sectionName":"나","seats":[
                {"seatId":11,"seatNo":"C-1","grade":"S석","price":110000,"status":"AVAILABLE"},
                {"seatId":12,"seatNo":"C-2","grade":"S석","price":110000,"status":"AVAILABLE"},
                {"seatId":13,"seatNo":"C-3","grade":"S석","price":110000,"status":"SOLD"},
                {"seatId":14,"seatNo":"C-4","grade":"S석","price":110000,"status":"AVAILABLE"},
                {"seatId":15,"seatNo":"D-1","grade":"A석","price":88000,"status":"AVAILABLE"},
                {"seatId":16,"seatNo":"D-2","grade":"A석","price":88000,"status":"AVAILABLE"},
                {"seatId":17,"seatNo":"D-3","grade":"A석","price":88000,"status":"AVAILABLE"},
                {"seatId":18,"seatNo":"D-4","grade":"A석","price":88000,"status":"SOLD"},
                {"seatId":19,"seatNo":"D-5","grade":"A석","price":88000,"status":"AVAILABLE"},
                {"seatId":20,"seatNo":"D-6","grade":"A석","price":88000,"status":"AVAILABLE"}
            ]}
        ]
    }""")

    private fun refundPolicy() = wrap("""{
        "refundPolicy":[
            {"daysRemaining":7,"refundRate":100},
            {"daysRemaining":3,"refundRate":50},
            {"daysRemaining":1,"refundRate":0}
        ],
        "showStartDate":"2026-04-12"
    }""")

    private fun upcomingShows() = wrap("""[
        {"showId":1,"title":"AESPA WORLD TOUR 2026","posterUrl":"$P/aespa.webp","venue":"올림픽체조경기장","reservationDate":"2026-04-01T18:00:00","status":"UPCOMING"},
        {"showId":4,"title":"ULTRA KOREA 2026","posterUrl":"$P/ultrakorea.webp","venue":"잠실종합운동장","reservationDate":"2026-05-15T10:00:00","status":"UPCOMING"}
    ]""")

    // ══════════════════════════════════════════
    //  Ticket
    // ══════════════════════════════════════════

    private fun purchase() = wrap("""{"txId":1001}""")

    private fun upcomingTickets() = wrap("""[
        {"ticketId":1,"numbering":1,"posterUrl":"$P/aespa.webp","show":{"showId":1,"name":"AESPA WORLD TOUR 2026","date":"2026-04-12","venue":"올림픽체조경기장, 서울"},"price":140000,"seatId":6,"sectionName":"가","seatNo":"B-1","grade":"R석","status":"UPCOMING"},
        {"ticketId":2,"numbering":2,"posterUrl":"$P/metallica.webp","show":{"showId":2,"name":"METALLICA M72 WORLD TOUR","date":"2026-05-24","venue":"고척스카이돔, 서울"},"price":150000,"seatId":11,"sectionName":"가","seatNo":"B-7","grade":"FLOOR","status":"ON-SALE"}
    ]""")

    private fun collectionTickets() = wrap("""[
        {"ticketId":3,"posterUrl":"$P/jarasum.webp","show":{"showId":5,"name":"JARASUM JAZZ FESTIVAL","date":"2024-10-05","venue":"자라섬, 가평"},"sectionName":"A","seatNo":"A-2","grade":"2일권"},
        {"ticketId":5,"posterUrl":"$P/aespa.webp","show":{"showId":1,"name":"AESPA WORLD TOUR 2025","date":"2025-04-12","venue":"올림픽체조경기장, 서울"},"sectionName":"A","seatNo":"A-1","grade":"VIP"},
        {"ticketId":6,"posterUrl":"$P/metallica.webp","show":{"showId":2,"name":"METALLICA M72 WORLD TOUR","date":"2025-05-24","venue":"고척스카이돔, 서울"},"sectionName":"F","seatNo":"F-11","grade":"GA PIT"}
    ]""")

    private fun qrCode() = wrap("""{
        "qrData":"1:OTP-mock-abc123",
        "expiresAt":"2026-04-12T18:00:30",
        "ticketId":1,
        "title":"AESPA WORLD TOUR 2026",
        "sectionName":"가",
        "seatNo":"B-1"
    }""")

    // ══════════════════════════════════════════
    //  Queue
    // ══════════════════════════════════════════

    private fun queueEntry() = wrap("""{
        "sessionId":1,"position":158,"totalWaiting":4830,"estimatedWaitSec":55
    }""")

    private fun queueStatus() = wrap("""{
        "position":42,"totalWaiting":4750,"estimatedWaitSec":15,"status":"WAITING"
    }""")

    private fun queueEnter() = wrap("""{
        "sessionToken":"mock-session-token-xyz","remainingSec":300,"purchaseLimit":4
    }""")

    // ══════════════════════════════════════════
    //  Wallet
    // ══════════════════════════════════════════

    private fun walletBalance() = wrap("""{
        "balance":2400,"walletAddress":"0x3a9F...dE42"
    }""")

    private fun walletTransactions() = wrap("""[
        {"transactionId":1,"type":"CHARGE","amount":5000,"description":"CTK 충전","createdAt":"2026-02-07T10:00:00"},
        {"transactionId":2,"type":"PURCHASE","amount":-140000,"description":"AESPA WORLD TOUR 2026 - R석","createdAt":"2026-02-17T14:30:00"},
        {"transactionId":3,"type":"CHARGE","amount":3000,"description":"CTK 충전","createdAt":"2026-02-22T09:00:00"},
        {"transactionId":4,"type":"RESALE_SELL","amount":145000,"description":"METALLICA FLOOR - 리세일 수익","createdAt":"2026-02-27T16:00:00"},
        {"transactionId":5,"type":"PURCHASE","amount":-300000,"description":"ULTRA KOREA 2026 - VIP","createdAt":"2026-03-04T11:00:00"}
    ]""")

    private fun txStatus() = wrap("""{
        "txId":1001,"status":"CONFIRMED","type":"PURCHASE","txHash":"0x1a2b3c4d5e6f","blockNumber":12345678,"createdAt":"2026-03-04T11:00:00","updatedAt":"2026-03-04T11:01:00"
    }""")

    // ══════════════════════════════════════════
    //  Resale
    // ══════════════════════════════════════════

    private fun resaleShows() = wrap("""{
        "shows":[
            {"showId":1,"title":"AESPA WORLD TOUR 2026","showStartDate":"2026-04-12","showEndDate":"2026-04-13","venue":"올림픽체조경기장, 서울","region":"SEOUL","posterUrl":"$P/aespa.webp","ticketCount":2},
            {"showId":2,"title":"METALLICA M72 WORLD TOUR","showStartDate":"2026-05-24","showEndDate":"2026-05-25","venue":"고척스카이돔, 서울","region":"SEOUL","posterUrl":"$P/metallica.webp","ticketCount":1},
            {"showId":4,"title":"ULTRA KOREA 2026","showStartDate":"2026-06-07","showEndDate":"2026-06-08","venue":"잠실종합운동장, 서울","region":"SEOUL","posterUrl":"$P/ultrakorea.webp","ticketCount":1}
        ],
        "page":0,"size":20,"totalElements":3,"totalPages":1
    }""")

    private fun resaleTickets() = wrap("""{
        "show":{"showId":1,"title":"AESPA WORLD TOUR 2026","posterUrl":"$P/aespa.webp","venue":"올림픽체조경기장, 서울","region":"SEOUL"},
        "tickets":[
            {"ticketId":101,"session":{"sessionId":1,"sessionDate":"2026-04-12","sessionStartTime":"18:00:00"},"sectionName":"가","seatId":3,"seatNo":"A-3","grade":"VIP","originalPrice":180000,"discountedPrice":170000,"discountRate":5.6},
            {"ticketId":102,"session":{"sessionId":1,"sessionDate":"2026-04-12","sessionStartTime":"18:00:00"},"sectionName":"가","seatId":9,"seatNo":"B-4","grade":"R석","originalPrice":140000,"discountedPrice":130000,"discountRate":7.1}
        ],
        "page":0,"size":20,"totalElements":2,"totalPages":1
    }""")

    // ══════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════

    private fun wrap(dataJson: String) = """
        {"httpStatusCode":200,"responseMessage":"Success","data":${dataJson.trim()}}
    """.trimIndent()

    private fun ok(message: String) = """
        {"httpStatusCode":200,"responseMessage":"$message","data":null}
    """.trimIndent()

    private fun created(message: String) = """
        {"httpStatusCode":201,"responseMessage":"$message","data":null}
    """.trimIndent()
}
