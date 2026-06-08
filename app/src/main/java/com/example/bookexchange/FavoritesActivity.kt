package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.bookexchange.BookAdapter
class FavoritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: BookAdapter // Користиме ист адаптер
    private val favoriteList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rvFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Поставуваме адаптер со празна листа во почеток
        adapter = BookAdapter(favoriteList, isFavoritesMode = true)
        recyclerView.adapter = adapter

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_favorites

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    startActivity(Intent(this, BookFeedActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_favorites -> true // Веќе сме тука
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Повикуваме функција да ги земе омилените
        loadFavoriteBooks()
    }

    private fun loadFavoriteBooks() {
        // Бараме книги каде isFavorite е true
        db.collection("books")
            .whereEqualTo("isFavorite", true)
            .get()
            .addOnSuccessListener { documents ->
                favoriteList.clear()
                for (doc in documents) {
                    val book = doc.toObject(Book::class.java)
                    book.id = doc.id
                    favoriteList.add(book)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Грешка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}