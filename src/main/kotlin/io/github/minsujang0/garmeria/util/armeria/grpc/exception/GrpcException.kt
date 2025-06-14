package io.github.minsujang0.garmeria.util.armeria.grpc.exception

import com.linecorp.armeria.server.ServiceRequestContext

sealed class GrpcException(throwable: Throwable?, val error: Enum<*>?) : RuntimeException(
    "${error?.toString() ?: ""}@$serverRpcMessage", throwable
)

class GrpcErrorException(error: Enum<*>) : GrpcException(null, error)

class GrpcServerException(throwable: Throwable, error: Enum<*>?) : GrpcException(throwable, error)

class GrpcClientCallException(throwable: Throwable, error: Enum<*>?) : GrpcException(throwable, error)

class ArmeriaGrpcClientCallException(throwable: Throwable, error: Enum<*>?) : GrpcException(throwable, error)

class InternalException(val error: Enum<*>?, cause: Throwable? = null) : RuntimeException(cause)

val serverRpcMessage
    get() = runCatching { // might catch IllegalStateException due to missing context
        ServiceRequestContext.current().rpcRequest()
    }.getOrNull()
        ?.run { "${serviceName()}/${method()} ${params()}" }
        ?: "unknown RPC" 