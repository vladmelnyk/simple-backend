package com.company.service

import com.company.dto.AccountDto
import com.company.dto.TransferDto
import com.company.dto.UserDto
import com.company.repository.Account
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class RepositoryServiceIT {

    private val db = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
    val repositoryService = RepositoryService(db)

    private val userDto = UserDto("test", "test", "test")
    private val accountDto = AccountDto()

    @BeforeAll
    fun setup() {
        repositoryService.init()
    }

    @AfterAll
    fun tearDown() {
        repositoryService.tearDown()
    }

    @Nested
    inner class Users {

        @Test
        fun `should create user successfully`() {
            val userId = repositoryService.addUser(userDto)

            assertThat(userId).isNotNull()
        }

        @Test
        fun `should get user successfully`() {
            val user = repositoryService.getUser(1)

            assertThat(user).isNotNull()
        }

        @Test
        fun `should update user successfully`() {
            val user = repositoryService.getUser(2)

            val updatedUser = repositoryService.updateUser(user!!, userDto)

            assertThat(updatedUser).isNotNull()
            assertThat(updatedUser?.firstName).isEqualTo("test")
        }

        @Test
        fun `should delete user successfully`() {
            val userId = repositoryService.addUser(userDto)
            val user = repositoryService.getUser(userId!!)


            val deletedUserId = repositoryService.deleteUser(user!!)

            assertThat(deletedUserId).isNotNull()
            assertThat(deletedUserId).isEqualTo(userId)
        }
    }

    @Nested
    inner class Accounts {

        @Test
        fun `should add account for user successfully`() {
            val userId = repositoryService.addUser(userDto)
            val user = repositoryService.getUser(userId!!)

            val accountId = repositoryService.addAccount(user!!, accountDto)

            assertThat(accountId).isNotNull()

        }

        @Test
        fun `should update account successfully`() {
            val userId = repositoryService.addUser(userDto)
            val user = repositoryService.getUser(userId!!)
            val accountId = repositoryService.addAccount(user!!, accountDto)
            val account = repositoryService.getAccount(accountId!!)
            val amount = 100.toBigDecimal().setScale(4)

            val updatedAccount = repositoryService.updateAccount(account!!, amount)

            assertThat(updatedAccount).isNotNull()
            assertThat(updatedAccount?.balance).isEqualTo(amount)
        }

        @Test
        fun `should delete account successfully`() {
            val userId = repositoryService.addUser(userDto)
            val user = repositoryService.getUser(userId!!)
            val accountId = repositoryService.addAccount(user!!, accountDto)
            val account = repositoryService.getAccount(accountId!!)

            val deletedAccountId = repositoryService.deleteAccount(account!!)

            assertThat(deletedAccountId).isNotNull()
            assertThat(deletedAccountId).isEqualTo(accountId)
        }

        @Test
        fun `should get all accounts for user successfully`() {
            val user = repositoryService.getUser(1)

            val accountList = repositoryService.getAccountsForUser(user!!)

            assertThat(accountList).isNotNull()
            assertThat(accountList?.size).isGreaterThan(0)
        }
    }

    @Nested
    inner class Transfers {

        private fun createAccount(currency: String = "usd", amount: BigDecimal = BigDecimal.ZERO.setScale(4)): Account {
            val userId = repositoryService.addUser(userDto)
            val user = repositoryService.getUser(userId!!)
            val accountId = repositoryService.addAccount(user!!, AccountDto(currency, amount))
            return repositoryService.getAccount(accountId!!)!!
        }

        @Test
        fun `add transfer successfully`() {
            val account1 = createAccount(amount = 100.toBigDecimal().setScale(4))
            val account2 = createAccount(amount = 50.toBigDecimal().setScale(4))
            val amount = 20.toBigDecimal().setScale(4)
            val requestId = UUID.randomUUID().toString()
            val receipt = UUID.randomUUID().toString()
            val transferDto = TransferDto(
                fromAccount = account1,
                toAccount = account2,
                amount = amount,
                requestId = requestId,
                receipt = receipt
            )

            val transfer = repositoryService.addTransfer(transferDto)

            assertThat(transfer).isNotNull
        }

        @Test
        fun `update account and add balances successfully`() {
            val account1 = createAccount(amount = 100.toBigDecimal().setScale(4))
            val account2 = createAccount(amount = 50.toBigDecimal().setScale(4))
            val amount = 20.toBigDecimal().setScale(4)
            val newAccountToAmount = 50.toBigDecimal() + amount
            val newAccountFromAmount = 50.toBigDecimal() - amount
            val requestId = UUID.randomUUID().toString()
            val receipt = UUID.randomUUID().toString()
            val transferDto = TransferDto(
                fromAccount = account1,
                toAccount = account2,
                amount = amount,
                requestId = requestId,
                receipt = receipt
            )

            val transfer =
                repositoryService.updateAccountsAndAddTransfers(newAccountFromAmount, newAccountToAmount, transferDto)

            assertThat(transfer).isNotNull
        }

        @Test
        fun `get transfer by requestId successfully`() {
            val account1 = createAccount(amount = 100.toBigDecimal().setScale(4))
            val account2 = createAccount(amount = 50.toBigDecimal().setScale(4))
            val amount = 20.toBigDecimal().setScale(4)
            val requestId = UUID.randomUUID().toString()
            val receipt = UUID.randomUUID().toString()
            val transferDto = TransferDto(
                fromAccount = account1,
                toAccount = account2,
                amount = amount,
                requestId = requestId,
                receipt = receipt
            )
            repositoryService.addTransfer(transferDto)

            val transferResponse = repositoryService.getTransferByRequestId(requestId)

            assertThat(transferResponse).isNotNull
            assertThat(transferResponse?.requestId).isEqualTo(requestId)
        }
    }

}