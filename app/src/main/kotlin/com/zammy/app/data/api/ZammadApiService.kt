package com.zammy.app.data.api

import com.zammy.app.data.api.model.ArticleDto
import com.zammy.app.data.api.model.ArticleRequest
import com.zammy.app.data.api.model.CreateTicketRequest
import com.zammy.app.data.api.model.GroupDto
import com.zammy.app.data.api.model.TicketDto
import com.zammy.app.data.api.model.UpdateTicketRequest
import com.zammy.app.data.api.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ZammadApiService {

    @GET("api/v1/tickets")
    suspend fun getTickets(
        @Query("state") state: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,
        @Query("expand") expand: Boolean = true
    ): List<TicketDto>

    @GET("api/v1/tickets/{id}")
    suspend fun getTicket(
        @Path("id") id: Int,
        @Query("expand") expand: Boolean = true
    ): TicketDto

    @POST("api/v1/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): TicketDto

    @PUT("api/v1/tickets/{id}")
    suspend fun updateTicket(
        @Path("id") id: Int,
        @Body request: UpdateTicketRequest
    ): TicketDto

    @GET("api/v1/ticket_articles/by_ticket/{ticketId}")
    suspend fun getArticles(
        @Path("ticketId") ticketId: Int,
        @Query("expand") expand: Boolean = true
    ): List<ArticleDto>

    @POST("api/v1/ticket_articles")
    suspend fun createArticle(@Body request: Map<String, Any>): ArticleDto

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): UserDto

    @GET("api/v1/users/{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    @GET("api/v1/users")
    suspend fun getAgents(
        @Query("role") role: String = "Agent",
        @Query("expand") expand: Boolean = false
    ): List<UserDto>

    @GET("api/v1/groups")
    suspend fun getGroups(): List<GroupDto>

    @GET("api/v1/tickets/search")
    suspend fun searchTickets(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("expand") expand: Boolean = true
    ): List<TicketDto>
}
