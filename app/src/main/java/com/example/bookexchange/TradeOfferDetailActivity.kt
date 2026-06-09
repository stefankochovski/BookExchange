package com.example.bookexchange

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.content.Intent

class TradeOfferDetailActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_offer_detail)

        val offerId = intent.getStringExtra("OFFER_ID")
        val bookWantedId = intent.getStringExtra("BOOK_WANTED_ID")
        val offeredName = intent.getStringExtra("OFFERED_BOOK_NAME")
        val offeredCity = intent.getStringExtra("OFFERED_BOOK_CITY")
        val offeredCondition = intent.getStringExtra("OFFERED_BOOK_CONDITION")
        val offeredImage = intent.getStringExtra("OFFERED_BOOK_IMAGE")

        val tvWantedBook = findViewById<TextView>(R.id.tvWantedBookTitle)
        val tvOfferedName = findViewById<TextView>(R.id.tvOfferedBookName)
        val tvOfferedCity = findViewById<TextView>(R.id.tvOfferedBookCity)
        val tvOfferedCondition = findViewById<TextView>(R.id.tvOfferedBookCondition)
        val ivOfferedBook = findViewById<ImageView>(R.id.ivOfferedBookDetail)
        val btnAccept = findViewById<Button>(R.id.btnAcceptDetail)
        val btnReject = findViewById<Button>(R.id.btnRejectDetail)

        if (bookWantedId != null) {
            db.collection("books").document(bookWantedId)
                .get()
                .addOnSuccessListener { doc ->
                    tvWantedBook.text = getString(R.string.offer_for, doc.getString("title") ?: "/")
                }
        }

        tvOfferedName.text = getString(R.string.book, offeredName)
        tvOfferedCity.text = "Град: $offeredCity"
        tvOfferedCondition.text = "Состојба: $offeredCondition"

        if (!offeredImage.isNullOrEmpty() && offeredImage != "NO_IMAGE") {
            Glide.with(this).load(offeredImage).into(ivOfferedBook)
        } else {
            ivOfferedBook.visibility = android.view.View.GONE
        }

        btnAccept.setOnClickListener {
            offerId?.let { id ->
                db.collection("trades").document(id).update("status", "accepted")
                    .addOnSuccessListener {
                        sendResponseNotification("accepted")
                        Toast.makeText(this, "Размената е прифатена!", Toast.LENGTH_SHORT).show()

                        // Земи го телефонот на испраќачот од Firestore
                        val senderId = intent.getStringExtra("SENDER_ID")
                        if (senderId != null) {
                            db.collection("users").document(senderId)
                                .get()
                                .addOnSuccessListener { doc ->
                                    val phone = doc.getString("phone")
                                    if (!phone.isNullOrEmpty()) {
                                        openViber(phone)
                                    } else {
                                        Toast.makeText(this,
                                            getString(R.string.user_no_phone), Toast.LENGTH_SHORT).show()
                                    }
                                    finish()
                                }
                                .addOnFailureListener {
                                    finish()
                                }
                        } else {
                            finish()
                        }
                    }
            }
        }

        btnReject.setOnClickListener {
            offerId?.let {
                db.collection("trades").document(it).update("status", "rejected")
                    .addOnSuccessListener {
                        sendResponseNotification("rejected")
                        Toast.makeText(this, getString(R.string.denyied_offer), Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    } // ← onCreate завршува овде

    // ✅ Функцијата е НАДВОР од onCreate, внатре во класата
    private fun sendResponseNotification(status: String) {
        val senderId = intent.getStringExtra("SENDER_ID") ?: return

        // 1. Зачувај in-app нотификација (веќе го имаш ова)
        val notification = hashMapOf(
            "receiverId" to senderId,
            "bookWantedId" to (intent.getStringExtra("BOOK_WANTED_ID") ?: ""),
            "offeredBookData" to mapOf(
                "name" to (intent.getStringExtra("OFFERED_BOOK_NAME") ?: ""),
                "city" to (intent.getStringExtra("OFFERED_BOOK_CITY") ?: ""),
                "condition" to (intent.getStringExtra("OFFERED_BOOK_CONDITION") ?: ""),
                "imageUrl" to (intent.getStringExtra("OFFERED_BOOK_IMAGE") ?: "NO_IMAGE")
            ),
            "status" to status,
            "senderId" to "",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("trades").add(notification)

        // 2. Земи го FCM токенот на испраќачот и испрати push
        db.collection("users").document(senderId)
            .get()
            .addOnSuccessListener { doc ->
                val fcmToken = doc.getString("fcmToken") ?: return@addOnSuccessListener
                val title = if (status == "accepted") getString(R.string.offer_approved) else getString(
                    R.string.offer_denied
                )
                val body = getString(R.string.check_notifications)
                sendPushNotification(fcmToken, title, body)
            }
    }

    private fun sendPushNotification(token: String, title: String, body: String) {
        // Зачувај во fcm_queue — Cloud Function ќе го испрати
        val message = hashMapOf(
            "token" to token,
            "title" to title,
            "body" to body,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("fcm_queue").add(message)
    }
    private fun openViber(phone: String) {
        try {
            val cleanPhone = phone.removePrefix("+") // тргни го + знакот
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("viber://chat?number=$cleanPhone"))
            intent.setPackage("com.viber.voip")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.viber_notdownload), Toast.LENGTH_SHORT).show()
        }
    }
}