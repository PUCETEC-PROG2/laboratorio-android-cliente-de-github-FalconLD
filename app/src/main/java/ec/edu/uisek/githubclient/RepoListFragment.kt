package ec.edu.uisek.githubclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import ec.edu.uisek.githubclient.databinding.FragmentRepoListBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoListFragment : Fragment() {

    interface RepoListListener {
        fun onAddRepositoryClick()
        fun onEditRepositoryClick(repo: Repo)
    }

    private var _binding: FragmentRepoListBinding? = null
    private val binding get() = _binding!!

    private lateinit var reposAdapter: ReposAdapter
    private var listener: RepoListListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRepoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener = activity as? RepoListListener

        reposAdapter = ReposAdapter(
            onEditClick = { repo -> listener?.onEditRepositoryClick(repo) },
            onDeleteClick = { repo -> confirmDelete(repo) },
        )
        binding.reposRecyclerView.adapter = reposAdapter
        // FAB: navegación a formulario de creación (FAB en este fragment)
        binding.fabAddRepo.setOnClickListener { listener?.onAddRepositoryClick() }
        fetchRepositories()
    }

    fun fetchRepositories() {
        if (_binding == null) return
        setLoading(true)
        binding.errorText.visibility = View.GONE

        RetrofitClient.gitHubApiService
            .getAuthenticatedUserRepositories()
            .enqueue(object : Callback<List<Repo>> {
                override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                    if (_binding == null) return
                    setLoading(false)
                    if (response.isSuccessful) {
                        val repos = response.body().orEmpty().distinctBy { it.id }
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showError(mapHttpError(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                    if (_binding == null) return
                    setLoading(false)
                    showError(getString(R.string.connection_error, t.message ?: ""))
                }
            })
    }

    private fun confirmDelete(repo: Repo) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirm_title)
            .setMessage(getString(R.string.delete_confirm_message, repo.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteRepository(repo)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteRepository(repo: Repo) {
        setLoading(true)
        RetrofitClient.gitHubApiService
            .deleteRepository(repo.apiOwner(), repo.apiRepoName())
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), R.string.delete_success, Toast.LENGTH_SHORT).show()
                        fetchRepositories()
                    } else {
                        setLoading(false)
                        showError(mapHttpError(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    if (_binding == null) return
                    setLoading(false)
                    showError(getString(R.string.connection_error, t.message ?: ""))
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun mapHttpError(code: Int, message: String): String {
        return when (code) {
            401 -> getString(R.string.error_auth)
            403 -> getString(R.string.error_forbidden)
            404 -> getString(R.string.error_not_found_repo)
            else -> getString(R.string.error_http, code, message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RepoListFragment"

        fun newInstance() = RepoListFragment()
    }
}
