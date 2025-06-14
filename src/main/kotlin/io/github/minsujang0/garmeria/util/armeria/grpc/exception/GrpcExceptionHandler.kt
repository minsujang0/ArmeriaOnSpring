package io.github.minsujang0.garmeria.util.armeria.grpc.exception

import com.google.rpc.Status
import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.common.grpc.GoogleGrpcExceptionHandlerFunction
import com.linecorp.armeria.common.grpc.GrpcExceptionHandlerFunction
import com.linecorp.armeria.common.util.Exceptions
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder
import io.grpc.Metadata
import io.grpc.StatusException
import io.grpc.StatusRuntimeException

object CustomGrpcError {
    private val ERROR_KEY = Metadata.Key.of("custom-grpc-error", Metadata.ASCII_STRING_MARSHALLER)

    fun putToTrailers(trailers: Metadata, errorName: String) {
        trailers.put(ERROR_KEY, errorName)
    }

    fun putToTrailers(trailers: Metadata, throwable: Throwable) {
        val errorName = extractErrorName(throwable)
        if (errorName != null) {
            putToTrailers(trailers, errorName)
        }
    }

    fun extractErrorName(throwable: Throwable): String? {
        return when (throwable) {
            is GrpcException -> throwable.error?.name
            is StatusRuntimeException -> throwable.trailers?.get(ERROR_KEY)
            is StatusException -> throwable.trailers.get(ERROR_KEY)
            else -> null
        }
    }
}

fun StatusRuntimeException.extractErrorName(): String? = CustomGrpcError.extractErrorName(this)
fun StatusException.extractErrorName(): String? = CustomGrpcError.extractErrorName(this)

fun GrpcServiceBuilder.googleRpcStatusExceptionHandler() =
    exceptionHandler(object : GoogleGrpcExceptionHandlerFunction {
        override fun apply(
            ctx: RequestContext,
            status: io.grpc.Status,
            throwable: Throwable,
            metadata: io.grpc.Metadata,
        ): io.grpc.Status? {
            return super.apply(ctx, status, throwable, metadata)?.withCause(Exceptions.peel(throwable))
        }

        override fun applyStatusProto(
            ctx: RequestContext,
            throwable: Throwable,
            metadata: Metadata,
        ): Status? {
            val defaultGrpcStatus = GrpcExceptionHandlerFunction.of()
                .apply(ctx, io.grpc.Status.UNKNOWN, throwable, metadata)!!
            throwable.printStackTrace()

            val builder = Status.newBuilder()
                .setCode(defaultGrpcStatus.code.value())
                .setMessage(throwable.message)

            CustomGrpcError.putToTrailers(metadata, throwable)

            return builder.build()
        }
    }) 