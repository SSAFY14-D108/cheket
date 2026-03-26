package com.ssafy.cheket.core.navigation

import com.ssafy.cheket.core.model.Seat
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.Show

/**
 * Navigation 간 복잡한 데이터를 전달하기 위한 싱글톤.
 * (route 파라미터로는 primitive만 전달 가능하므로)
 */
object NavParams {
    var selectedSeats: List<Seat> = emptyList()
    var totalPrice: Int = 0
    var failureReason: String = ""
    var recipientName: String = ""
    var recipientPhone: String = ""
    var transferFailureReason: String = ""
    var transferTransactionId: Long = 0L
    var selectedTicket: Ticket? = null
    var showInfo: Show? = null
    var sessionId: Long = 0L
    var seatAccessToken: String? = null
    var seatAccessExpiresAt: String? = null
    var initialSortOption: String? = null // ShowsScreen 초기 정렬 (예: "OPEN_SOON")

    fun clearPurchase() {
        selectedSeats = emptyList()
        totalPrice = 0
        failureReason = ""
        showInfo = null
        sessionId = 0L
        seatAccessToken = null
        seatAccessExpiresAt = null
    }

    fun clearTransfer() {
        recipientName = ""
        recipientPhone = ""
        transferFailureReason = ""
        transferTransactionId = 0L
    }

    fun clearSelectedTicket() {
        selectedTicket = null
    }
}
