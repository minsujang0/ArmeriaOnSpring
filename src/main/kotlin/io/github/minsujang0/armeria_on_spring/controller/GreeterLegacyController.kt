package io.github.minsujang0.armeria_on_spring.controller

import io.github.minsujang0.armeria_on_spring.service.GreeterService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class SayHelloLegacyRequest(val name: String)
data class SayHelloLegacyResponse(val message: String)

@RestController
@RequestMapping("/v1/greeter")
class GreeterLegacyController(
    private val greeterService: GreeterService
) {

    @PostMapping("/say-hello")
    fun sayHello(@RequestBody request: SayHelloLegacyRequest): SayHelloLegacyResponse {
        val message = greeterService.sayHello(request.name)
        return SayHelloLegacyResponse(message)
    }
} 