package ge.ngvalia.messengerapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_page)
        setupRecyclerView()
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
