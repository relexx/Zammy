package com.zammy.app.domain.repository

import com.zammy.app.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun getUser(id: Int): Result<User>
    suspend fun getAgents(): Result<List<User>>
}
