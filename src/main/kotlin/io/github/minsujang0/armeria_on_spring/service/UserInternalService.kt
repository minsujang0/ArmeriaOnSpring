package io.github.minsujang0.armeria_on_spring.service

import io.github.minsujang0.armeria_on_spring.entity.User
import io.github.minsujang0.armeria_on_spring.util.armeria.grpc.exception.InternalException
import org.springframework.stereotype.Service

@Service
class UserInternalService {
    fun checkUserBanned(user: User): Boolean {
        if (user.isBanned) {
            throw InternalException(Error.BANNED_USER)
        }
        return false
    }

    fun checkUserState(user: User) {
        if (user.nickname.isBlank()) {
            throw IllegalStateException("User nickname cannot be blank")
        }
    }

    fun checkUserInvalid(user: User) {
        if (user.name.startsWith("invalid_")) {
            throw IllegalArgumentException("Invalid user: ${user.name}")
        }
    }

    enum class Error {
        BANNED_USER
    }
}