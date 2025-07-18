package ge.ngvalia.messengerapp.ui.homepage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import ge.ngvalia.messengerapp.R
import ge.ngvalia.messengerapp.ui.auth.LoginActivity
import ge.ngvalia.messengerapp.ui.chat.ChatUtils
import ge.ngvalia.messengerapp.userdiscovery.network.FirebaseMigrationHelper
import ge.ngvalia.messengerapp.userdiscovery.ui.UserDiscoveryFragment
import kotlinx.coroutines.launch
import ge.ngvalia.messengerapp.ui.profile.ProfileActivity

class MainPageActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewModel: MainPageViewModel
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var searchEditText: EditText
    private lateinit var conversationsRecyclerView: RecyclerView
    private lateinit var loadingContainer: FrameLayout
    private lateinit var emptyStateContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.main_page)

        initializeViews()
        setupViewModel()
        setupBottomNavigation()
        setupRecyclerView()
        setupSearchFunctionality()
        setupObservers()

        val fab: FloatingActionButton = findViewById(R.id.fab_add)
        runMigration()

        fab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserDiscoveryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initializeViews() {
        conversationsRecyclerView = findViewById(R.id.conversations_recycler_view)
        searchEditText = findViewById(R.id.search_edit_text)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        loadingContainer = findViewById(R.id.loading_container)
        emptyStateContainer = findViewById(R.id.empty_state_container)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainPageViewModel::class.java]
    }

    private fun runMigration() {
        lifecycleScope.launch {
            val migrationHelper = FirebaseMigrationHelper()
            migrationHelper.addNicknameLowerField()
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_home
        viewModel.refresh()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.nav_home
    }

    private fun setupRecyclerView() {
        conversationsRecyclerView.layoutManager = LinearLayoutManager(this)

        conversationAdapter = ConversationAdapter { conversation ->
            ChatUtils.startChatActivity(
                this,
                conversation.otherUserId,
                conversation.otherUserName,
                conversation.otherUserAvatar
            )
        }

        conversationsRecyclerView.adapter = conversationAdapter
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                viewModel.search(query)
            }
        })
    }

    private fun setupObservers() {
        viewModel.conversations.observe(this) { conversations ->
            if (viewModel.isSearchMode.value != true) {
                conversationAdapter.submitList(conversations)
                updateUI(conversations)
            }
        }

        viewModel.filteredConversations.observe(this) { filteredConversations ->
            if (viewModel.isSearchMode.value == true) {
                conversationAdapter.submitList(filteredConversations)
                updateUI(filteredConversations)
            }
        }

        viewModel.isSearchMode.observe(this) { isSearchMode ->
            if (isSearchMode) {
                showSearchResults()
            } else {
                showConversations()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            updateLoadingState(isLoading)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
                updateLoadingState(false)
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            loadingContainer.visibility = View.VISIBLE
            conversationsRecyclerView.visibility = View.GONE
            emptyStateContainer.visibility = View.GONE
        } else {
            loadingContainer.visibility = View.GONE
        }
    }

    private fun updateUI(conversations: List<Conversation>) {
        if (viewModel.isLoading.value == true) {
            return
        }

        if (conversations.isEmpty()) {
            conversationsRecyclerView.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
        } else {
            conversationsRecyclerView.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
        }
    }

    private fun showSearchResults() {
    }

    private fun showConversations() {
        if (conversationsRecyclerView.adapter != conversationAdapter) {
            conversationsRecyclerView.adapter = conversationAdapter
            viewModel.conversations.value?.let { conversations ->
                conversationAdapter.submitList(conversations)
                updateUI(conversations)
            }
        }
    }

    override fun onBackPressed() {
        if (viewModel.isSearchMode.value == true) {
            searchEditText.text?.clear()
            viewModel.clearSearch()
        } else {
            super.onBackPressed()
        }
    }
}