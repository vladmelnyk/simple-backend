package com.company.router

import com.company.dto.*
import com.company.main
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class ApiRouterIT {
    private val mapper = jacksonObjectMapper()

    @Nested
    inner class Accounts {
        private val accountDto = createAccount()
        private val userId = 1L
        private val requestId = UUID.randomUUID().toString()

        @Test
        fun `account creation succeeds`() {
            withTestEngine {
                handleRequest(Post, "/users/1/accounts") {
                    setJsonBody(accountDto)
                }.apply {
                    assertThat(response.status()).isEqualTo(OK)
                    val responseId = response.readJsonModel<Long>()
                    assertThat(responseId).isNotNull()
                }
            }
        }

        @Test
        fun `get accounts for user successfully`() {
            withTestEngine {
                val userId = 1L
                val accountDto = createAccount()
                createRemoteAccount(userId, accountDto)

                handleRequest(Get, "/users/$userId/accounts").apply {
                    assertThat(response.status()).isEqualTo(OK)
                    val responseAccounts = response.readJsonList<AccountDto>()
                    assertThat(responseAccounts.size)
                }
            }
        }

        @Test
        fun `get single account for user successfully`() {
            withTestEngine {
                val userId = 1L
                val accountDto = createAccount(balance = BigDecimal.ZERO.setScale(4))
                val accountId = createRemoteAccount(userId, accountDto)

                handleRequest(Get, "/accounts/$accountId").apply {
                    assertThat(response.status()).isEqualTo(OK)
                    val responseAccount = response.readJsonModel<AccountDto>()
                    assertThat(responseAccount).isEqualTo(accountDto)
                }
            }
        }


        @Test
        fun `test delete account success`() {
            withTestEngine {
                val id = createRemoteAccount(1, accountDto)

                handleRequest(Delete, "/accounts/$id").apply {
                    assertThat(response.status()).isEqualTo(OK)
                    val response = response.readJsonModel<Long>()
                    assertThat(response).isEqualTo(id)
                }
            }
        }


        @Test
        fun `should fail on deletion of non-existent account`() {
            withTestEngine {
                handleRequest(Delete, "/accounts/34").apply {
                    assertThat(response.status()).isEqualTo(NotFound)
                }
            }
        }


        @Test
        fun `test successful deposit to account`() {
            withTestEngine {
                val id = createRemoteAccount(userId, accountDto)
                val amount = BigDecimal.TEN
                val depositRequest = DepositRequest(amount, requestId)
                handleRequest(Patch, "/accounts/$id") {
                    setJsonBody(depositRequest)
                }.apply {
                    assertThat(response.status()).isEqualTo(OK)
                    val response = response.readJsonModel<AccountDto>()
                    assertThat(response).isEqualTo(createAccount(amount.setScale(4)))
                }
            }
        }

        @Test
        fun `test error on invalid amount`() {
            withTestEngine {
                val id = createRemoteAccount(userId, accountDto)
                val amount = -BigDecimal.TEN
                val depositRequest = DepositRequest(amount, requestId)
                handleRequest(Patch, "/accounts/$id") {
                    setJsonBody(depositRequest)
                }.apply {
                    assertThat(response.status()).isEqualTo(BadRequest)
                }
            }
        }

        @Test
        fun `deposit to non-existent account`() {
            withTestEngine {
                val amount = BigDecimal.TEN
                val depositRequest = DepositRequest(amount, requestId)
                handleRequest(Patch, "/accounts/54") {
                    setJsonBody(depositRequest)
                }.apply {
                    assertThat(response.status()).isEqualTo(NotFound)
                }
            }
        }

        @Nested
        inner class Users {
            private val userId = 1L
            private val userDto = UserDto("firstName1", "lastName1", "email1")

            @Test
            fun `should get user successfully`() {
                withTestEngine {
                    handleRequest(Get, "/users/$userId") {
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<UserDto>()
                        assertThat(response).isEqualTo(userDto)
                    }
                }
            }

            @Test
            fun `get user not found`() {
                withTestEngine {
                    handleRequest(Get, "/users/400") {
                    }.apply {
                        assertThat(response.status()).isEqualTo(NotFound)
                    }
                }
            }

            @Test
            fun `add user successfully`() {
                withTestEngine {
                    val userDto = UserDto("test", "test", "test")
                    handleRequest(Post, "/users") {
                        setJsonBody(userDto)
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<Long>()
                        assertThat(response).isNotNull()
                    }
                }
            }

            @Test
            fun `update user successfully`() {
                withTestEngine {
                    val userDto = UserDto("test", "test", "test")
                    handleRequest(Put, "/users/3") {
                        setJsonBody(userDto)
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<UserDto>()
                        assertThat(response).isEqualTo(response)
                    }
                }
            }

            @Test
            fun `delete user successfully`() {
                val userId3 = 3L
                withTestEngine {
                    handleRequest(Delete, "/users/$userId3") {
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<Long>()
                        assertThat(response).isEqualTo(userId3)
                    }
                }
            }
        }

        @Nested
        inner class Transfer {
            private val accountDto = createAccount()
            private val userId = 1L
            private val requestId = UUID.randomUUID().toString()

            @Test
            fun `test successful transfer from account`() {
                withTestEngine {
                    val fromAccountId = createRemoteAccount(userId, accountDto)
                    val toAccountId = createRemoteAccount(userId, accountDto)
                    val amount = BigDecimal.TEN
                    val depositRequest = DepositRequest(amount, requestId)
                    val transferRequest = TransferRequest(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amount = amount,
                        requestId = requestId
                    )
                    handleRequest(Patch, "/accounts/$fromAccountId") {
                        setJsonBody(depositRequest)
                    }
                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<TransferResponse>()
                        assertThat(response.requestId).isEqualTo(requestId)
                        assertThat(response.receipt).isNotBlank()
                    }
                }
            }

            @Test
            fun `retry transfer with the same idempotancy key`() {
                withTestEngine {
                    val fromAccountId = createRemoteAccount(userId, accountDto)
                    val toAccountId = createRemoteAccount(userId, accountDto)
                    val amount = BigDecimal.TEN
                    val depositRequest = DepositRequest(amount, requestId)
                    val transferRequest = TransferRequest(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amount = amount,
                        requestId = requestId
                    )
                    handleRequest(Patch, "/accounts/$fromAccountId") {
                        setJsonBody(depositRequest)
                    }
                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<TransferResponse>()
                        assertThat(response.requestId).isEqualTo(requestId)
                        assertThat(response.receipt).isNotBlank()
                    }
                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(OK)
                        val response = response.readJsonModel<TransferResponse>()
                        assertThat(response.requestId).isEqualTo(requestId)
                        assertThat(response.receipt).isNotBlank()
                    }
                }
            }


            @Test
            fun `transfer from and to account the same fails`() {
                withTestEngine {
                    val fromAccountId = createRemoteAccount(userId, accountDto)
                    val toAccountId = fromAccountId
                    val amount = BigDecimal.TEN
                    val transferRequest = TransferRequest(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amount = amount,
                        requestId = requestId
                    )
                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(BadRequest)
                    }
                }
            }

            @Test
            fun `transfer from non-existing account fails`() {
                withTestEngine {
                    val fromAccountId = 223L
                    val toAccountId = createRemoteAccount(userId, accountDto)
                    val amount = BigDecimal.TEN
                    val transferRequest = TransferRequest(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amount = amount,
                        requestId = requestId
                    )

                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(NotFound)
                    }
                }
            }

            @Test
            fun `should fail transfer on insufficient funds`() {
                withTestEngine {
                    val fromAccountId = createRemoteAccount(userId, accountDto)
                    val toAccountId = createRemoteAccount(userId, accountDto)
                    val amount = 100000.toBigDecimal()
                    val requestId = UUID.randomUUID().toString()
                    val transferRequest = TransferRequest(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amount = amount,
                        requestId = requestId
                    )

                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(BadRequest)
                    }
                }
            }

            @Test
            fun `should have 400 on invalid amount`() {
                withTestEngine {
                    val fromAccountId = createRemoteAccount(userId, accountDto)
                    val toAccountId = createRemoteAccount(userId, accountDto)
                    val amount = -BigDecimal.TEN
                    val transferRequest = TransferRequest(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amount = amount,
                        requestId = requestId
                    )

                    handleRequest(Post, "/transfers") {
                        setJsonBody(transferRequest)
                    }.apply {
                        assertThat(response.status()).isEqualTo(BadRequest)
                    }
                }
            }
        }

        private fun createAccount(
            balance: BigDecimal = BigDecimal.ZERO,
            currency: String = "usd"
        ) = AccountDto(
            currency = currency,
            balance = balance
        )

        private fun TestApplicationRequest.setJsonBody(value: Any?) {
            addHeader(ContentType, Json.toString())
            setBody(mapper.writeValueAsString(value))
        }

        private inline fun <reified T> TestApplicationResponse.readJsonModel() =
            mapper.readValue(content, T::class.java)

        private inline fun <reified T> TestApplicationResponse.readJsonList(): List<T> = mapper.readValue(
            content,
            mapper.typeFactory.constructCollectionType(List::class.java, T::class.java)
        )

        private fun getIdFromJson(json: String): Long {
            return json.toLong()
        }

        fun <T> withTestEngine(test: TestApplicationEngine.() -> T): T {
            return withApplication(createTestEnvironment()) {
                application.main()
                test()
            }
        }

        fun TestApplicationEngine.createRemoteAccount(userId: Long, accountDto: AccountDto): Long {
            return handleRequest(Post, "/users/$userId/accounts") {
                addHeader(ContentType, Json.toString())
                setJsonBody(accountDto)
            }.run { getIdFromJson(response.content!!) }
        }

    }
}