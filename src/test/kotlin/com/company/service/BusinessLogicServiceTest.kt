package com.company.service

import com.company.dto.AccountDto
import com.company.dto.TransferRequest
import com.company.dto.TransferResponse
import com.company.dto.UserDto
import com.company.repository.Account
import com.company.repository.Transfer
import com.company.repository.User
import com.company.router.InternalBadRequestException
import com.company.router.InternalNotFoundException
import com.company.router.InternalServerException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockKExtension::class)
internal class BusinessLogicServiceTest {

    @RelaxedMockK
    private lateinit var repositoryService: RepositoryService

    private lateinit var businessLogicService: BusinessLogicService

    @BeforeAll
    fun setup() {
        businessLogicService = BusinessLogicService(repositoryService)
    }

    @BeforeEach
    fun reset() {
        clearAllMocks()
    }

    @Test
    fun `should add user successfully`() {
        val userDto = UserDto("dummy", "dummy", "dummy")
        val userId = 1L
        every { repositoryService.addUser(userDto) } returns userId

        val result = businessLogicService.addUser(userDto)

        assertThat(result).isEqualTo(userId)
    }

    @Test
    fun `internal server exception add user`() {
        val userDto = UserDto("dummy", "dummy", "dummy")
        every { repositoryService.addUser(userDto) } returns null

        assertThrows<InternalServerException> { businessLogicService.addUser(userDto) }
    }

    @Test
    fun `should get user successfully`() {
        val user = mockk<User>()
        val userDto = UserDto("dummy", "dummy", "dummy")
        every { user.firstName } returns userDto.firstName
        every { user.lastName } returns userDto.lastName
        every { user.email } returns userDto.email
        val userId = 1L
        every { repositoryService.getUser(userId) } returns user

        val result = businessLogicService.getUser(userId)

        assertThat(result).isEqualTo(userDto)
    }

    @Test
    fun `internal not found exception get user`() {
        val userId = 1L
        every { repositoryService.getUser(userId) } returns null

        assertThrows<InternalNotFoundException> { businessLogicService.getUser(userId) }
    }


    @Test
    fun `should update user successfully`() {
        val user = mockk<User>()
        val user2 = mockk<User>()
        val userDto = UserDto("dummy", "dummy", "dummy")
        val userDtoUpdated = UserDto("dummy3", "dummy3", "dummy3")
        every { user.firstName } returns userDto.firstName
        every { user.lastName } returns userDto.lastName
        every { user.email } returns userDtoUpdated.email
        every { user2.firstName } returns userDtoUpdated.firstName
        every { user2.lastName } returns userDtoUpdated.lastName
        every { user2.email } returns userDtoUpdated.email
        val userId = 1L
        every { repositoryService.getUser(userId) } returns user
        every { repositoryService.updateUser(user, userDtoUpdated) } returns user2

        val result = businessLogicService.updateUser(userId, userDtoUpdated)

        assertThat(result).isEqualTo(userDtoUpdated)
    }

    @Test
    fun `internal not found exception update user`() {
        val userId = 1L
        val userDto = UserDto("dummy", "dummy", "dummy")
        every { repositoryService.getUser(userId) } returns null

        assertThrows<InternalNotFoundException> { businessLogicService.updateUser(userId, userDto) }
    }

    @Test
    fun `internal server exception update user`() {
        val user = mockk<User>()
        val userDto = UserDto("dummy", "dummy", "dummy")
        val userDtoUpdated = UserDto("dummy3", "dummy3", "dummy3")
        every { user.firstName } returns userDto.firstName
        every { user.lastName } returns userDto.lastName
        every { user.email } returns userDtoUpdated.email
        val userId = 1L
        every { repositoryService.getUser(userId) } returns user
        every { repositoryService.updateUser(user, userDtoUpdated) } returns null

        assertThrows<InternalServerException> {
            businessLogicService.updateUser(userId, userDtoUpdated)
        }
    }

    @Test
    fun `should delete user successfully`() {
        val user = mockk<User>()
        val userId = 1L
        every { repositoryService.getUser(userId) } returns user
        every { repositoryService.deleteUser(user) } returns userId

        val result = businessLogicService.deleteUser(userId)

        assertThat(result).isEqualTo(userId)
    }

    @Test
    fun `internal not found exception delete user`() {
        val userId = 1L
        every { repositoryService.getUser(userId) } returns null

        assertThrows<InternalNotFoundException> { businessLogicService.deleteUser(userId) }
    }


    @Test
    fun `internal server exception delete user`() {
        val user = mockk<User>()
        val userId = 1L
        every { repositoryService.getUser(userId) } returns user
        every { repositoryService.deleteUser(user) } returns null

        assertThrows<InternalServerException> { businessLogicService.deleteUser(userId) }
    }

    @Test
    fun `should get accounts for user successfully`() {
        val user = mockk<User>()
        val accountDto = AccountDto()
        val accountDtoList = listOf(accountDto)
        val userId = 1L
        every { repositoryService.getUser(userId) } returns user
        every { repositoryService.getAccountsForUser(user) } returns accountDtoList

        val result = businessLogicService.getAccountsForUser(userId)

        assertThat(result).isEqualTo(accountDtoList)
    }

    @Test
    fun `should create account for user successfully`() {
        val user = mockk<User>()
        val accountDto = AccountDto()
        val userId = 1L
        val accountId = 1L
        every { repositoryService.getUser(userId) } returns user
        every { repositoryService.addAccount(user, accountDto) } returns accountId

        val result = businessLogicService.createAccountForUser(userId, accountDto)

        assertThat(result).isEqualTo(accountId)
    }

    @Test
    fun `should deposit account successfully`() {
        val account = mockk<Account>()
        every { account.balance } returns BigDecimal.ZERO
        val accountUpdated = mockk<Account>()
        val amount = 10.toBigDecimal()
        val currency = "usd"
        val accountDto = AccountDto(currency, amount)
        every { accountUpdated.balance } returns amount
        every { accountUpdated.currency } returns currency
        val accountId = 1L
        every { repositoryService.getAccount(accountId) } returns account
        every { repositoryService.updateAccount(account, amount) } returns accountUpdated

        val result = businessLogicService.depositAccount(accountId, amount)

        assertThat(result).isEqualTo(accountDto)
    }

    @Test
    fun `should get account successfully`() {
        val account = mockk<Account>()
        val currency = "usd"
        val balance = BigDecimal.ZERO
        every { account.balance } returns balance
        every { account.currency } returns currency
        val accountDto = AccountDto(currency, balance)
        val accountId = 1L
        every { repositoryService.getAccount(accountId) } returns account

        val result = businessLogicService.getAccount(accountId)

        assertThat(result).isEqualTo(accountDto)
    }

    @Test
    fun `should delete account successfully`() {
        val account = mockk<Account>()
        val currency = "usd"
        val balance = BigDecimal.ZERO
        every { account.balance } returns balance
        every { account.currency } returns currency
        val accountId = 1L
        every { repositoryService.getAccount(accountId) } returns account
        every { repositoryService.deleteAccount(account) } returns accountId

        val result = businessLogicService.deleteAccount(accountId)

        assertThat(result).isEqualTo(accountId)
    }

    @Test
    fun `should transfer successfully`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val amount = 10.toBigDecimal()
        val requestId = UUID.randomUUID().toString()
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            requestId = requestId
        )
        val accountFrom = mockk<Account>()
        val accountFromBalance = 50.toBigDecimal()
        val accountTo = mockk<Account>()
        val accountToBalance = 50.toBigDecimal()
        every { accountFrom.balance } returns accountFromBalance
        every { accountTo.balance } returns accountToBalance
        every { accountTo.currency } returns "usd"
        every { accountFrom.currency } returns "usd"

        val transfer = mockk<Transfer>()
        every { transfer.requestId } returns requestId
        every { transfer.receipt } returns receipt
        every { repositoryService.getTransferByRequestId(requestId) } returns null
        every { repositoryService.getAccount(transferRequest.fromAccountId) } returns accountFrom
        every { repositoryService.getAccount(transferRequest.toAccountId) } returns accountTo
        every {
            repositoryService.updateAccountsAndAddTransfers(
                newAmountAccountFrom = accountFromBalance - amount,
                newAmountAccountTo = accountToBalance + amount,
                transferDto = any()
            )
        } returns transfer

        val result = businessLogicService.transfer(transferRequest)

        assertThat(result).isNotNull
        assertThat(result.requestId).isEqualTo(requestId)
        assertThat(result.receipt).isNotBlank()
    }

    @Test
    fun `internal bad request exception on transfer for same accounts`() {
        val fromAccountId = 1L

        assertThrows<InternalBadRequestException> {
            businessLogicService.transfer(
                TransferRequest(
                    fromAccountId,
                    fromAccountId,
                    BigDecimal.TEN,
                    "smth"
                )
            )
        }
    }

    @Test
    fun `returns immediately transfer if requestId exists`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val requestId = UUID.randomUUID().toString()
        val amount = BigDecimal.TEN
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(fromAccountId, toAccountId, amount, requestId)
        val transferResponse = TransferResponse(requestId, receipt)
        every { repositoryService.getTransferByRequestId(requestId) } returns transferResponse

        val result = businessLogicService.transfer(transferRequest)

        assertThat(result.receipt).isEqualTo(receipt)
        assertThat(result.requestId).isEqualTo(requestId)
    }

    @Test
    fun `transfer throws not found exception if account not found`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val requestId = UUID.randomUUID().toString()
        val amount = BigDecimal.TEN
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(fromAccountId, toAccountId, amount, requestId)
        val transferResponse = TransferResponse(requestId, receipt)
        every { repositoryService.getTransferByRequestId(requestId) } returns null
        every { repositoryService.getAccount(fromAccountId) } returns null

        assertThrows<InternalNotFoundException> { businessLogicService.transfer(transferRequest) }
    }

    @Test
    fun `transfer with incorrect amount throws bad request exception`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val requestId = UUID.randomUUID().toString()
        val amount = -BigDecimal.TEN
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(fromAccountId, toAccountId, amount, requestId)
        val transferResponse = TransferResponse(requestId, receipt)
        every { repositoryService.getAccount(fromAccountId) } returns null

        assertThrows<InternalBadRequestException> { businessLogicService.transfer(transferRequest) }
    }

    @Test
    fun `transfer with insufficient funds throws bad request exception`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val requestId = UUID.randomUUID().toString()
        val amount = -BigDecimal.TEN
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(fromAccountId, toAccountId, amount, requestId)
        val transferResponse = TransferResponse(requestId, receipt)
        val accountFrom = mockk<Account>()
        val accountFromBalance = 0.toBigDecimal()
        val accountTo = mockk<Account>()
        val accountToBalance = 50.toBigDecimal()
        every { accountFrom.balance } returns accountFromBalance
        every { accountTo.balance } returns accountToBalance
        every { accountTo.currency } returns "usd"
        every { accountFrom.currency } returns "usd"

        every { repositoryService.getAccount(fromAccountId) } returns accountFrom
        every { repositoryService.getAccount(toAccountId) } returns accountTo

        assertThrows<InternalBadRequestException> { businessLogicService.transfer(transferRequest) }
    }

    @Test
    fun `transfer with different currencies throws bad request exception`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val requestId = UUID.randomUUID().toString()
        val amount = BigDecimal.TEN
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(fromAccountId, toAccountId, amount, requestId)
        val transferResponse = TransferResponse(requestId, receipt)
        val accountFrom = mockk<Account>()
        val accountFromBalance = 50.toBigDecimal()
        val accountTo = mockk<Account>()
        val accountToBalance = 50.toBigDecimal()
        every { accountFrom.balance } returns accountFromBalance
        every { accountTo.balance } returns accountToBalance
        every { accountTo.currency } returns "eur"
        every { accountFrom.currency } returns "usd"

        every { repositoryService.getTransferByRequestId(requestId) } returns null
        every { repositoryService.getAccount(fromAccountId) } returns accountFrom
        every { repositoryService.getAccount(toAccountId) } returns accountTo

        assertThrows<InternalBadRequestException> { businessLogicService.transfer(transferRequest) }
    }

    @Test
    fun `transfer had issues saving to db and throws internal server exception`() {
        val fromAccountId = 1L
        val toAccountId = 2L
        val amount = 10.toBigDecimal()
        val requestId = UUID.randomUUID().toString()
        val receipt = UUID.randomUUID().toString()
        val transferRequest = TransferRequest(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            requestId = requestId
        )
        val accountFrom = mockk<Account>()
        val accountFromBalance = 50.toBigDecimal()
        val accountTo = mockk<Account>()
        val accountToBalance = 50.toBigDecimal()
        every { accountFrom.balance } returns accountFromBalance
        every { accountTo.balance } returns accountToBalance
        every { accountTo.currency } returns "usd"
        every { accountFrom.currency } returns "usd"

        val transfer = mockk<Transfer>()
        every { transfer.requestId } returns requestId
        every { transfer.receipt } returns receipt
        every { repositoryService.getTransferByRequestId(requestId) } returns null
        every { repositoryService.getAccount(transferRequest.fromAccountId) } returns accountFrom
        every { repositoryService.getAccount(transferRequest.toAccountId) } returns accountTo
        every {
            repositoryService.updateAccountsAndAddTransfers(
                newAmountAccountFrom = accountFromBalance - amount,
                newAmountAccountTo = accountToBalance + amount,
                transferDto = any()
            )
        } returns null

        assertThrows<InternalServerException> {
            businessLogicService.transfer(transferRequest)
        }
    }

}