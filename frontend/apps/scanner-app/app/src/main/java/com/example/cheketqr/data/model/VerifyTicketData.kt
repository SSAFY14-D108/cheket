package com.example.cheketqr.data.model

data class VerifyTicketData(
    val ticketId: Long,
    val title: String,
    val seatNo: String,
    val ownerName: String,
    val status: String
)
