package com.example.studysorter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.ComposeView
import com.example.studysorter.screens.MainScreen
import androidx.compose.runtime.Composable




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainScreen = @Composable {
            MainScreen()
        }
        setContentView(ComposeView(this).apply {
            setContent {
                mainScreen()
            }
        })
    }
}
/*val button: ImageButton = findViewById(R.id.image)
button.setOnClickListener {
    // Tworzenie intentu, który przechodzi do aktywności profilu
    val intent = Intent(this, Profil::class.java)
    startActivity(intent)
}
}*/