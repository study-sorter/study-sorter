package com.example.studysorter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Profil : AppCompatActivity() {
    private val chmura = FirebaseFirestore.getInstance()
    private lateinit var userEmailTextView: TextView
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        userEmailTextView = findViewById(R.id.viewMail)
        storageReference = FirebaseStorage.getInstance().reference

        val changeAvatarButton: Button = findViewById(R.id.zmiana)
        changeAvatarButton.setOnClickListener {
            chooseImageFromGallery()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email
            userEmailTextView.text = userEmail
        } else {
            userEmailTextView.text = "Brak zalogowanego użytkownika"
        }
    }

    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private val PICK_IMAGE_REQUEST = 1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                uploadImageToFirebaseStorage(imageUri)
            } else {
                Toast.makeText(this, "Nie udało się uzyskać adresu URI pliku", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val fileName = "avatar_${currentUser.uid}.jpg"
            val fileRef = storageReference.child("avatars").child(fileName)
            val uploadTask = fileRef.putFile(imageUri)
            uploadTask.addOnSuccessListener {

                //pobierz adres URL przesłanego zdjęcia i zapisz go w bazie danych Firestore
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val userDocRef = chmura.collection("users").document(currentUser.uid)
                    userDocRef.update("avatarUrl", uri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Zmiana avatara zakończona pomyślnie", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Wystąpił błąd podczas zmiany avatara: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Wystąpił błąd podczas przesyłania zdjęcia: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}