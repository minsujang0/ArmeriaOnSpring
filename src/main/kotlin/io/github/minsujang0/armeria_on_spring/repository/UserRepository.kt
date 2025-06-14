package io.github.minsujang0.armeria_on_spring.repository

import io.github.minsujang0.armeria_on_spring.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
}