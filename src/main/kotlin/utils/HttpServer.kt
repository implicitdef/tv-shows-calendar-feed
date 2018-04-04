package utils

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import utils.Utils.log

object HttpServer {

    fun start() {
        val port = 8080
        val server = embeddedServer(Netty, port = port) {
            routing {
                get("/") {
                    call.respondText("Hello World!")
                }
            }
        }
        log("Running on port $port")
        server.start()
    }
}