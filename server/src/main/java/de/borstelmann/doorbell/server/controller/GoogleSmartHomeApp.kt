package de.borstelmann.doorbell.server.controller

import com.google.actions.api.smarthome.*
import com.google.actions.api.smarthome.QueryRequest
import com.google.actions.api.smarthome.QueryResponse
import com.google.actions.api.smarthome.SyncRequest
import com.google.actions.api.smarthome.SyncResponse
import com.google.auth.oauth2.GoogleCredentials
import de.borstelmann.doorbell.server.domain.model.User
import de.borstelmann.doorbell.server.domain.model.security.CustomUserSession
import de.borstelmann.doorbell.server.response.google.home.GoogleHomeDeviceService
import de.borstelmann.doorbell.server.response.google.home.GoogleHomeExecutionService
import de.borstelmann.doorbell.server.services.DoorbellService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GoogleSmartHomeApp(
        private val googleHomeExecutionService: GoogleHomeExecutionService,
        private val googleHomeDeviceService: GoogleHomeDeviceService,
        private val doorbellService: DoorbellService
) : SmartHomeApp() {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?) {
        TODO("Not yet implemented")
    }

    override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
        val executeResponse = ExecuteResponse()
        executeResponse.requestId = request.requestId
        executeResponse.payload = ExecuteResponse.Payload()

        val inputs = getExecuteRequestInputs(request)
        val commandsResponse = googleHomeExecutionService.execute(inputs.payload.commands).toTypedArray()

        executeResponse.payload.commands = commandsResponse
        return executeResponse
    }

    override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
        val queryResponse = QueryResponse()
        queryResponse.requestId = request.requestId

        val user = CustomUserSession.getCurrentUserOrThrow()

        val inputs = getQueryRequestInputs(request)
        val devices = inputs.payload.devices
        queryResponse.payload = QueryResponse.Payload()
        queryResponse.payload.devices = getStatePerDevice(user, devices)
        return queryResponse
    }

    private fun getStatePerDevice(user: User, devices: Array<QueryRequest.Inputs.Payload.Device>): Map<String, Map<String, Any>> {
        val deviceIds = devices.map { it.id.toLong() }
                .distinct()
                .toList()

        return googleHomeDeviceService.getDevicesForUser(user, deviceIds)
                .associateBy(keySelector = { it.id }, valueTransform = { it.queryState })
    }

    override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
        val syncResponse = SyncResponse()
        syncResponse.requestId = request.requestId
        syncResponse.payload = SyncResponse.Payload()

        val user = CustomUserSession.getCurrentUserOrThrow()

        syncResponse.payload.agentUserId = user.id.toString()
        syncResponse.payload.devices = googleHomeDeviceService.getAllDevicesForUser(user)
                .map { it.sync }
                .toTypedArray()

        return syncResponse
    }

    fun reportStateDoorbellState(deviceId: Long) {
        val reportStateRequest = googleHomeDeviceService.makeReportDeviceStateRequest(deviceId)
        val reportStateAndNotificationResponse = reportState(reportStateRequest)
        log.info("Got response {}", reportStateAndNotificationResponse)
    }


    @Suppress("KotlinConstantConditions")
    private fun getExecuteRequestInputs(request: ExecuteRequest) =
            request.inputs[0] as ExecuteRequest.Inputs

    @Suppress("KotlinConstantConditions")
    private fun getQueryRequestInputs(request: QueryRequest) =
            request.inputs[0] as QueryRequest.Inputs

    @Autowired(required = false)
    fun setGoogleCredentials(googleCredentials: GoogleCredentials) {
        credentials = googleCredentials
    }
}
