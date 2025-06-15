package io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception

import com.google.protobuf.ProtocolMessageEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

// blocking version
inline fun <T> blockingCatch(
    crossinline errorEmitter: ((Throwable) -> Enum<*>?) = { null },
    crossinline block: () -> T,
): T {
    try {
        return block()
    } catch (it: Throwable) {
        onCatch(errorEmitter, it)

        // unreachable, but compiler doesn't know that
        throw it
    }
}

fun <T> simpleCatch(errorToEmit: Enum<*>, block: () -> T): T {
    return blockingCatch({ errorToEmit }, block)
}

fun <T> throwableMapCatch(
    throwableMap: Map<Class<out Throwable>, Enum<*>>,
    defaultError: Enum<*>,
    block: () -> T,
): T {
    return blockingCatch({ throwable ->
        throwableMap.entries.find { entry -> entry.key.isInstance(throwable) }?.value
            ?: defaultError
    }, block)
}

fun <T> errorMapCatch(
    errorMap: Map<Enum<*>, Enum<*>>,
    defaultError: Enum<*>,
    block: () -> T,
): T {
    return blockingCatch({ throwable ->
        findInErrorMapOrDefault(errorMap, defaultError, throwable)
    }, block)
}

// suspending version
suspend inline fun <T> suspendingCatch(
    crossinline errorEmitter: ((Throwable) -> Enum<*>?) = { null },
    crossinline block: suspend CoroutineScope.() -> T,
): T = coroutineScope {
    try {
        block()
    } catch (it: Throwable) {
        onCatch(errorEmitter, it)

        // unreachable, but compiler doesn't know that
        throw it
    }
}

suspend fun <T> simpleCatchSus(errorToEmit: Enum<*>, block: suspend CoroutineScope.() -> T): T {
    return suspendingCatch({ errorToEmit }, block)
}

suspend fun <T> throwableMapCatchSus(
    throwableMap: Map<Class<out Throwable>, Enum<*>>,
    defaultError: Enum<*>,
    block: suspend CoroutineScope.() -> T,
): T {
    return suspendingCatch({ throwable ->
        throwableMap.entries.find { entry -> entry.key.isInstance(throwable) }?.value
            ?: defaultError
    }, block)
}

suspend fun <T> errorMapCatchSus(
    errorMap: Map<Enum<*>, Enum<*>>,
    defaultError: Enum<*>,
    block: suspend CoroutineScope.() -> T,
): T {
    return suspendingCatch({ throwable ->
        findInErrorMapOrDefault(errorMap, defaultError, throwable)
    }, block)
}

inline fun onCatch(crossinline errorEmitter: ((Throwable) -> Enum<*>?), throwable: Throwable) {
    when (throwable) {
        // Use InternalException for propagation purposes
        // If the InternalException contains an Enum defined in the Protocol, convert it to a GrpcServerException to finish the chain.
        // Otherwise, continue propagating the InternalException so that it is handled by the upper layer.
        is InternalException -> {
            val error = errorEmitter(throwable)
            when (error) {
                is ProtocolMessageEnum -> {
                    throw GrpcServerException(throwable, error)
                }

                else -> {
                    throw InternalException(error, throwable.cause ?: throwable)
                }
            }
        }

        // already gRPC exception, just rethrow
        // FIXME: In the current structure, if a GrpcException occurs within a block, the upper errorEmitter is ignored, so it should be modified later
        is GrpcException -> throw throwable

        // exception coming from gRPC client
        is io.grpc.StatusException, is io.grpc.StatusRuntimeException -> {
            // Armeria gRPC client exception
            if (throwable.cause is com.linecorp.armeria.common.grpc.StatusCauseException)
                throw ArmeriaGrpcClientCallException(throwable.cause as com.linecorp.armeria.common.grpc.StatusCauseException, errorEmitter(throwable))
            throw GrpcClientCallException(throwable.cause ?: throwable, errorEmitter(throwable))
        }

        // otherwise take it as server exception
        else -> throw GrpcServerException(throwable, errorEmitter(throwable))
    }
}

fun findInErrorMapOrDefault(
    errorMap: Map<Enum<*>, Enum<*>>,
    defaultError: Enum<*>,
    throwable: Throwable,
): Enum<*> {
    return errorMap.entries.find { (input, output) ->
        when (throwable) {
            is io.grpc.StatusRuntimeException -> {
                input.name == throwable.extractErrorName()
            }

            is io.grpc.StatusException -> {
                input.name == throwable.extractErrorName()
            }

            is GrpcException -> {
                input == throwable.error
            }

            else -> {
                false
            }
        }
    }?.value
        ?: defaultError
} 