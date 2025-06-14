package io.github.minsujang0.armeria_on_spring.service

import io.github.minsujang0.armeria_on_spring.repository.UserDslRepository
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.GrpcErrorException
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.errorMapCatch
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.simpleCatch
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.throwableMapCatch
import io.github.minsujang0.greeter.v1.GreeterNewError
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A standard Spring service that contains business logic.
 * This service is marked as transactional, which is a common pattern in Spring applications.
 * It can be seamlessly injected into both Spring MVC controllers and Armeria gRPC services.
 */
@Service
class GreeterService(
    private val userDslRepository: UserDslRepository,
    private val userInternalService: UserInternalService,
) {
    @Transactional
    fun sayHello(name: String): String {
        val user = userDslRepository.findByName(name)
            ?: throw GrpcErrorException(GreeterNewError.GREETER_NEW_ERROR_USER_NOT_FOUND)

        simpleCatch(
            GreeterNewError.GREETER_NEW_ERROR_USER_INVALID
        ) {
            userInternalService.checkUserInvalid(user)
        }

        throwableMapCatch(
            mapOf(
                IllegalStateException::class.java to GreeterNewError.GREETER_NEW_ERROR_USER_INVALID
            ),
            GreeterNewError.GREETER_NEW_ERROR_UNSPECIFIED
        ) {
            userInternalService.checkUserState(user)
        }

        val userBanned = errorMapCatch(
            mapOf(UserInternalService.Error.BANNED_USER to GreeterNewError.GREETER_NEW_ERROR_USER_BANNED),
            GreeterNewError.GREETER_NEW_ERROR_UNSPECIFIED
        ) {
            userInternalService.checkUserBanned(user)
        }
        if (!userBanned) println("User ${user.nickname} is ok.")


        return "Hello, ${user.nickname}!"
    }
}