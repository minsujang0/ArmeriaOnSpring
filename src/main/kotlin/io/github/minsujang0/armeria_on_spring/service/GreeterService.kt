package io.github.minsujang0.armeria_on_spring.service

import io.github.minsujang0.armeria_on_spring.repository.UserDslRepository
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.GrpcErrorException
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.errorMapCatch
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.simpleCatch
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.throwableMapCatch
import io.github.minsujang0.greeter.v1.GreeterNewError
import io.github.minsujang0.level_service.v1.LevelServiceError
import io.github.minsujang0.level_service.v1.LevelServiceGrpc
import io.github.minsujang0.level_service.v1.getLevelRequest
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
    private val levelGrpcClient: LevelServiceGrpc.LevelServiceBlockingV2Stub,
) {
    @Transactional
    fun sayHello(name: String): String {
        // Case 1: Explicitly throwing a GrpcErrorException.
        // This is the most direct way to return a specific, predefined gRPC error to the client.
        // If the user is not found, we immediately stop execution and return a USER_NOT_FOUND error.
        val user = userDslRepository.findByName(name)
            ?: throw GrpcErrorException(GreeterNewError.GREETER_NEW_ERROR_USER_NOT_FOUND)

        // Case 2: Using `simpleCatch` for generic exception handling.
        // Any `Throwable` that occurs within this block will be caught and wrapped in a
        // `GrpcServerException` with the specified error code (USER_INVALID).
        // This is useful for wrapping segments of code where the specific type of exception is less
        // important than the context in which it occurred.
        simpleCatch(
            GreeterNewError.GREETER_NEW_ERROR_USER_NOT_FOUND
        ) {
            userInternalService.checkUserInvalid(user)
        }

        // Case 3: Using `throwableMapCatch` for handling specific Throwables.
        // This utility catches exceptions and maps them to specific gRPC errors based on their type.
        // Here, an `IllegalStateException` is mapped to a USER_INVALID error.
        // If any other exception occurs, it falls back to the default UNSPECIFIED error.
        throwableMapCatch(
            mapOf(
                IllegalStateException::class.java to GreeterNewError.GREETER_NEW_ERROR_USER_NOT_FOUND
            ),
            GreeterNewError.GREETER_NEW_ERROR_UNSPECIFIED
        ) {
            userInternalService.checkUserState(user)
        }

        // Case 4: Using `errorMapCatch` to handle and translate `InternalException`.
        // This block calls another service (`userInternalService`) which may throw its own `InternalException`.
        // `errorMapCatch` catches this exception and translates the internal error enum (`UserInternalService.Error.BANNED_USER`)
        // into the corresponding gRPC error enum for this service (`GreeterNewError.GREETER_NEW_ERROR_USER_BANNED`).
        // This maintains a clear separation of error domains between internal services.
        val userBanned = errorMapCatch(
            mapOf(UserInternalService.Error.BANNED_USER to GreeterNewError.GREETER_NEW_ERROR_USER_NOT_FOUND),
            GreeterNewError.GREETER_NEW_ERROR_UNSPECIFIED
        ) {
            userInternalService.checkUserBanned(user)
        }
        if (!userBanned) println("User ${user.nickname} is ok.")

        // Case 5: Handling errors from a downstream gRPC service call.
        // This block makes a call to an external `LevelService` using a generated gRPC client.
        // `errorMapCatch` is used again, this time to catch a `GrpcClientCallException` that may be
        // thrown by the client if the downstream service returns an error. It then maps the
        // specific error from the external service (`LevelServiceError.LEVEL_SYSTEM_UNAVAILABLE`)
        // to an appropriate error for the current service (`GreeterNewError.SERVICE_UNAVAILABLE`).
        // This demonstrates how to create a resilient service that gracefully handles failures
        // in its dependencies.
        val level = errorMapCatch(
            mapOf(LevelServiceError.LEVEL_SERVICE_ERROR_LEVEL_SYSTEM_UNAVAILABLE to GreeterNewError.GREETER_NEW_ERROR_SERVICE_UNAVAILABLE),
            GreeterNewError.GREETER_NEW_ERROR_UNSPECIFIED
        ) {
            levelGrpcClient.getLevel(getLevelRequest { this.userId = user.id!!.toString() })
        }.level
        if (level == 0) {
            throw GrpcErrorException(GreeterNewError.GREETER_NEW_ERROR_USER_INVALID)
        }

        return "Hello, ${user.nickname}!"
    }
}