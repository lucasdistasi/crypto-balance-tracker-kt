package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.USERNAME_NOT_FOUND
import com.distasilucas.cryptobalancetracker.entity.User
import com.distasilucas.cryptobalancetracker.model.Role
import com.distasilucas.cryptobalancetracker.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.Optional

class UserServiceTest {

    private val userRepositoryMock = mockk<UserRepository>()

    private val userService = UserService(userRepositoryMock)

    @Test
    fun `should retrieve user`() {
        val userEntity = User(
            username = "username",
            password = "password",
            role = Role.ROLE_ADMIN,
            createdAt = LocalDateTime.now()
        )

        every { userRepositoryMock.findByUsername("username") } returns Optional.of(userEntity)

        val user = userService.findByUsername("username")

        assertThat(user)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt")
            .isEqualTo(
                User(
                    username = "username",
                    password = "password",
                    role = Role.ROLE_ADMIN,
                    createdAt = LocalDateTime.now()
                )
            )
    }

    @Test
    fun `should throw UsernameNotFoundException if user does not exists`() {
        every { userRepositoryMock.findByUsername("username") } returns Optional.empty()

        val exception = assertThrows<UsernameNotFoundException> { userService.findByUsername("username") }

        assertThat(exception.message).isEqualTo(USERNAME_NOT_FOUND)
    }
}