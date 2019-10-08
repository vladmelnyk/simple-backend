package com.company.dto

import java.math.BigDecimal

data class DepositRequest(
    val amount: BigDecimal,
    val requestId: String
)