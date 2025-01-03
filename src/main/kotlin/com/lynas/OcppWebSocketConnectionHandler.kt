package com.lynas

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession

val sessionMap = ConcurrentHashMap<String, WebSocketSession>()
class OcppWebSocketConnectionHandler: WebSocketHandler {
    private val logger = LoggerFactory.getLogger(OcppWebSocketConnectionHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val chargePointName = getChargePointName(session)
            ?: throw RuntimeException("No charge point name found")
        sessionMap[chargePointName] = session
        logger.info("OCPP connection established for $chargePointName")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {

        val requestMessage = message.payload as String
        val chargePointName = getChargePointName(session)

        logger.info("Charging $chargePointName to $requestMessage")
        handleBootNotification(session, requestMessage)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.info("Exception occurred while handling transport error: ${exception.message} - ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        logger.info("Connection closed ${session.id} -  status: ${closeStatus.code}")
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    private fun handleBootNotification(session: WebSocketSession, requestMessage: String) {
        val chargePointName = getChargePointName(session)
        if (!session.isOpen) {
            logger.error("WebSocket session is not open for $chargePointName")
            return
        }

        val requestMessageAsStringList = ObjectMapper().readValue(requestMessage, List::class.java)
        if (requestMessageAsStringList[2] == "BootNotification") {
            val bootNotificationResponse = listOf(
                3,
                requestMessageAsStringList[1],
                mapOf(
                    "status" to "Accepted",
                    "currentTime" to getCurrentTimeString(),
                    "interval" to "300"
                )
            ).toJsonString()
            session.sendMessage(TextMessage(bootNotificationResponse))
            logger.info("Boot Notification Response Message: $bootNotificationResponse")
        }
    }

    private fun getChargePointName(session: WebSocketSession): String? {
        val uri = session.uri
        return uri?.path?.split("/")?.lastOrNull() // Extracts the last part of the path
    }

    fun sendOcppGetConfigurationRequestToChargePoint(cpName: String, configName: String) {
        val session = sessionMap[cpName]
            ?: throw RuntimeException("No websocket connection handler found for cpName: $cpName")

        val messageId = UUID.randomUUID().toString().replace("-", "")
        val message = listOf(
            2,
            messageId,
            "GetConfiguration",
            mapOf(
                "key" to listOf(configName)
            )
        ).toJsonString()

        session.sendMessage(TextMessage(message))
        logger.info("Sending OCPP GET_CONFIGURATION Request Message: $message")
    }
}

fun getCurrentTimeString(): String {
    val instant = Instant.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneOffset.UTC)
    return formatter.format(instant) + ".000+00:00"
}

fun Any.toJsonString(): String = ObjectMapper().writeValueAsString(this)