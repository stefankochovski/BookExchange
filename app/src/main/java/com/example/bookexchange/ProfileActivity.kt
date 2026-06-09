package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Дефинирање на RecyclerView и листата
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter // Потребно е да имаш ваков адаптер

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Кориснички податоци
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        tvEmail.text = auth.currentUser?.email ?: getString(R.string.not_logged)

        // 2. Иницијализација на RecyclerView
        recyclerView = findViewById(R.id.rvMyBooks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadMyBooks() // Повикуваме функција да ги вчита книгите

        // 3. Копче за одјавување
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        // 4. Навигација (иста како твојата)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_profile
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> { startActivity(Intent(this, BookFeedActivity::class.java)); finish(); true }
                R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); finish(); true }
                R.id.nav_notifications -> { startActivity(Intent(this, NotificationsActivity::class.java)); finish(); true }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadMyBooks() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("books")
            .whereEqualTo("ownerId", currentUserId) // Филтрирање според ownerId
            .get()
            .addOnSuccessListener { documents ->
                val bookList = mutableListOf<Book>() // Мора да имаш дефинирано Data Class 'Book'
                for (doc in documents) {
                    val book = doc.toObject(Book::class.java)
                    bookList.add(book)
                }
                bookAdapter = BookAdapter(bookList)
                recyclerView.adapter = bookAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    getString(R.string.error_loading, e.message), Toast.LENGTH_SHORT).show()
            }
    }
}