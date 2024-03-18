package com.example.studysorter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import java.net.Authenticator


class Logowanie : AppCompatActivity() {
    private val AuthInst: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logowanie)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.zarejestruj_Logowanie).setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(Intent(applicationContext, Rejestracja::class.java))
            }
        })
    }
    /*
    Todo("dodanie przycisku wylogowania gdzies")
    override fun onStart() { //od razu przerzuca na MainActivity w przypadku gdzie urzydkownik jest już zalogowany
        super.onStart()
        AuthInst.currentUser?.let { auth -> //jeśli null to się nie wykona
            intent = Intent(this, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)//wyczyszczenie tej aktywności czyli że nie można się tu cofnąć
            }
            startActivity(intent)
        }
    }
    */
    fun logowanie(view: View) {//pobiera maila i haslo i loguje osobe na razie bez sprawdzania czegokolwiek procz pustego
        val email: String = findViewById<TextView>(R.id.Email_Logowanie).text.toString()
        val haslo: String = findViewById<TextView>(R.id.Haslo_Logowanie).text.toString()
        if (email.isNotEmpty() && haslo.isNotEmpty()) {
            AuthInst.signInWithEmailAndPassword(email, haslo)
                .addOnSuccessListener {
                intent = Intent(this, MainActivity::class.java).apply {
                            flags =(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)//wyczyszczenie tej aktywności czyli że nie można się tu cofnąć
                        }
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("LOG_DEBUD_LOGOWANIE",it.message.toString())
                    Toast.makeText(this,it.message.toString(), Toast.LENGTH_LONG).show()
                }
        }
    }
}