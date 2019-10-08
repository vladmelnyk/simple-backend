package com.company.dto

import com.company.repository.Account
import java.math.BigDecimal

data class TransferDto(
    val fromAccount: Account,
    val toAccount: Account,
    val amount: BigDecimal,
    val requestId: String,
    var receipt: String
)