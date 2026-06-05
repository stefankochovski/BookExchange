package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Дефинирање на Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Иницијализација на Firebase
        auth = FirebaseAuth.getInstance()

        // Проверка: Ако корисникот е веќе најавен, директно оди на главниот екран
        if (auth.currentUser != null) {
            startDashboard()
        }

        // Поврзување со Елементите од XML интерфејсот
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLoginEmail = findViewById<Button>(R.id.btnLoginEmail)
        val btnRegisterEmail = findViewById<Button>(R.id.btnRegisterEmail)
        val btnAnonymous = findViewById<Button>(R.id.btnAnonymous)

        // 1. РЕГИСТРАЦИЈА СО Е-МЕЈЛ И ЛОЗИНКА
        btnRegisterEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                            startDashboard()
                        } else {
                            Toast.makeText(this, "${getString(R.string.login_failed)}: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        // 2. НАЈАВА СО ПОСТОЕЧКИ Е-МЕЈЛ И ЛОЗИНКА
        btnLoginEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                            startDashboard()
                        } else {
                            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // 3. АНОНИМНА НАЈАВА (КАКО ГОСТ)
        btnAnonymous.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Влеговте како Гост!", Toast.LENGTH_SHORT).show()
                        startDashboard()
                    } else {
                        Toast.makeText(this, "Грешка при најава како гост.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Функција која нè префрла на главниот екран (Dashboard) по успешна најава
    private fun startDashboard() {
        // За почеток, бидејќи го немаме креирано вториот екран, само ќе ја рестартираме активностa или ќе ја оставиме празна.
        // Утре тука ќе го ставиме кодот за отворање на главната социјална мрежа!
        Toast.makeText(this, "Успешно поврзано со Firebase!", Toast.LENGTH_LONG).show()
    }
}