package com.zammy.app.data.repository

import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.domain.model.User
import com.zammy.app.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: ZammadApiService
) : UserRepository {

    override suspend fun getCurrentUser(): Result<User> = runCatching {
        val dto = api.getCurrentUser()
        User(
            id = dto.id,
            login = dto.login,
            firstname = dto.firstname ?: "",
            lastname = dto.lastname ?: "",
            email = dto.email ?: "",
            active = dto.active ?: true
        )
    }

    override suspend fun getUser(id: Int): Result<User> = runCatching {
        val dto = api.getUser(id)
        User(
            id = dto.id,
            login = dto.login,
            firstname = dto.firstname ?: "",
            lastname = dto.lastname ?: "",
            email = dto.email ?: "",
            active = dto.active ?: true
        )
    }

    override suspend fun getAgents(): Result<List<User>> = runCatching {
        api.getAgents().map { dto ->
            User(
                id = dto.id,
                login = dto.login,
                firstname = dto.firstname ?: "",
                lastname = dto.lastname ?: "",
                email = dto.email ?: "",
                active = dto.active ?: true
            )
        }
    }
}
