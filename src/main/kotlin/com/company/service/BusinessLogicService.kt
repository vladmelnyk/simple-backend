package com.company.service

import com.company.dto.*
import com.company.repository.Account
import com.company.repository.User
import com.company.router.InternalBadRequestException
import com.company.router.InternalNotFoundException
import com.company.router.InternalServerException
import java.math.BigDecimal
import java.util.*

class BusinessLogicService(
    private val repositoryService: RepositoryService
) {

    fun addUser(userDto: UserDto): Long {
        return repositoryService.addUser(userDto) ?: throw InternalServerException("Cannot add user")
    }

    private fun getUserEntity(id: Long): User {
        return repositoryService.getUser(id) ?: throw InternalNotFoundException("Cannot find user $id")
    }

    fun getUser(id: Long): UserDto {
        val user = getUserEntity(id)
        return UserDto(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email
        )
    }

    fun updateUser(id: Long, userDto: UserDto): UserDto {
        val user = getUserEntity(id)
        val updatedUser =
            repositoryService.updateUser(user, userDto) ?: throw InternalServerException("Cannot update user $id")
        return UserDto(
            firstName = updatedUser.firstName,
            lastName = updatedUser.lastName,
            email = updatedUser.email
        )
    }

    fun deleteUser(id: Long): Long {
        val user = getUserEntity(id)
        return repositoryService.deleteUser(user) ?: throw InternalServerException("Cannot delete user $id")
    }

    fun getAccountsForUser(id: Long): List<AccountDto> {
        val user = getUserEntity(id)
        return repositoryService.getAccountsForUser(user)
            ?: throw InternalServerException("Cannot get accounts for user $id")
    }

    fun createAccountForUser(userId: Long, accountDto: AccountDto): Long {
        val user = getUserEntity(userId)
        return repositoryService.addAccount(user, accountDto)
            ?: throw InternalServerException("Cannot add account for user $userId")
    }

    private fun checkAmount(amount: BigDecimal) {
        if (amount < BigDecimal.ZERO) {
            throw InternalBadRequestException("Amount should be positive")
        }
    }

    fun depositAccount(accountId: Long, amount: BigDecimal): AccountDto {
        checkAmount(amount)
        val account = getAccountEntity(accountId)
        val currentBalance = account.balance
        val accountUpdated = repositoryService.updateAccount(account, currentBalance + amount)
            ?: throw InternalServerException("Cannot update account $account")
        return AccountDto(accountUpdated.currency, accountUpdated.balance)
    }

    fun getAccount(accountId: Long): AccountDto {
        val account = repositoryService.getAccount(accountId)
            ?: throw InternalNotFoundException("Cannot find account $accountId")
        return AccountDto(currency = account.currency, balance = account.balance)
    }

    private fun getAccountEntity(accountId: Long): Account {
        return repositoryService.getAccount(accountId)
            ?: throw InternalNotFoundException("Cannot find account $accountId")
    }

    fun deleteAccount(accountId: Long): Long {
        val account = getAccountEntity(accountId)
        return repositoryService.deleteAccount(account)
            ?: throw InternalServerException("Cannot delete account $account")
    }

    fun transfer(transferRequest: TransferRequest): TransferResponse {
        if (transferRequest.fromAccountId == transferRequest.toAccountId) {
            throw InternalBadRequestException("from and to account shouldn't be equal")
        }
        checkAmount(transferRequest.amount)

        val transferByRequestId = repositoryService.getTransferByRequestId(transferRequest.requestId)
        transferByRequestId?.let {
            println("Idempotency key $transferRequest.requestId already exists, returning original transfer")
            return transferByRequestId
        }

        val accountFrom = getAccountEntity(transferRequest.fromAccountId)
        val accountTo = getAccountEntity(transferRequest.toAccountId)
        if (accountFrom.balance < transferRequest.amount) {
            throw InternalBadRequestException("Insufficient funds")
        }
        if (accountFrom.currency != accountTo.currency) {
            throw InternalBadRequestException("Accounts have different currencies")
        }

        val transferDto = TransferDto(
            fromAccount = accountFrom,
            toAccount = accountTo,
            amount = transferRequest.amount,
            requestId = transferRequest.requestId,
            receipt = UUID.randomUUID().toString()
        )

        val transfer = repositoryService.updateAccountsAndAddTransfers(
            newAmountAccountFrom = accountFrom.balance - transferRequest.amount,
            newAmountAccountTo = accountTo.balance + transferRequest.amount,
            transferDto = transferDto
        ) ?: throw InternalServerException("Cannot add transfer $transferDto")

        return TransferResponse(transfer.requestId, transfer.receipt)

    }
}