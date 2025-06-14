package io.github.minsujang0.armeria_on_spring.grpc

import io.github.minsujang0.armeria_on_spring.service.GreeterService
import io.github.minsujang0.armeria_on_spring.util.armeria.web.userIdFromHeader
import io.github.minsujang0.greeter.v1.GreeterNew2ServiceGrpcKt
import io.github.minsujang0.greeter.v1.SayHelloNew2Request
import io.github.minsujang0.greeter.v1.SayHelloNew2Response
import io.github.minsujang0.greeter.v1.sayHelloNew2Response
import org.springframework.stereotype.Component

/**
 * A modern, non-blocking gRPC service implemented with Kotlin Coroutines.
 *
 * This service leverages `suspend` functions to handle requests asynchronously, making it
 * highly efficient and scalable. It integrates naturally with Armeria's event loop and
 * represents the recommended approach for building high-performance microservices.
 */
@Component
class GreeterNew2GrpcService(
    private val greeterService: GreeterService,
) : GreeterNew2ServiceGrpcKt.GreeterNew2ServiceCoroutineImplBase() {
    override suspend fun sayHelloNew2(request: SayHelloNew2Request): SayHelloNew2Response {
        // Need R2DBC or other reactive database access to fetch data in transaction

        return sayHelloNew2Response {
            this.message =
                "Hello, ${request.name}! You look like $userIdFromHeader." // get userIdFromHeader from Armeria's context
        }
    }
} 