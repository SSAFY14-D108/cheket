package com.ssafy.cheket.core.navigation

import com.ssafy.cheket.core.model.Seat
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
    var showInfo: Show? = null

    fun clearPurchase() {
        selectedSeats = emptyList()
        totalPrice = 0
        failureReason = ""
        showInfo = null
    }

    fun clearTransfer() {
        recipientName = ""
        recipientPhone = ""
        transferFailureReason = ""
    }
}
