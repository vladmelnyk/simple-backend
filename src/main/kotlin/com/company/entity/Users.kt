package com.company.entity

import org.jetbrains.exposed.dao.LongIdTable

object Users : LongIdTable() {
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", length = 50)
}