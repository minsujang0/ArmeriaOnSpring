package io.github.minsujang0.armeria_on_spring.grpc

import io.github.minsujang0.armeria_on_spring.service.GreeterService
import io.github.minsujang0.greeter.v1.GreeterNewServiceGrpc
import io.github.minsujang0.greeter.v1.SayHelloNewRequest
import io.github.minsujang0.greeter.v1.SayHelloNewResponse
import io.github.minsujang0.greeter.v1.sayHelloNewResponse
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Component

@Component
class GreeterNewGrpcService(private val greeterService: GreeterService) :
    GreeterNewServiceGrpc.GreeterNewServiceImplBase() {
    override fun sayHelloNew(
        request: SayHelloNewRequest,
        responseObserver: StreamObserver<SayHelloNewResponse>,
    ) {
        responseObserver.runCatching {
            sayHelloNewResponse {
                this.message = greeterService.sayHello(request.name)
            }
        }
    }
} 