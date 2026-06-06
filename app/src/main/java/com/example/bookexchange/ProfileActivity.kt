package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var rvMyBooks: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvEmail = findViewById<TextView>(R.id.tvUserEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        rvMyBooks = findViewById(R.id.rvMyBooks)

        tvEmail.text = "Најавени сте како: ${auth.currentUser?.email}"

        rvMyBooks.layoutManager = LinearLayoutManager(this)

        loadMyBooks()

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java) // Твоето име за екранот за најава
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadMyBooks() {
        val userId = auth.currentUser?.uid ?: return

        // Потсети се: Ова бара полето 'ownerId' да постои во секој документ во 'books'
        db.collection("books").whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val myBooksList = documents.toObjects(Book::class.java)
                rvMyBooks.adapter = BookAdapter(myBooksList)
            }
    }
}