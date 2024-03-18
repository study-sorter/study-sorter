package com.example.studysorter

import android.app.backup.FileBackupHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Rejestracja : AppCompatActivity() {
    private val AuthInst :FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rejestracja)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun rejestracja(view: View) {//pobiera maila i haslo i loguje osobe na razie bez sprawdzania czegokolwiek procz pustego
        val email: String = findViewById<TextView>(R.id.Email_Logowanie).text.toString()
        val haslo: String = findViewById<TextView>(R.id.Haslo_Logowanie).text.toString()
        if (email.isNotEmpty() && haslo.isNotEmpty()) {
            AuthInst.createUserWithEmailAndPassword(email, haslo)
                .addOnSuccessListener {
                    intent = Intent(this, Logowanie::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("LOG_DEBUD_REJESTRACJA",it.message.toString())
                    Toast.makeText(this,it.message.toString(), Toast.LENGTH_LONG).show()
                }
        }
    }
}