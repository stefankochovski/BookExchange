package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val offers = mutableListOf<TradeOffer>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var rvNotifications: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        // ✅ ПОПРАВЕНО: findViewById се повикува откако setContentView е веќе извршен
        val mainView = findViewById<android.view.View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        rvNotifications = findViewById(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_notifications

        // Навигација
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    startActivity(Intent(this, BookFeedActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notifications -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Иницијализација на адаптерот
        adapter = NotificationAdapter(offers)
        rvNotifications.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("trades")
            .whereEqualTo("receiverId", currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                offers.clear()
                if (snapshot != null) {
                    for (doc in snapshot) {
                        val offer = doc.toObject(TradeOffer::class.java).apply { id = doc.id }
                        offers.add(offer)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun accept(offer: TradeOffer) {
        db.collection("trades").document(offer.id).update("status", "accepted")
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.accepted_offer), Toast.LENGTH_SHORT).show()
            }
    }

    private fun reject(offer: TradeOffer) {
        db.collection("trades").document(offer.id).update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.denied_offer), Toast.LENGTH_SHORT).show()
            }
    }
}