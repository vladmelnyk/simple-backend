package com.company.repository

import com.company.entity.Accounts
import com.company.entity.Users
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var email by Users.email

    val accounts by Account referrersOn Accounts.userId

}