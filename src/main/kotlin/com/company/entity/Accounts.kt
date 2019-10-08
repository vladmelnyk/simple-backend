package com.company.entity

import org.jetbrains.exposed.dao.LongIdTable

object Accounts : LongIdTable() {
    var currency = varchar("currency", length = 3)
    var balance = decimal(
        "balance", precision = 8,
        scale = 4
    )
    val userId = reference("user_id", Users)
}