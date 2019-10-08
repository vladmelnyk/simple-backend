package com.company.router

import io.ktor.http.HttpStatusCode

class HttpException(val code: HttpStatusCode, val reason: String = code.description) :
    RuntimeException(reason)

class InternalServerException(message: String?) : RuntimeException(message)
class InternalNotFoundException(message: String?) : RuntimeException(message)
class InternalBadRequestException(message: String?) : RuntimeException(message)

