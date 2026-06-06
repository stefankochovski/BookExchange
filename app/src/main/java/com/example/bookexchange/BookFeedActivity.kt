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
import com.google.firebase.firestore.FirebaseFirestore

class BookFeedActivity : AppCompatActivity() {

    private lateinit var rvBooksFeed: RecyclerView
    private lateinit var fabAddBook: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var spinnerFilterCriterion: Spinner
    private lateinit var bottomNavigation: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()
    private val bookList = mutableListOf<Book>()
    private val filteredList = mutableListOf<Book>()
    private lateinit var adapter: BookAdapter

    private var searchCriterion: String = "Наслов"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_feed)

        rvBooksFeed = findViewById(R.id.rvBooksFeed)
        fabAddBook = findViewById(R.id.fabAddBook)
        etSearch = findViewById(R.id.etSearch)
        spinnerFilterCriterion = findViewById(R.id.spinnerFilterCriterion)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        val criteria = arrayOf("Наслов", "Автор", "Состојба", "Град")
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
                etSearch.hint = "Пребарај по $searchCriterion..."
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
                    Toast.makeText(this, "Веќе сте во Каталогот", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_favorites -> {
                    Toast.makeText(this, "Омилени (Наскоро: логика за приказ)", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Профил (Наскоро: кориснички профил)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        fetchBooksFromFirebase()
    }

    private fun fetchBooksFromFirebase() {
        db.collection("books")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Грешка при вчитување: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                bookList.clear()
                if (value != null) {
                    for (doc in value.documents) {
                        val book = doc.toObject(Book::class.java)
                        if (book != null) {
                            // Автоматски го доделуваме ID-то од документот на книгата
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
                    "Наслов" -> book.title.lowercase().contains(lowerCaseQuery)
                    "Автор" -> book.author.lowercase().contains(lowerCaseQuery)
                    "Состојба" -> book.condition.lowercase().contains(lowerCaseQuery)
                    "Град" -> book.city.lowercase().contains(lowerCaseQuery)
                    else -> false
                }

                if (matches) {
                    filteredList.add(book)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    inner class BookAdapter(private val books: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

        inner class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
            val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
            var tvCondition: TextView = view.findViewById(R.id.tvBookCondition)
            val ivImage: ImageView = view.findViewById(R.id.ivBookImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
            return BookViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = books[position]
            holder.tvTitle.text = book.title
            holder.tvAuthor.text = book.author
            holder.tvCondition.text = "Состојба: ${book.condition}"
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, BookDetailActivity::class.java)

                intent.putExtra("BOOK_TITLE", book.title)
                intent.putExtra("BOOK_AUTHOR", book.author)
                intent.putExtra("BOOK_PUBLISHER", book.publisher)
                intent.putExtra("BOOK_CONDITION", book.condition)
                intent.putExtra("BOOK_CITY", book.city)
                intent.putExtra("BOOK_CONTACT", book.contact)

                context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int = books.size
    }
}