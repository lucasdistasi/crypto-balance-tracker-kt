package com.distasilucas.cryptobalancetracker.configuration

import com.distasilucas.cryptobalancetracker.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "security", name = ["enabled"], havingValue = "true")
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        val headerType = if (StringUtils.isBlank(authHeader)) "" else authHeader.split(" ")[0]

        if (StringUtils.isBlank(authHeader) || isNotBearerToken(headerType)) {
            filterChain.doFilter(request, response)
            return
        }

        val jwtToken = authHeader.split(" ")[1]
        val userName: String = jwtService.extractUsername(jwtToken)

        if (StringUtils.isNotBlank(userName) && isNotAlreadyAuthenticated()) {
            val userDetails = userDetailsService.loadUserByUsername(userName)

            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                val authenticationToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )

                val webAuthenticationDetails = WebAuthenticationDetailsSource().buildDetails(request)
                authenticationToken.details = webAuthenticationDetails

                val securityContext = SecurityContextHolder.getContext()
                securityContext.authentication = authenticationToken
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun isNotBearerToken(authHeader: String) = authHeader != "Bearer"

    private fun isNotAlreadyAuthenticated() = null == SecurityContextHolder.getContext().authentication
}