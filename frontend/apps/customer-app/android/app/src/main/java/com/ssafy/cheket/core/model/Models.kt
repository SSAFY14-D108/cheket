package com.ssafy.cheket.core.model

enum class TicketStatus { SOLD, LISTED, USED, EXPIRED }
enum class ShowStatus { ON_SALE, SOLD_OUT }
enum class SeatStatus { AVAILABLE, LOCKED, SOLD }

enum class TxType { PURCHASE, RESALE_LIST, RESALE_BUY, TRANSFER }
enum class TxStatus { PENDING, CONFIRMING, CONFIRMED, FAILED }

enum class WalletTxType {
    CHARGE, PURCHASE, RESALE_BUY, RESALE_SELL, TRANSFER_SEND, TRANSFER_RECEIVE
}

enum class PurchaseFailureReason {
    SOLD_OUT, LOCK_FAILED, LIMIT_EXCEEDED, INSUFFICIENT_BALANCE, NETWORK
}

enum class WaitingQueueState { WAITING, READY_TO_ENTER, EXPIRED }

data class Grade(
    val name: String,
    val price: Int,
    val remaining: Int,
    val color: String? = null,
)

data class ShowDate(
    val id: String,
    val label: String,      // e.g. "2026.04.12 (토) 18:00"
    val day: String = "",   // e.g. "DAY 1"
)

data class RefundRule(
    val id: String,
    val daysBefore: Int,
    val feeRate: Float,
    val label: String,
)

data class Show(
    val id: String,
    val name: String,
    val artistName: String? = null,
    val date: String,
    val dates: List<ShowDate> = emptyList(),
    val venue: String,
    val region: String,
    val poster: String,
    val status: ShowStatus,
    val maxPerUser: Int,
    val grades: List<Grade>,
    val openDate: String? = null,
    val description: String? = null,
    val refundRules: List<RefundRule> = emptyList(),
)

data class Ticket(
    val id: String,
    val showId: String,
    val showName: String,
    val showDate: String,
    val venue: String,
    val poster: String,
    val seatId: String,
    val seatLabel: String,
    val grade: String,
    val originalPrice: Int,
    val status: TicketStatus,
    val resalePrice: Int? = null,
    val attendedDate: String? = null,
)

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val walletAddress: String,
    val ctkBalance: Int,
)

data class Seat(
    val id: String,
    val row: String,
    val number: Int,
    val grade: String,
    val price: Int,
    val status: SeatStatus,
)

data class ResaleItem(
    val id: String,
    val ticketId: String,
    val showId: String,
    val showName: String,
    val showDate: String,
    val venue: String,
    val poster: String,
    val seatLabel: String,
    val grade: String,
    val originalPrice: Int,
    val resalePrice: Int,
    val sellerId: String,
)

data class ResaleGroupItem(
    val showId: String,
    val showName: String,
    val poster: String,
    val venue: String,
    val showDate: String,
    val count: Int,
    val minPrice: Int,
    val items: List<ResaleItem>,
)

data class BannerSlide(
    val id: String,
    val showId: String,
    val image: String,
    val title: String,
    val subtitle: String,
    val venue: String,
    val dates: String,
)

data class CategoryIcon(
    val id: String,
    val label: String,
    val emoji: String,
)

data class RankingItem(
    val rank: Int,
    val showId: String,
    val name: String,
    val venue: String,
    val poster: String,
    val genre: String,
)

data class OpenScheduleItem(
    val id: String,
    val showId: String,
    val name: String,
    val openLabel: String,
    val openType: String,
    val tags: List<String>,
    val poster: String,
    val isToday: Boolean,
)

data class DiscountItem(
    val id: String,
    val showId: String,
    val name: String,
    val venue: String,
    val dates: String,
    val discountType: String,
    val discountPct: Int,
    val finalPrice: Int,
    val countdownLabel: String,
    val isTimeDeal: Boolean,
    val poster: String,
)

/** Blockchain transaction record */
data class TxRecord(
    val id: String,
    val txHash: String,
    val type: TxType,
    val status: TxStatus,
    val label: String,
    val amount: Int? = null,
    val createdAt: Long,
    val confirmedAt: Long? = null,
    val confirmations: Int = 0,
    val errorMessage: String? = null,
)

/** Wallet (CTK) transaction */
data class WalletTx(
    val id: String,
    val type: WalletTxType,
    val label: String,
    val amount: Int,        // positive for in, negative for out
    val balance: Int,       // balance after transaction
    val createdAt: Long,
)

data class TransferResult(
    val success: Boolean,
    val recipientName: String? = null,
    val reason: String? = null,
)
