package ge.ngvalia.messengerapp.userdiscovery.ui

import android.content.Intent
import android.os.Bundle
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
import ge.ngvalia.messengerapp.data.repository.SearchUserRepository
import ge.ngvalia.messengerapp.databinding.FragmentUserDiscoveryBinding
import ge.ngvalia.messengerapp.userdiscovery.ui.adapter.UserListAdapter
import ge.ngvalia.messengerapp.userdiscovery.viewmodel.UserDiscoveryViewModel
import ge.ngvalia.messengerapp.userdiscovery.viewmodel.UserDiscoveryViewModelFactory
import ge.ngvalia.messengerapp.userdiscovery.network.UserApi
import ge.ngvalia.messengerapp.ui.chat.ChatActivity
import ge.ngvalia.messengerapp.data.model.User
import ge.ngvalia.messengerapp.ui.auth.RegisterActivity

class UserDiscoveryFragment : Fragment() {

    private var _binding: FragmentUserDiscoveryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserDiscoveryViewModel by viewModels {
        UserDiscoveryViewModelFactory(SearchUserRepository(UserApi()))
    }
    private lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDiscoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: ImageButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        adapter = UserListAdapter { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("OTHER_USER_ID", user.uid)
                putExtra("OTHER_USER_NAME", user.nickname)
            }
            startActivity(intent)
        }

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
                    query.length >= 3 -> viewModel.searchUsers(query)
                    query.isEmpty() -> viewModel.clearSearch()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        observeViewModel()

        binding.retryButton.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            binding.searchProgressBar.visibility = if (isSearching) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                if (binding.searchEditText.text.isNullOrEmpty()) {
                    binding.mainProgressBar.visibility = View.VISIBLE
                    binding.searchProgressBar.visibility = View.GONE
                } else {
                    binding.searchProgressBar.visibility = View.VISIBLE
                    binding.mainProgressBar.visibility = View.GONE
                }
                binding.errorStateLayout.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
            } else {
                binding.mainProgressBar.visibility = View.GONE
                binding.searchProgressBar.visibility = View.GONE
            }
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)

            if (users.isEmpty() && viewModel.isLoading.value != true) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.errorStateLayout.visibility = View.GONE

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}