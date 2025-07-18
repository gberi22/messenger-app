package ge.ngvalia.messengerapp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessengerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        configureRealtimeDatabase()
    }

    private fun configureRealtimeDatabase() {
        val database = FirebaseDatabase.getInstance()

        database.setPersistenceEnabled(true)

        database.goOnline()

        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("Firebase", "Connected to Realtime Database")
                } else {
                    Log.d("Firebase", "Disconnected from Realtime Database")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Connection listener cancelled", error.toException())
            }
        })
    }
}