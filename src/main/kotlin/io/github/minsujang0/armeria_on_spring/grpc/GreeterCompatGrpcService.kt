package io.github.minsujang0.armeria_on_spring.grpc

import io.github.minsujang0.armeria_on_spring.service.GreeterService
import io.github.minsujang0.armeria_on_spring.util.armeria.web.returnCatching
import io.github.minsujang0.greeter.v1.GreeterCompatServiceGrpc
import io.github.minsujang0.greeter.v1.SayHelloCompatRequest
import io.github.minsujang0.greeter.v1.SayHelloCompatResponse
import io.github.minsujang0.greeter.v1.sayHelloCompatResponse
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Component

@Component
class GreeterCompatGrpcService(private val greeterService: GreeterService) :
    GreeterCompatServiceGrpc.GreeterCompatServiceImplBase() {
    override fun sayHelloCompat(
        request: SayHelloCompatRequest,
        responseObserver: StreamObserver<SayHelloCompatResponse>,
    ) {
        responseObserver.returnCatching {
            sayHelloCompatResponse {
                this.message = greeterService.sayHello(request.name)
            }
        }
    }
}