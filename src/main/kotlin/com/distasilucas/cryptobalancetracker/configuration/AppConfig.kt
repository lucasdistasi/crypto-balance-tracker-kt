package com.distasilucas.cryptobalancetracker.configuration

import com.distasilucas.cryptobalancetracker.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AppConfig(private val userService: UserService) {

  @Bean
  fun authenticationProvider(): AuthenticationProvider {
    val daoAuthenticationProvider = DaoAuthenticationProvider()
    daoAuthenticationProvider.setUserDetailsService(userDetailsService())
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder())

    return daoAuthenticationProvider
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun userDetailsService() = UserDetailsService { userService.findByUsername(it) }
}
