package de.borstelmann.doorbell.server.controller

import com.google.actions.api.smarthome.*
import de.borstelmann.doorbell.server.response.google.home.GoogleHomeDeviceService
import de.borstelmann.doorbell.server.services.AuthenticationService
import de.borstelmann.doorbell.server.services.UserService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GoogleSmartHomeApp(
        private val googleHomeDeviceService: GoogleHomeDeviceService,
        private val authenticationService: AuthenticationService,
        private val userService: UserService
) : SmartHomeApp() {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?) {
        val user = authenticationService.currentUserOrThrow
        userService.disableGoogleHomeForUser(user.id)
    }

    override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
        val executeResponse = ExecuteResponse()
        executeResponse.requestId = request.requestId
        executeResponse.payload = ExecuteResponse.Payload()

        val inputs = getExecuteRequestInputs(request)
        val commandsResponse = googleHomeDeviceService.execute(inputs.payload.commands).toTypedArray()

        executeResponse.payload.commands = commandsResponse
        return executeResponse
    }

    override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
        val queryResponse = QueryResponse()
        queryResponse.requestId = request.requestId

        val inputs = getQueryRequestInputs(request)
        val devices = inputs.payload.devices
        queryResponse.payload = QueryResponse.Payload()
        queryResponse.payload.devices = getStatePerDevice(devices)
        return queryResponse
    }

    private fun getStatePerDevice(devices: Array<QueryRequest.Inputs.Payload.Device>): Map<String, Map<String, Any>> {
        val deviceIds = devices.map { it.id.toLong() }
                .distinct()
                .toList()

        return googleHomeDeviceService.getDevicesForUser(deviceIds)
                .associateBy(keySelector = { it.id }, valueTransform = { it.queryState })
    }

    override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
        val syncResponse = SyncResponse()
        syncResponse.requestId = request.requestId
        syncResponse.payload = SyncResponse.Payload()

        val user = authenticationService.getCurrentUserOrThrow()

        userService.enableGoogleHomeForUser(user.id)

        syncResponse.payload.agentUserId = user.id.toString()
        syncResponse.payload.devices = googleHomeDeviceService.getAllDevicesForUser()
                .map { it.sync }
                .toTypedArray()

        return syncResponse
    }


    @Suppress("KotlinConstantConditions")
    private fun getExecuteRequestInputs(request: ExecuteRequest) =
            request.inputs[0] as ExecuteRequest.Inputs

    @Suppress("KotlinConstantConditions")
    private fun getQueryRequestInputs(request: QueryRequest) =
            request.inputs[0] as QueryRequest.Inputs
}
