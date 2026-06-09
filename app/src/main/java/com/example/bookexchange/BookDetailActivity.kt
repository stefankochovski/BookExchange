package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class BookDetailActivity : AppCompatActivity() {

    private lateinit var ivBookImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvContact: TextView
    private lateinit var btnAddToFavorites: Button
    private lateinit var btnSendOffer: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        // Иницијализација на компонентите
        ivBookImage = findViewById(R.id.ivDetailBookImage)
        tvTitle = findViewById(R.id.tvDetailTitle)
        tvAuthor = findViewById(R.id.tvDetailAuthor)
        tvPublisher = findViewById(R.id.tvDetailPublisher)
        tvCondition = findViewById(R.id.tvDetailCondition)
        tvCity = findViewById(R.id.tvDetailCity)
        tvContact = findViewById(R.id.tvDetailContact)
        btnAddToFavorites = findViewById(R.id.btnAddToFavorites)
        btnSendOffer = findViewById(R.id.btnSendOffer)

        // ПРЕЗЕМАЊЕ НА ПОДАТОЦИТЕ пратени од претходниот екран
        val bookId = intent.getStringExtra("BOOK_ID")
        val ownerId = intent.getStringExtra("OWNER_ID") // Ова мора да се прати од BookFeedActivity!
        val title = intent.getStringExtra("BOOK_TITLE") ?: getString(R.string.unknown_t)
        val author = intent.getStringExtra("BOOK_AUTHOR") ?: getString(R.string.unknown_a)
        val publisher = intent.getStringExtra("BOOK_PUBLISHER") ?: "/"
        val condition = intent.getStringExtra("BOOK_CONDITION") ?: getString(R.string.nnn)
        val city = intent.getStringExtra("BOOK_CITY") ?: "/"
        val contact = intent.getStringExtra("BOOK_CONTACT") ?: "/"

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Поставување на податоците
        tvTitle.text = title
        tvAuthor.text = getString(R.string.athor, author)
        tvPublisher.text = getString(R.string.pblshr, publisher)
        tvCondition.text = getString(R.string.cndtn, condition)
        tvCity.text = getString(R.string.twn, city)
        tvContact.text = getString(R.string.cnct, contact)

        // Копче 1: Зачувај во омилени
        btnAddToFavorites.setOnClickListener {
            if (bookId != null) {
                db.collection("books").document(bookId)
                    .update("isFavorite", true)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.book_in_fav), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Грешка: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Копче 2: Испрати понуда (Отвора нов екран)
        btnSendOffer.setOnClickListener {
            val intent = Intent(this, OfferFormActivity::class.java)
            intent.putExtra("bookWantedId", bookId)
            intent.putExtra("receiverId", ownerId)
            startActivity(intent)
        }
    }
}