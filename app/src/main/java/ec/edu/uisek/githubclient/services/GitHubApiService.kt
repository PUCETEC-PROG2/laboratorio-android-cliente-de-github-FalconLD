package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoPayload
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {

    companion object {
        const val AFFILIATION_LIST = "owner,collaborator,organization_member"
    }

    @GET("user/repos")
    fun getAuthenticatedUserRepositories(
        @Query("affiliation") affiliation: String = AFFILIATION_LIST,
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc",
        @Query("per_page") perPage: Int = 100,
    ): Call<List<Repo>>

    @POST("user/repos")
    fun createRepository(@Body repository: RepoPayload): Call<Repo>

    @PATCH("repos/{owner}/{repo}")
    fun updateRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body repository: RepoPayload,
    ): Call<Repo>

    @DELETE("repos/{owner}/{repo}")
    fun deleteRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): Call<Void>
}
