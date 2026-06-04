package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import ec.edu.uisek.githubclient.databinding.FragmentRepoItemBinding
import ec.edu.uisek.githubclient.models.Repo

class RepoViewHolder(
    private val binding: FragmentRepoItemBinding,
    private val onEditClick: (Repo) -> Unit,
    private val onDeleteClick: (Repo) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(repo: Repo) {
        binding.repoName.text = repo.name
        binding.repoDescription.text = repo.description ?: binding.root.context.getString(R.string.no_description)
        binding.repoLanguage.text = repo.language ?: binding.root.context.getString(R.string.no_language)

        val avatarCacheKey = repo.owner.updatedAt ?: repo.owner.id.toString()
        val avatarUrl = repo.owner.avatarUrl.let { url ->
            val separator = if (url.contains('?')) "&" else "?"
            "$url${separator}cb=$avatarCacheKey"
        }

        Glide.with(binding.root.context)
            .load(avatarUrl)
            .signature(ObjectKey(avatarCacheKey))
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .circleCrop()
            .into(binding.repoOwnerImage)

        val canModify = repo.canModify()
        binding.btnEdit.isEnabled = canModify
        binding.btnDelete.isEnabled = canModify
        binding.btnEdit.alpha = if (canModify) 1f else 0.38f
        binding.btnDelete.alpha = if (canModify) 1f else 0.38f
        if (canModify) {
            binding.btnEdit.setOnClickListener { onEditClick(repo) }
            binding.btnDelete.setOnClickListener { onDeleteClick(repo) }
        } else {
            binding.btnEdit.setOnClickListener(null)
            binding.btnDelete.setOnClickListener(null)
        }
    }
}

class ReposAdapter(
    private val onEditClick: (Repo) -> Unit,
    private val onDeleteClick: (Repo) -> Unit,
) : RecyclerView.Adapter<RepoViewHolder>() {

    private var repositories: List<Repo> = emptyList()

    override fun getItemCount(): Int = repositories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = FragmentRepoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return RepoViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    fun updateRepositories(newRepositories: List<Repo>) {
        repositories = newRepositories
        notifyDataSetChanged()
    }
}
