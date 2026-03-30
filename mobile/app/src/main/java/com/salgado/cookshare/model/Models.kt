package com.salgado.cookshare.model

data class User(
    val userID: Long? = null,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val userEmail: String,
    val userPassword: String
)

