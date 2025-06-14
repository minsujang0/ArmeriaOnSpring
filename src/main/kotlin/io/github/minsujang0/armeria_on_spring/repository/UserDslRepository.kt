package io.github.minsujang0.armeria_on_spring.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import io.github.minsujang0.armeria_on_spring.entity.QUser.user
import io.github.minsujang0.armeria_on_spring.entity.User
import org.springframework.stereotype.Component

@Component
class UserDslRepository(private val queryFactory: JPAQueryFactory) {
    fun findByName(name: String): User? {
        return queryFactory.selectFrom(user)
            .where(user.name.eq(name))
            .fetchOne()
    }
}