package io.github.minsujang0.armeria_on_spring.util.armeria.web

import com.linecorp.armeria.server.ServiceRequestContext
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.blockingCatch
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.suspendingCatch
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T> withEventLoopCatching(block: suspend CoroutineScope.() -> T): T {
    return withContext(
        ServiceRequestContext.current().eventLoop().asCoroutineDispatcher(),
        { suspendingCatch(block = block) })
}

fun <T> StreamObserver<T>.returnCatching(block: () -> T) {
    onNext(blockingCatch(block = block))
    onCompleted()
}