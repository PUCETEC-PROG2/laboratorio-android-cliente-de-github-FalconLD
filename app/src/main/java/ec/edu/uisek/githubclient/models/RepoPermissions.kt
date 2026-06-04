package ec.edu.uisek.githubclient.models

data class RepoPermissions(
    val admin: Boolean = false,
    val push: Boolean = false,
    val pull: Boolean = false,
) {
    fun canModify(): Boolean = admin || push
}
