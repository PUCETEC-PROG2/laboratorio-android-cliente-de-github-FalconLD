package ec.edu.uisek.githubclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ec.edu.uisek.githubclient.databinding.FragmentRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoPayload
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoFormFragment : Fragment() {

    interface RepoFormListener {
        fun onFormCancelled()
        fun onFormSaved()
    }

    private var _binding: FragmentRepoFormBinding? = null
    private val binding get() = _binding!!

    private var listener: RepoFormListener? = null
    private var editOwner: String? = null
    private var editRepoName: String? = null
    private var editFullName: String? = null
    private val isEditMode: Boolean
        get() = editOwner != null && editRepoName != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            editFullName = args.getString(ARG_FULL_NAME)
            editOwner = args.getString(ARG_OWNER)
            editRepoName = args.getString(ARG_REPO_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRepoFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener = activity as? RepoFormListener

        if (isEditMode) {
            binding.formTitle.setText(R.string.edit_repository)
            binding.editRepoName.setText(editRepoName.orEmpty())
            binding.editRepoDescription.setText(arguments?.getString(ARG_DESCRIPTION).orEmpty())
            binding.editRepoName.isEnabled = false
        } else {
            binding.formTitle.setText(R.string.create_repository)
        }

        binding.btnCancel.setOnClickListener { listener?.onFormCancelled() }
        binding.btnSave.setOnClickListener { saveRepository() }
    }

    private fun saveRepository() {
        val name = binding.editRepoName.text.toString().trim()
        val description = binding.editRepoDescription.text.toString().trim()

        if (!isEditMode && name.isBlank()) {
            binding.formError.text = getString(R.string.error_name_required)
            binding.formError.visibility = View.VISIBLE
            return
        }

        binding.formError.visibility = View.GONE
        setFormLoading(true)

        if (isEditMode) {
            val owner = Repo.parseOwnerFromFullName(editFullName) ?: editOwner ?: return
            val repoName = Repo.parseRepoNameFromFullName(editFullName) ?: editRepoName ?: return
            val payload = RepoPayload(description = description)
            RetrofitClient.gitHubApiService
                .updateRepository(owner, repoName, payload)
                .enqueue(repoCallback())
        } else {
            val payload = RepoPayload(
                name = name,
                description = description.ifBlank { null },
            )
            RetrofitClient.gitHubApiService
                .createRepository(payload)
                .enqueue(repoCallback())
        }
    }

    private fun repoCallback(): Callback<Repo> = object : Callback<Repo> {
        override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
            if (_binding == null) return
            setFormLoading(false)
            if (response.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) R.string.update_success else R.string.create_success,
                    Toast.LENGTH_SHORT,
                ).show()
                listener?.onFormSaved()
            } else {
                showFormError(mapHttpError(response.code(), response.message()))
            }
        }

        override fun onFailure(call: Call<Repo>, t: Throwable) {
            if (_binding == null) return
            setFormLoading(false)
            showFormError(getString(R.string.connection_error, t.message ?: ""))
        }
    }

    private fun setFormLoading(loading: Boolean) {
        binding.formProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !loading
        binding.btnCancel.isEnabled = !loading
    }

    private fun showFormError(message: String) {
        binding.formError.text = message
        binding.formError.visibility = View.VISIBLE
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
        private const val ARG_FULL_NAME = "arg_full_name"
        private const val ARG_OWNER = "arg_owner"
        private const val ARG_REPO_NAME = "arg_repo_name"
        private const val ARG_DESCRIPTION = "arg_description"

        fun newInstance(): RepoFormFragment = RepoFormFragment()

        fun newInstance(repo: Repo): RepoFormFragment = RepoFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FULL_NAME, repo.fullName ?: Repo.buildFullName(repo.apiOwner(), repo.apiRepoName()))
                putString(ARG_OWNER, repo.apiOwner())
                putString(ARG_REPO_NAME, repo.apiRepoName())
                putString(ARG_DESCRIPTION, repo.description)
            }
        }
    }
}
