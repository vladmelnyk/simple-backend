package com.company.service

import com.company.dto.AccountDto
import com.company.dto.TransferDto
import com.company.dto.TransferResponse
import com.company.dto.UserDto
import com.company.entity.Accounts
import com.company.entity.Transfers
import com.company.entity.Users
import com.company.repository.Account
import com.company.repository.Transfer
import com.company.repository.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class RepositoryService(
    private val db: Database
) {

    fun tearDown() {
        transaction(db) {
            SchemaUtils.drop(Users, Accounts, Transfers)
        }
    }

    fun addAccount(userFound: User, accountDto: AccountDto): Long? {
        return kotlin.runCatching {
            transaction(db) {
                Account.new {
                    balance = accountDto.balance
                    currency = accountDto.currency
                    user = userFound
                }.id.value
            }
        }.getOrNull()
    }

    fun updateAccount(account: Account, amount: BigDecimal): Account? {
        return kotlin.runCatching {
            transaction(db) {
                account.balance = amount
                account
            }
        }.getOrNull()
    }

    fun deleteAccount(account: Account): Long? {
        return kotlin.runCatching {
            transaction(db) {
                account.delete()
                account.id.value
            }
        }.getOrNull()
    }

    fun getAccount(id: Long): Account? {
        return kotlin.runCatching {
            transaction(db) {
                Account.findById(id)!!
            }
        }.getOrNull()
    }

    fun getAccountsForUser(user: User): List<AccountDto>? {
        return kotlin.runCatching {
            transaction(db) {
                user.accounts
                    .toList()
                    .map {
                        AccountDto(
                            currency = it.currency,
                            balance = it.balance
                        )
                    }
            }
        }.getOrNull()
    }

    fun addUser(userDto: UserDto): Long? {
        return kotlin.runCatching {
            transaction(db) {
                User.new {
                    firstName = userDto.firstName
                    lastName = userDto.lastName
                    email = userDto.email
                }.id.value
            }
        }.getOrNull()
    }

    fun getUser(id: Long): User? {
        return kotlin.runCatching {
            transaction(db) {
                User.findById(id)
            }
        }.getOrNull()
    }

    fun updateUser(user: User, userDto: UserDto): User? {
        return kotlin.runCatching {
            transaction(db) {
                user.firstName = userDto.firstName
                user.lastName = userDto.lastName
                user.email = userDto.email
                user
            }
        }.getOrNull()
    }

    fun deleteUser(user: User): Long? {
        return kotlin.runCatching {
            transaction(db) {
                user.delete()
                user.id.value
            }
        }.getOrNull()
    }

    fun getTransferByRequestId(requestId: String): TransferResponse? {
        return kotlin.runCatching {
            transaction(db) {
                Transfer.find { Transfers.requestId eq requestId }
                    .map {
                        TransferResponse(
                            requestId = it.requestId,
                            receipt = it.receipt
                        )
                    }.first()
            }
        }.getOrNull()
    }

    fun addTransfer(
        transferDto: TransferDto
    ): Transfer? {
        return kotlin.runCatching {
            transaction(db) {
                Transfer.new {
                    fromAccount = transferDto.fromAccount
                    toAccount = transferDto.toAccount
                    amount = transferDto.amount
                    receipt = transferDto.receipt
                    requestId = transferDto.requestId
                }
            }
        }.getOrNull()
    }

    fun updateAccountsAndAddTransfers(
        newAmountAccountFrom: BigDecimal,
        newAmountAccountTo: BigDecimal,
        transferDto: TransferDto
    ): Transfer? {
        return transaction(db)
        {
            updateAccount(
                transferDto.fromAccount,
                newAmountAccountFrom
            )
            updateAccount(
                transferDto.toAccount,
                newAmountAccountTo
            )
            addTransfer(transferDto)
        }
    }


    fun init() {
        transaction(db) {
            SchemaUtils.create(Users, Accounts, Transfers)
            addUser(UserDto("firstName1", "lastName1", "email1"))
            addUser(UserDto("firstName1", "lastName1", "email2"))
            addAccount(getUser(1)!!, AccountDto("eur", 300.0.toBigDecimal()))
            addAccount(getUser(2)!!, AccountDto("eur", 100.0.toBigDecimal()))
        }
    }
}