package com.zammy.app.domain.model

data class User(
    val id: Int,
    val login: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val active: Boolean = true,
    val roles: List<String> = emptyList()
) {
    val fullName: String get() = "$firstname $lastname".trim()
}
