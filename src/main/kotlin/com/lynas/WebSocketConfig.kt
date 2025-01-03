package com.lynas

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(OcppWebSocketConnectionHandler(), "/{chargePointName}")
            .setAllowedOrigins("*")
            .addInterceptors(customInterceptor())
    }

    @Bean
    fun ocppWebSocketConnectionHandler(): WebSocketHandler {
        return OcppWebSocketConnectionHandler()
    }

    @Bean
    fun customInterceptor(): HandshakeInterceptor {
        return object : HandshakeInterceptor {
            override fun beforeHandshake(
                request: ServerHttpRequest,
                response: ServerHttpResponse,
                wsHandler: WebSocketHandler,
                attributes: MutableMap<String, Any>
            ): Boolean {
                val subprotocols = request.headers["Sec-WebSocket-Protocol"]
                if (subprotocols != null && subprotocols.contains("ocpp1.6")) {
                    response.headers.add("Sec-WebSocket-Protocol", "ocpp1.6")
                }
                return true
            }

            override fun afterHandshake(
                request: ServerHttpRequest,
                response: ServerHttpResponse,
                wsHandler: WebSocketHandler,
                exception: Exception?
            ) {}
        }
    }
}