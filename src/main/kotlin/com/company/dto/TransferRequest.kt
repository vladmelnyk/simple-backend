package com.company.dto

import java.math.BigDecimal

data class TransferRequest(
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal,
    val requestId: String
)