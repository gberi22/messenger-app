package ge.ngvalia.messengerapp.userdiscovery.ui

import android.os.Bundle
import android.util.Log
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ge.ngvalia.messengerapp.R
import ge.ngvalia.messengerapp.databinding.FragmentUserDiscoveryBinding
import ge.ngvalia.messengerapp.userdiscovery.ui.adapter.UserListAdapter
import ge.ngvalia.messengerapp.userdiscovery.viewmodel.UserDiscoveryViewModel
import ge.ngvalia.messengerapp.userdiscovery.viewmodel.UserDiscoveryViewModelFactory
import ge.ngvalia.messengerapp.userdiscovery.data.repository.UserRepository
import ge.ngvalia.messengerapp.userdiscovery.network.UserApi

class UserDiscoveryFragment : Fragment() {

    private var _binding: FragmentUserDiscoveryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserDiscoveryViewModel by viewModels {
        UserDiscoveryViewModelFactory(UserRepository(UserApi()))
    }
    private lateinit var adapter: UserListAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserDiscoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val backButton: ImageButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        adapter = UserListAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        // Lazy loading (if paginated)
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(1)) {
                    viewModel.onScrolledToEnd()
                }
            }
        })

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""

                when {
                    query.length >= 2 -> viewModel.searchUsers(query)
                    query.isEmpty() -> viewModel.clearSearch()
                    // For 1 character, do nothing to avoid too many requests
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Observers
        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            binding.searchProgressBar.visibility = if (isSearching) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // Show appropriate loading indicator
                if (binding.searchEditText.text.isNullOrEmpty()) {
                    // Initial load or pagination
                    binding.mainProgressBar.visibility = View.VISIBLE
                    binding.searchProgressBar.visibility = View.GONE
                } else {
                    // Search loading
                    binding.searchProgressBar.visibility = View.VISIBLE
                    binding.mainProgressBar.visibility = View.GONE
                }
                // Hide error and empty states
                binding.errorStateLayout.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
            } else {
                // Hide all loading indicators
                binding.mainProgressBar.visibility = View.GONE
                binding.searchProgressBar.visibility = View.GONE
            }
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)

            // Show empty state if no users and not loading
            if (users.isEmpty() && viewModel.isLoading.value != true) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.errorStateLayout.visibility = View.GONE

                // Update empty state text based on search
                val searchText = binding.searchEditText.text.toString()
                binding.emptyStateText.text = if (searchText.isNotEmpty()) {
                    "No users found for '$searchText'"
                } else {
                    "No users found"
                }
            } else {
                binding.emptyStateLayout.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                binding.errorStateLayout.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
                binding.errorStateText.text = errorMsg
            } else {
                binding.errorStateLayout.visibility = View.GONE
            }
        }

        binding.retryButton.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}