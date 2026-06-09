package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup) // ← вистинскиот layout

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPhone = findViewById<EditText>(R.id.etRegisterPhone)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etPasswordConfirm = findViewById<EditText>(R.id.etRegisterPasswordConfirm)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<Button>(R.id.btnBackToLogin)

        btnBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            // Автоматски додај + ако го нема
            val phone = etPhone.text.toString().trim().let {
                if (it.startsWith("+")) it else "+$it"
            }
            val password = etPassword.text.toString().trim()
            val passwordConfirm = etPasswordConfirm.text.toString().trim()

            // Валидација
            if (email.isEmpty() || phone.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

// ✅ Валидација на телефонски број
            val phoneRegex = Regex("^\\+3897[0-9]{7}$")
            if (!phoneRegex.matches(phone)) {
                Toast.makeText(this, getString(R.string.phone_format), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, getString(R.string.password_nomatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, getString(R.string.minSign_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Регистрација
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Зачувај email и телефон во Firestore
                        db.collection("users").document(userId)
                            .set(mapOf(
                                "email" to email,
                                "phone" to phone
                            ))
                            .addOnSuccessListener {
                                Toast.makeText(this,
                                    getString(R.string.register_sucess), Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, BookFeedActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,
                                    getString(R.string.error_save_data), Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this,
                            getString(R.string.error, task.exception?.message), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}