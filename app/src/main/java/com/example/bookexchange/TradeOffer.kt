package com.example.bookexchange

data class TradeOffer(
    var id: String = "",
    val bookWantedId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val status: String = "",
    val offeredBookData: Map<String, String> = emptyMap(),
    val timestamp: Long = 0
)