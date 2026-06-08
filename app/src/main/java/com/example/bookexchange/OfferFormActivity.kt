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
    private var capturedBitmap: Bitmap? = null // За чување на слика од камера

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://bookexchange-54489.firebasestorage.app")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer_form)

        val bookWantedId = intent.getStringExtra("bookWantedId")
        val receiverId = intent.getStringExtra("receiverId")

        val ivBook = findViewById<ImageView>(R.id.ivOfferedBook)
        val btnSelect = findViewById<Button>(R.id.btnSelectImage)
        val btnCamera = findViewById<Button>(R.id.btnCamera) // Треба да имаш вакво копче во XML
        val btnSend = findViewById<Button>(R.id.btnSendOffer)
        val btnCancel = findViewById<Button>(R.id.btnCancelOffer)
        val etName = findViewById<EditText>(R.id.etOfferName)
        val etCity = findViewById<EditText>(R.id.etOfferCity)
        val etCondition = findViewById<EditText>(R.id.etOfferCondition)

        // Лаунчери
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
            capturedBitmap = null // Ресетирај камера
            ivBook.setImageURI(uri)
        }

        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                selectedImageUri = null // Ресетирај галерија
                ivBook.setImageBitmap(bitmap)
            }
        }

        btnSelect.setOnClickListener { pickImage.launch("image/*") }
        btnCamera.setOnClickListener { takePicture.launch() }
        btnCancel.setOnClickListener { finish() }

        btnSend.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, "Не си логиран!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageUri == null && capturedBitmap == null) {
                Toast.makeText(this, "Ве молиме одберете слика", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSend.isEnabled = false
            btnSend.text = "Се испраќа..."

            // Конверзија на слика во бајтови
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
                    val offer = hashMapOf(
                        "bookWantedId" to bookWantedId,
                        "senderId" to auth.currentUser?.uid,
                        "receiverId" to receiverId,
                        "status" to "pending",
                        "offeredBookData" to hashMapOf(
                            "imageUrl" to uri.toString(),
                            "name" to etName.text.toString(),
                            "city" to etCity.text.toString(),
                            "condition" to etCondition.text.toString()
                        ),
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("trades").add(offer)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Успешно!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            btnSend.isEnabled = true
                            btnSend.text = "Испрати"
                            Toast.makeText(this, "Грешка во база: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnSend.isEnabled = true
                    btnSend.text = "Испрати"
                    android.util.Log.e("UPLOAD_ERROR", "Порака: ${e.message}", e)
                    android.util.Log.e("UPLOAD_ERROR", "Причина: ${e.cause?.message}")
                    Toast.makeText(this, "Грешка: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}