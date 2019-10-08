package com.company.dto

import java.math.BigDecimal

data class AccountDto(
    val currency: String = "usd",
    val balance: BigDecimal = BigDecimal.ZERO
)