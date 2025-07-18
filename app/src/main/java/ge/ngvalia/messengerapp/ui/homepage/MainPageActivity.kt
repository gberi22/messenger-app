package ge.ngvalia.messengerapp.ui.homepage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
import ge.ngvalia.messengerapp.ui.chat.ChatActivity
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
        runMigration() // delete this later

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
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainPageViewModel::class.java]
    }

    private fun runMigration() {
        lifecycleScope.launch {
            try {
                val migrationHelper = FirebaseMigrationHelper()
                migrationHelper.addNicknameLowerField()
            } catch (e: Exception) {
                // Handle migration error silently
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset to home when returning to MainActivity
        bottomNavigationView.selectedItemId = R.id.nav_home
        // Refresh conversations
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

        // Conversation adapter
        conversationAdapter = ConversationAdapter { conversation ->
            // Navigate to chat
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
                val query = s?.toString()?.trim() ?: ""
                viewModel.search(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        // Observe conversations
        viewModel.conversations.observe(this) { conversations ->
            if (viewModel.isSearchMode.value != true) {
                conversationAdapter.submitList(conversations)
            }
        }

        // Observe filtered conversations (during search)
        viewModel.filteredConversations.observe(this) { filteredConversations ->
            if (viewModel.isSearchMode.value == true) {
                conversationAdapter.submitList(filteredConversations)
            }
        }

        // Observe search mode
        viewModel.isSearchMode.observe(this) { isSearchMode ->
            if (isSearchMode) {
                // Show search results
                showSearchResults()
            } else {
                // Show conversations
                showConversations()
            }
        }


        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            // You can show/hide loading indicator here
            // For now, we'll just log it
            if (isLoading) {
                // Show loading indicator
            } else {
                // Hide loading indicator
            }
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showSearchResults() {
        // The adapter switching is handled in the search results observer
        // This method can be used for additional UI changes during search
    }

    private fun showConversations() {
        // Make sure we're showing the conversation adapter
        if (conversationsRecyclerView.adapter != conversationAdapter) {
            conversationsRecyclerView.adapter = conversationAdapter
            // Resubmit the current list
            viewModel.conversations.value?.let { conversations ->
                conversationAdapter.submitList(conversations)
            }
        }
    }

    override fun onBackPressed() {
        // Clear search if in search mode
        if (viewModel.isSearchMode.value == true) {
            searchEditText.text?.clear()
            viewModel.clearSearch()
        } else {
            super.onBackPressed()
        }
    }
}