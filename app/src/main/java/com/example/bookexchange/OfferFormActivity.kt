package com.example.bookexchange

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class OfferFormActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private var capturedBitmap: Bitmap? = null

    // Глобални променливи за да бидат достапни во сите функции
    private lateinit var btnSend: Button
    private var bookWantedId: String? = null
    private var receiverId: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://bookexchange-54489.firebasestorage.app")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer_form)

        // Преземање на ID-ата веднаш при стартување
        bookWantedId = intent.getStringExtra("bookWantedId")
        receiverId = intent.getStringExtra("receiverId")

        // Дебаг логирање (провери го Logcat со филтер "OFFER_DEBUG")
        android.util.Log.d("OFFER_DEBUG", "Примен receiverId: $receiverId")

        val ivBook = findViewById<ImageView>(R.id.ivOfferedBook)
        val btnSelect = findViewById<Button>(R.id.btnSelectImage)
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        btnSend = findViewById(R.id.btnSendOffer)
        val btnCancel = findViewById<Button>(R.id.btnCancelOffer)
        val etName = findViewById<EditText>(R.id.etOfferName)
        val etCity = findViewById<EditText>(R.id.etOfferCity)
        val etCondition = findViewById<EditText>(R.id.etOfferCondition)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
            capturedBitmap = null
            ivBook.setImageURI(uri)
        }

        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                selectedImageUri = null
                ivBook.setImageBitmap(bitmap)
            }
        }

        btnSelect.setOnClickListener { pickImage.launch("image/*") }
        btnCamera.setOnClickListener { takePicture.launch() }
        btnCancel.setOnClickListener { finish() }

        btnSend.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Дополнителна проверка за сигурност
            if (receiverId.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.reciever_not_found), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            btnSend.text = getString(R.string.sending)

            if (selectedImageUri == null && capturedBitmap == null) {
                saveOfferToDatabase(null)
            } else {
                val baos = ByteArrayOutputStream()
                if (capturedBitmap != null) {
                    capturedBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                } else {
                    val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                    inputStream?.use { it.copyTo(baos) }
                }
                val bytes = baos.toByteArray()

                val fileName = UUID.randomUUID().toString()
                val ref = storage.reference.child("offer_images/$fileName")

                ref.putBytes(bytes)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) task.exception?.let { throw it }
                        ref.downloadUrl
                    }
                    .addOnSuccessListener { uri ->
                        saveOfferToDatabase(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        btnSend.isEnabled = true
                        btnSend.text = getString(R.string.send)
                        Toast.makeText(this,
                            getString(R.string.error_attaching, e.message), Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun saveOfferToDatabase(imageUrl: String?) {
        val etName = findViewById<EditText>(R.id.etOfferName)
        val etCity = findViewById<EditText>(R.id.etOfferCity)
        val etCondition = findViewById<EditText>(R.id.etOfferCondition)

        val offer = hashMapOf(
            "bookWantedId" to bookWantedId,
            "senderId" to auth.currentUser?.uid,
            "receiverId" to receiverId, // Сега користиме глобална променлива
            "status" to "pending",
            "offeredBookData" to hashMapOf(
                "imageUrl" to (imageUrl ?: "NO_IMAGE"),
                "name" to etName.text.toString(),
                "city" to etCity.text.toString(),
                "condition" to etCondition.text.toString()
            ),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("trades").add(offer)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.offer_send_success), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnSend.isEnabled = true
                btnSend.text = getString(R.string.sd)
                Toast.makeText(this, getString(R.string.error_base, e.message), Toast.LENGTH_LONG).show()
            }
    }
}