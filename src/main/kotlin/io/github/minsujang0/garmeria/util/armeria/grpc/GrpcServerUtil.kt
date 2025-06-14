package io.github.minsujang0.garmeria.util.armeria.grpc

import com.linecorp.armeria.server.grpc.GrpcService
import io.github.minsujang0.garmeria.util.armeria.grpc.exception.googleRpcStatusExceptionHandler
import io.grpc.BindableService
import io.grpc.protobuf.services.ProtoReflectionService

fun GrpcService(
    vararg bindableServices: BindableService,
    useBlocking: Boolean,
    useHttpJsonTranscoding: Boolean,
    useUnframedRequests: Boolean,
): GrpcService {
    return GrpcService.builder()
        .addServices(*bindableServices)
        .addService(ProtoReflectionService.newInstance()) // grpc reflection
        .googleRpcStatusExceptionHandler()
        .useBlockingTaskExecutor(useBlocking) // to enable blockingTaskExecutor
        .enableHttpJsonTranscoding(useHttpJsonTranscoding) // to enable automatic gRPC-JSON transcoding
        .enableUnframedRequests(useUnframedRequests) // to accept plain text requests, enables armeria doc calling
        .build()
} 