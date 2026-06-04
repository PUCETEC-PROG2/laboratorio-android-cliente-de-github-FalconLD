package ec.edu.uisek.githubclient

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo

class MainActivity : AppCompatActivity(),
    RepoListFragment.RepoListListener,
    RepoFormFragment.RepoFormListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showListFragment()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStackImmediate()
                    refreshListIfVisible()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onAddRepositoryClick() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, RepoFormFragment.newInstance())
            addToBackStack(null)
        }
    }

    override fun onEditRepositoryClick(repo: Repo) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, RepoFormFragment.newInstance(repo))
            addToBackStack(null)
        }
    }

    override fun onFormCancelled() {
        supportFragmentManager.popBackStackImmediate()
    }

    override fun onFormSaved() {
        supportFragmentManager.popBackStackImmediate()
        refreshListIfVisible()
    }

    private fun refreshListIfVisible() {
        (supportFragmentManager.findFragmentById(R.id.fragment_container) as? RepoListFragment)
            ?.fetchRepositories()
    }

    private fun showListFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, RepoListFragment.newInstance())
        }
    }

}
