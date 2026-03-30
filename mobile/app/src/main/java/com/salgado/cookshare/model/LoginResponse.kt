package com.salgado.cookshare.model

data class LoginResponse(
    val message: String,
    val firstName: String,
    val lastName: String,
    val email: String
)