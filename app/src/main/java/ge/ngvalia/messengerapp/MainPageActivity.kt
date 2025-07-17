package ge.ngvalia.messengerapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import ge.ngvalia.messengerapp.ui.auth.LoginActivity
import ge.ngvalia.messengerapp.ui.chat.Conversation
import ge.ngvalia.messengerapp.ui.chat.ConversationAdapter
import ge.ngvalia.messengerapp.userdiscovery.data.repository.UserRepository
import ge.ngvalia.messengerapp.userdiscovery.network.FirebaseMigrationHelper
import ge.ngvalia.messengerapp.userdiscovery.network.UserApi
import ge.ngvalia.messengerapp.userdiscovery.ui.UserDiscoveryFragment
import ge.ngvalia.messengerapp.userdiscovery.viewmodel.UserDiscoveryViewModel
import ge.ngvalia.messengerapp.userdiscovery.viewmodel.UserDiscoveryViewModelFactory
import kotlinx.coroutines.launch
import ge.ngvalia.messengerapp.ui.profile.ProfileActivity

class MainPageActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.main_page)
        setupBottomNavigation()
        setupRecyclerView()
        val fab: FloatingActionButton = findViewById(R.id.fab_add)

        runMigration() // delete

        fab.setOnClickListener {
            // Replace R.id.fragment_container with your actual container ID
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UserDiscoveryFragment())
                .addToBackStack(null)
                .commit()
        }

    }

    private fun runMigration() {
        lifecycleScope.launch {
            try {
                val migrationHelper = FirebaseMigrationHelper()
                migrationHelper.addNicknameLowerField()
            } catch (e: Exception) {
            } finally {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset to home when returning to MainActivity
        bottomNavigationView.selectedItemId = R.id.nav_home
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)

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
        val recyclerView = findViewById<RecyclerView>(R.id.conversations_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create placeholder data
        val placeholderConversations = createPlaceholderData()

        // Set adapter
        recyclerView.adapter = ConversationAdapter(placeholderConversations)
    }

    private fun createPlaceholderData(): List<Conversation> {
        return listOf(
            Conversation("Sayed Eftiaz", "On my way home but i needed to stop by the book store to...", "5 min"),
            Conversation("Sanjida Akter", "On my way home but i needed to stop by the book store to...", "15 min"),
            Conversation("Tour de Bhutan", "On my way home but i needed to stop by the book store to...", "5 min"),
            Conversation("John Doe", "Hey, how are you doing today?", "1 hour"),
            Conversation("Jane Smith", "Can we meet tomorrow for lunch?", "2 hours"),
            Conversation("Mike Johnson", "Thanks for the help with the project!", "3 hours"),
            Conversation("Sarah Wilson", "Did you see the latest news?", "1 day"),
            Conversation("David Brown", "Happy birthday! 🎉", "2 days"),
            Conversation("Lisa Davis", "The meeting has been rescheduled", "3 days"),
            Conversation("Tom Anderson", "Great job on the presentation!", "1 week"),
            Conversation("Emma Taylor", "Let's catch up soon", "2 weeks"),
            Conversation("Alex Miller", "Thanks for the recommendation", "1 month"),
            Conversation("Chris Wilson", "See you at the conference", "2 months"),
            Conversation("Amy Johnson", "Hope you're doing well", "3 months"),
            Conversation("Ryan Davis", "Looking forward to working together", "6 months")
        )
    }
}
