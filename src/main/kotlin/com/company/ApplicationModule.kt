package com.company

import com.company.router.ApiRouter
import com.company.router.HttpException
import com.company.service.BusinessLogicService
import com.company.service.RepositoryService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.Database

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) { jackson() }

    val db by lazy {
        Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
    }
    val repositoryService = RepositoryService(db)
    repositoryService.init()
    val businessLogicService = BusinessLogicService(repositoryService)
    val apiRouter = ApiRouter(businessLogicService)

    routing { apiRouter.apply { routes() } }
    install(StatusPages) {
        exception<HttpException> { call.respond(it.code, it.reason) }
    }
}