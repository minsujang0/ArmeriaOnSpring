package io.github.minsujang0.armeria_on_spring.grpc

import io.github.minsujang0.greeter.v1.GreeterNew2ServiceGrpcKt
import io.github.minsujang0.greeter.v1.SayHelloNew2Request
import io.github.minsujang0.greeter.v1.SayHelloNew2Response
import io.github.minsujang0.greeter.v1.sayHelloNew2Response
import org.springframework.stereotype.Component

@Component
class GreeterNew2GrpcService : GreeterNew2ServiceGrpcKt.GreeterNew2ServiceCoroutineImplBase() {
    override suspend fun sayHelloNew2(request: SayHelloNew2Request): SayHelloNew2Response {
        // Need R2DBC or other reactive database access to fetch data in transaction

        return sayHelloNew2Response {
            this.message = "Hello, ${request.name}!"
        }
    }
} 