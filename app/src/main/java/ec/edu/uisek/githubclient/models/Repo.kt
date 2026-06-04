package ec.edu.uisek.githubclient.models

import com.google.gson.annotations.SerializedName

data class Repo(
    val id: Long,
    val name: String,
    val description: String?,
    val language: String?,
    val owner: RepoOwner,
    @SerializedName("full_name")
    val fullName: String? = null,
    val permissions: RepoPermissions? = null,
) {
    fun apiOwner(): String = parseOwnerFromFullName(fullName) ?: owner.login

    fun apiRepoName(): String = parseRepoNameFromFullName(fullName) ?: name

    fun canModify(): Boolean = permissions?.canModify() ?: false

    companion object {
        fun parseOwnerFromFullName(fullName: String?): String? {
            if (fullName.isNullOrBlank()) return null
            val slash = fullName.indexOf('/')
            if (slash <= 0) return null
            return fullName.substring(0, slash)
        }

        fun parseRepoNameFromFullName(fullName: String?): String? {
            if (fullName.isNullOrBlank()) return null
            val slash = fullName.indexOf('/')
            if (slash < 0 || slash >= fullName.length - 1) return null
            return fullName.substring(slash + 1)
        }

        fun buildFullName(ownerLogin: String, repoName: String): String =
            "$ownerLogin/$repoName"
    }
}
