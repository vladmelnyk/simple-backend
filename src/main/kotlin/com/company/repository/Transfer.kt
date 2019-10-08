package com.company.repository

import com.company.entity.Transfers
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class Transfer(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Transfer>(Transfers)

    var fromAccount by Account referencedOn Transfers.fromAccount
    var toAccount by Account referencedOn Transfers.toAccount
    var requestId by Transfers.requestId
    var amount by Transfers.amount
    var receipt by Transfers.receipt
}