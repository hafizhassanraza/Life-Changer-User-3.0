package com.enfotrix.life_changer_user_2_0.api.Responses

data class ResSignup(
    val success: Boolean,
    val data: Data?,
    val message: String
)

data class Data(
    val token: String,
    val name: String
)
