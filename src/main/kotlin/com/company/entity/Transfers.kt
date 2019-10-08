package com.company.entity

import org.jetbrains.exposed.dao.LongIdTable

object Transfers : LongIdTable() {
    val fromAccount = reference("from_account_id", Accounts)
    val toAccount = reference("to_account_id", Accounts)
    var requestId = varchar("request_id", 40).index(isUnique = true)
    var amount = decimal("amount", precision = 8, scale = 4)
    var receipt = varchar("receipt", 40)
}