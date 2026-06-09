package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth // Додадено за автентикација
import com.google.firebase.firestore.FirebaseFirestore

class BookFeedActivity : AppCompatActivity() {

    private lateinit var rvBooksFeed: RecyclerView
    private lateinit var fabAddBook: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var spinnerFilterCriterion: Spinner
    private lateinit var bottomNavigation: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Иницијализација на Auth
    private val bookList = mutableListOf<Book>()
    private val filteredList = mutableListOf<Book>()
    private lateinit var adapter: BookAdapter

    private var searchCriterion: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_feed)
        searchCriterion = getString(R.string.tlt)

        rvBooksFeed = findViewById(R.id.rvBooksFeed)
        fabAddBook = findViewById(R.id.fabAddBook)
        etSearch = findViewById(R.id.etSearch)
        spinnerFilterCriterion = findViewById(R.id.spinnerFilterCriterion)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        val criteria = arrayOf(getString(R.string.ttl), getString(R.string.athr),
            getString(R.string.cndtion), getString(R.string.cty))
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, criteria)
        spinnerFilterCriterion.adapter = spinnerAdapter

        rvBooksFeed.layoutManager = LinearLayoutManager(this)
        adapter = BookAdapter(filteredList)
        rvBooksFeed.adapter = adapter

        fabAddBook.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        spinnerFilterCriterion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                searchCriterion = criteria[position]
                etSearch.hint = getString(R.string.search_on, searchCriterion)
                filterBooks(etSearch.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBooks(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_catalog -> {
                    Toast.makeText(this, getString(R.string.cathalouge), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        fetchBooksFromFirebase()
    }

    private fun fetchBooksFromFirebase() {
        val currentUserId = auth.currentUser?.uid

        // Филтрираме во самата заявка кон Firestore
        db.collection("books")
            .whereNotEqualTo("ownerId", currentUserId) // Прикажи само туѓи книги
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this,
                        getString(R.string.loading_error, error.message), Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                bookList.clear()
                if (value != null) {
                    for (doc in value.documents) {
                        val book = doc.toObject(Book::class.java)
                        if (book != null) {
                            book.id = doc.id
                            bookList.add(book)
                        }
                    }
                }

                filteredList.clear()
                filteredList.addAll(bookList)
                adapter.notifyDataSetChanged()
            }
    }

    private fun filterBooks(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(bookList)
        } else {
            val lowerCaseQuery = query.lowercase().trim()
            for (book in bookList) {
                val matches = when (searchCriterion) {
                    getString(R.string.titl) -> book.title.lowercase().contains(lowerCaseQuery)
                    getString(R.string.authr) -> book.author.lowercase().contains(lowerCaseQuery)
                    getString(R.string.condtn) -> book.condition.lowercase().contains(lowerCaseQuery)
                    getString(R.string.cityy) -> book.city.lowercase().contains(lowerCaseQuery)
                    else -> false
                }

                if (matches) {
                    filteredList.add(book)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}