package com.company.router

import com.company.dto.AccountDto
import com.company.dto.DepositRequest
import com.company.dto.TransferRequest
import com.company.dto.UserDto
import com.company.service.BusinessLogicService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

class ApiRouter(
    private val businessLogicService: BusinessLogicService
) {
    fun Route.routes() {

        route("/users") {
            post {
                val userDto = call.receiveModel<UserDto>()
                val result = kotlin.runCatching { businessLogicService.addUser(userDto) }
                    .getOrElse { handleInternalException(it) }
                call.respond(result)
            }

            get("/{id}") {
                val id = call.getIdFromPath("id")
                val user = kotlin.runCatching { businessLogicService.getUser(id) }
                    .getOrElse { handleInternalException(it) }
                call.respond(user)
            }

            put("/{id}") {
                val id = call.getIdFromPath("id")
                val userDto = call.receiveModel<UserDto>()
                val user = kotlin.runCatching { businessLogicService.updateUser(id, userDto) }
                    .getOrElse { handleInternalException(it) }
                call.respond(user)
            }

            delete("/{id}") {
                val id = call.getIdFromPath("id")
                val user = kotlin.runCatching { businessLogicService.deleteUser(id) }
                    .getOrElse { handleInternalException(it) }
                call.respond(user)
            }

            get("/{id}/accounts") {
                val id = call.getIdFromPath("id")
                val accounts = kotlin.runCatching { businessLogicService.getAccountsForUser(id) }
                    .getOrElse { handleInternalException(it) }
                call.respond(accounts)
            }

            post("/{id}/accounts") {
                val accountDto = call.receiveModel<AccountDto>()
                val id = call.getIdFromPath("id")
                val result = kotlin.runCatching { businessLogicService.createAccountForUser(id, accountDto) }
                    .getOrElse { handleInternalException(it) }
                call.respond(result)
            }
        }

        route("/accounts") {
            get("/{id}") {
                val id = call.getIdFromPath("id")
                val account = kotlin.runCatching { businessLogicService.getAccount(id) }
                    .getOrElse { handleInternalException(it) }
                call.respond(account)
            }

            patch("/{id}") {
                val depositRequest = call.receiveModel<DepositRequest>()
                val id = call.getIdFromPath("id")
                val result = kotlin.runCatching {
                    businessLogicService.depositAccount(id, depositRequest.amount)
                }.getOrElse { handleInternalException(it) }
                call.respond(result)
            }

            delete("/{id}") {
                val id = call.getIdFromPath("id")
                val result = kotlin.runCatching { businessLogicService.deleteAccount(id) }
                    .getOrElse { handleInternalException(it) }
                call.respond(result)
            }
        }

        route("/transfers") {
            post {
                val transferRequest = call.receiveModel<TransferRequest>()
                val result = kotlin.runCatching { businessLogicService.transfer(transferRequest) }
                    .getOrElse { handleInternalException(it) }
                call.respond(result)
            }
        }
    }

    private fun handleInternalException(e: Throwable) {
        when (e) {
            is InternalNotFoundException ->
                throw HttpException(NotFound, e.message ?: "")
            is InternalBadRequestException ->
                throw HttpException(BadRequest, e.message ?: "")
            else -> {
                throw HttpException(InternalServerError, e.message ?: "")
            }
        }
    }

    private fun ApplicationCall.getIdFromPath(varname: String): Long {
        val id = parameters[varname]
        return kotlin.runCatching { id!!.toLong() }.getOrElse { throw HttpException(BadRequest, "invalid id: $id") }
    }

    private suspend inline fun <reified T : Any> ApplicationCall.receiveModel(): T {
        kotlin.runCatching {
            return this.receive()
        }.getOrElse {
            val name = T::class.java
            throw HttpException(BadRequest, "Bad request for class [$name]")
        }
    }


}