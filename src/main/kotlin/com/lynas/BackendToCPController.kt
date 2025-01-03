package com.lynas

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.WebSocketHandler

@RestController
class BackendToCPController(
    @Qualifier("ocppWebSocketConnectionHandler") private val ocppWebSocketConnectionHandler: WebSocketHandler
) {
    @GetMapping("/getConfig/cp-name/{cpName}/configName/{configName}")
    fun getConfig(@PathVariable cpName: String, @PathVariable configName: String): String {
        if (ocppWebSocketConnectionHandler is OcppWebSocketConnectionHandler) {
            ocppWebSocketConnectionHandler.sendOcppGetConfigurationRequestToChargePoint(cpName, configName)
        }
        return "See log for Ocpp GET_CONFIG of CP $cpName"
    }
}