package com.distasilucas.cryptobalancetracker.service

import com.distasilucas.cryptobalancetracker.constants.TOKEN_EXPIRED
import com.distasilucas.cryptobalancetracker.exception.ApiException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import java.util.function.Function
import javax.crypto.SecretKey

@Service
@ConditionalOnProperty(prefix = "security", name = ["enabled"], havingValue = "true")
class JwtService(
  @Value("\${jwt.signing-key}")
  private val jwtSigningKey: String
) {

  private val logger = KotlinLogging.logger { }

  fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
    logger.info { "Validating JWT token for ${userDetails.username}" }

    return isTokenNonExpired(token) && extractUsername(token) == userDetails.username
  }

  fun extractUsername(token: String): String {
    return extractClaim<String>(token, Claims::getSubject)
  }

  private fun isTokenNonExpired(token: String): Boolean {
    val date = extractClaim<Date>(token, Claims::getExpiration)
    return date.after(Date())
  }

  private fun <T> extractClaim(token: String, claims: Function<Claims, T>): T {
    val claim: Claims = extractClaims(token)
    return claims.apply(claim)
  }

  private fun extractClaims(token: String): Claims {
    return try {
      Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .payload
    } catch (ex: ExpiredJwtException) {
      throw ApiException(HttpStatus.BAD_REQUEST, TOKEN_EXPIRED)
    } catch (ex: Exception) {
      logger.warn { "Exception when parsing JWT Token. ${ex.message}" }
      throw ApiException()
    }
  }

  private fun getSigningKey(): SecretKey {
    val decoders: ByteArray = Decoders.BASE64.decode(jwtSigningKey)

    return Keys.hmacShaKeyFor(decoders)
  }

}
