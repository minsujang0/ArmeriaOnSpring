package io.github.minsujang0.armeria_on_spring.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class User(
    val name: String,
    val nickname: String,
) {
    val isBanned: Boolean = false

    @Id
    var id: Long? = null
}