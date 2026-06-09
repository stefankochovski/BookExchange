package com.example.bookexchange

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    // Дефинирање на сите полиња од дизајнот
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etPublisher: EditText
    private lateinit var spinnerCondition: Spinner
    private lateinit var ivPreview: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnSave: Button
    private lateinit var etBookCity: EditText
    private lateinit var etBookContact: EditText
    private lateinit var btnCancel: Button

    // ✅ ПОПРАВЕНО: празен string наместо getString() кој не може да се повика овде
    private var selectedCondition: String = ""
    private val db = FirebaseFirestore.getInstance() // Поврзување со Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // ✅ ПОПРАВЕНО: getString() се повикува овде каде Context веќе постои
        selectedCondition = getString(R.string.nn)

        // Овозможи ја стрелката за назад во горната лента
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.new_book_add)

        // Иницијализација на компонентите со точните XML ID-а
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etPublisher = findViewById(R.id.etBookPublisher)
        spinnerCondition = findViewById(R.id.spinnerCondition)
        ivPreview = findViewById(R.id.ivBookPreview)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnSave = findViewById(R.id.btnSaveBook)

        // ТОЧКА 1: Овде го поврзуваме новото копче за откажување со XML дизајнот
        btnCancel = findViewById(R.id.btnCancel)

        // Врзување на новите две полиња кои ги додадовме во XML
        etBookCity = findViewById(R.id.etBookCity)
        etBookContact = findViewById(R.id.etBookContact)

        // 1. Полнење на паѓачкото мени (Spinner) со состојби
        val conditions = arrayOf(getString(R.string.n), getString(R.string.s), getString(R.string.o))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, conditions)
        spinnerCondition.adapter = adapter

        spinnerCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCondition = conditions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 2. Логика за Камера (Сликање директно)
        val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    ivPreview.setImageBitmap(bitmap)
                    ivPreview.visibility = View.VISIBLE
                }
            }
        }

        btnCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }

        // 3. Логика за Галерија (Избор на постоечка слика)
        val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    ivPreview.setImageURI(imageUri)
                    ivPreview.visibility = View.VISIBLE
                }
            }
        }

        btnGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        // 4. Логика за Зачувување во База на Податоци (Firestore)
        btnSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val author = etAuthor.text.toString().trim()
            val publisher = etPublisher.text.toString().trim()
            val city = etBookCity.text.toString().trim()
            val contact = etBookContact.text.toString().trim()

            // ПРОВЕРКА 1: Дали сите текстуални полиња се пополнети
            if (title.isEmpty() || author.isEmpty() || publisher.isEmpty() || city.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, getString(R.string.fields_fill), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ПРОВЕРКА 2: Дали е додадена слика
            if (ivPreview.visibility != View.VISIBLE) {
                Toast.makeText(this, getString(R.string.pls_add_pic), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ако поминаа двете проверки, тогаш јавуваме дека се зачувува
            Toast.makeText(this, getString(R.string.trying_save), Toast.LENGTH_SHORT).show()

            // Креирање на објект (мапа) со податоците за книгата
            val bookData = hashMapOf(
                "title" to title,
                "author" to author,
                "publisher" to publisher,
                "condition" to selectedCondition,
                "ownerId" to FirebaseAuth.getInstance().currentUser?.uid,
                "city" to city,
                "contact" to contact,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            // Испраќање во Firebase во колекција со име "books"
            db.collection("books")
                .add(bookData)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.bood_added), Toast.LENGTH_LONG).show()

                    // Чистење на формата по успешно зачувување
                    etTitle.text.clear()
                    etAuthor.text.clear()
                    etPublisher.text.clear()
                    etBookCity.text.clear()
                    etBookContact.text.clear()
                    ivPreview.setImageBitmap(null)
                    ivPreview.visibility = View.GONE // Ја криеме сликата за следното внесување
                }
                .addOnFailureListener { ex ->
                    Toast.makeText(this,
                        getString(R.string.falinka, ex.localizedMessage), Toast.LENGTH_LONG).show()
                }
        }

        // ТОЧКА 2: Логика кога ќе се кликне копчето за откажување
        btnCancel.setOnClickListener {
            finish() // Ја затвора оваа активност и го враќа корисникот назад во каталогот
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}