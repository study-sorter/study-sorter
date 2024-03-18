package com.example.studysorter

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profil : AppCompatActivity() {
    private val chmura = FirebaseFirestore.getInstance()
    private lateinit var userEmailTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        // Znajdź TextView w układzie
        userEmailTextView = findViewById(R.id.viewMail)

        // Pobierz aktualnie zalogowanego użytkownika z Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Sprawdź, czy użytkownik jest zalogowany
        if (currentUser != null) {
            // Pobierz adres e-mail użytkownika
            val userEmail = currentUser.email
            // Wyświetl adres e-mail użytkownika w TextView
            userEmailTextView.text = userEmail
        } else {
            // Jeśli użytkownik nie jest zalogowany
            userEmailTextView.text = "Brak zalogowanego użytkownika"
        }
    }
}
