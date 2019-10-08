package com.company.repository

import com.company.entity.Accounts
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class Account(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Account>(Accounts)

    var currency by Accounts.currency
    var balance by Accounts.balance
    var user by User referencedOn Accounts.userId
}