package com.distasilucas.cryptobalancetracker.entity

import com.distasilucas.cryptobalancetracker.model.Role
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.util.UUID

@Document("Users")
data class User(
    @Id
    private val id: String = UUID.randomUUID().toString(),
    private val username: String,
    private val password: String,
    private val role: Role,

    @Field("created_at")
    private val createdAt: LocalDateTime

): UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(role.name))

    override fun getPassword() = password

    override fun getUsername() = username

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true

}