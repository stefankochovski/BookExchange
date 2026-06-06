package com.example.bookexchange

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        // Стрелка за назад
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Детали за книгата"

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

        // ПРЕЗЕМАЊЕ НА ПОДАТОЦИТЕ пратени од претходниот екран (Каталогот)
        val title = intent.getStringExtra("BOOK_TITLE") ?: "Непознат наслов"
        val author = intent.getStringExtra("BOOK_AUTHOR") ?: "Непознат автор"
        val publisher = intent.getStringExtra("BOOK_PUBLISHER") ?: "/"
        val condition = intent.getStringExtra("BOOK_CONDITION") ?: "Нова"
        val city = intent.getStringExtra("BOOK_CITY") ?: "/"
        val contact = intent.getStringExtra("BOOK_CONTACT") ?: "/"
        // Стави го ова некаде при крајот на onCreate функцијата во BookDetailActivity.kt
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetail)
        toolbar.setNavigationOnClickListener {
            finish() // Го затвора екранот и се враќа назад на клик на стрелката
        }

        // Поставување на податоците во текстуалните полиња
        tvTitle.text = title
        tvAuthor.text = "Автор: $author"
        tvPublisher.text = "Издавач: $publisher"
        tvCondition.text = "Состојба: $condition"
        tvCity.text = "Град: $city"
        tvContact.text = "Контакт: $contact"

        // Забелешка за сликата: Ако користиш Firebase Storage за слики, овде се користи библиотека како Glide.
        // За почеток, ја оставаме стандардната икона додека не го поврзеш делот со слики.

        // Копче 1: Зачувај во омилени
        btnAddToFavorites.setOnClickListener {
            // Овде понатаму ќе додадеме логика за локална база (Room) или Firestore "favorites"
            Toast.makeText(this, "Книгата е додадена во твоите Омилени!", Toast.LENGTH_SHORT).show()
        }

        // Копче 2: Испрати понуда
        btnSendOffer.setOnClickListener {
            // Овде понатаму може да отвориме чат или форма за порака
            Toast.makeText(this, "Понудата за размена е успешно испратена до $contact!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // Го враќа корисникот назад во каталогот
        return true
    }
}