package io.github.minsujang0.armeria_on_spring.service

import io.github.minsujang0.armeria_on_spring.repository.UserDslRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GreeterService(private val userDslRepository: UserDslRepository) {
    @Transactional
    fun sayHello(name: String): String {
        val user = userDslRepository.findByName(name)
            ?: throw IllegalArgumentException("User with name '$name' not found")
        return "Hello, ${user.nickname}!"
    }
}