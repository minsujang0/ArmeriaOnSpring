package io.github.minsujang0.garmeria.util.armeria.web

import com.linecorp.armeria.server.ServiceRequestContext
import io.grpc.BindableService
import java.util.*

const val USER_ID_HEADER = "X-User-Id"

val BindableService.userIdFromHeader: UUID
    get() = ServiceRequestContext.current().request().headers().get(USER_ID_HEADER)?.let(UUID::fromString)
        ?: throw IllegalArgumentException("User-Id header not found") 