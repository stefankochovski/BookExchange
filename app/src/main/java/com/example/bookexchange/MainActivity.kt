package com.example.bookexchange

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()
    private lateinit var analytics: FirebaseAnalytics

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this,
                getString(R.string.google_login_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        analytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startDashboard()
            return
        }

        // Google Sign-In конфигурација
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("980744443485-t92is8v8dporaclishahs8fa0ovhetph.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLoginEmail = findViewById<Button>(R.id.btnLoginEmail)
        val btnAnonymous = findViewById<Button>(R.id.btnAnonymous)
        val btnLoginGoogle = findViewById<Button>(R.id.btnLoginGoogle)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)

        // Регистрирај се линк
        val fullText = getString(R.string.noprofile)
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(getString(R.string.signupp))
        val end = start + getString(R.string.signup).length
        spannable.setSpan(
            ForegroundColorSpan(android.graphics.Color.parseColor("#4CAF50")),
            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@MainActivity, SignUpActivity::class.java))
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvRegisterLink.text = spannable
        tvRegisterLink.movementMethod = LinkMovementMethod.getInstance()
        tvRegisterLink.highlightColor = android.graphics.Color.TRANSPARENT

        val btnLanguage = findViewById<Button>(R.id.btnLanguage)
        btnLanguage.setOnClickListener {
            val currentLocale = resources.configuration.locales[0].language
            val newLocale = if (currentLocale == "en") "mk" else "en"
            setLocale(newLocale)
        }

        // Email логин
        btnLoginEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fields), Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            saveTokenAndNavigate()
                        } else {
                            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Google логин
        btnLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // Анонимна најава
        btnAnonymous.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.guest_logged), Toast.LENGTH_SHORT).show()
                        startDashboard()
                    } else {
                        Toast.makeText(this, getString(R.string.guest_error), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveTokenAndNavigate()
                } else {
                    Toast.makeText(this, getString(R.string.google_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveTokenAndNavigate() {
        val userId = auth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnFailureListener {
                    // Ако корисникот е нов (Google), set наместо update
                    db.collection("users").document(userId)
                        .set(mapOf("fcmToken" to token))
                }
        }
        startDashboard()
    }

    private fun startDashboard() {
        startActivity(Intent(this, BookFeedActivity::class.java))
        finish()
    }

    private fun setLocale(languageCode: String) {
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        // Рестартирај ја активноста за да се примени јазикот
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}