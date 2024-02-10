package com.distasilucas.cryptobalancetracker.model.response.platform

import java.io.Serializable

data class PlatformResponse(
  val id: String,
  val name: String
) : Serializable
