package com.zammy.app.domain.usecase

import com.zammy.app.domain.model.User
import com.zammy.app.domain.repository.UserRepository
import javax.inject.Inject

class GetAgentsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> = userRepository.getAgents()
}
