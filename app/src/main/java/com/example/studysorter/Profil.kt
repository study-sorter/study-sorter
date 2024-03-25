package com.example.studysorter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.google.firebase.firestore.SetOptions



class Profil : AppCompatActivity() {
    private val chmura = FirebaseFirestore.getInstance()
    private lateinit var userEmailTextView: TextView
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        userEmailTextView = findViewById(R.id.viewMail)
        val userNickTextView: TextView = findViewById(R.id.nick) // Add this line
        storageReference = FirebaseStorage.getInstance().reference

        val changeAvatarButton: Button = findViewById(R.id.zmiana)
        changeAvatarButton.setOnClickListener {
            chooseImageFromGallery()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveImageUriToFirebase()
        }

        if (currentUser != null) {
            val userEmail = currentUser.email
            userEmailTextView.text = userEmail


            val userDoc = chmura.collection("users").document(currentUser.uid)
            userDoc.get().addOnSuccessListener { document ->
                if (document != null) {
                    val avatarUrl = document.getString("avatarUrl")
                    val nickname = document.getString("nickname")

                    if (avatarUrl != null) {
                        val avatarImageButton: ImageView = findViewById(R.id.avatar)
                        Glide.with(this).load(avatarUrl).into(avatarImageButton)
                    }

                    if (nickname != null) {
                        userNickTextView.text = nickname
                    }
                }
            }
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
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                // Set the selected image to the avatar ImageView
                findViewById<ImageView>(R.id.avatar).setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, "Nie udało się uzyskać adresu URI pliku", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveImageUriToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && selectedImageUri != null) {
            val fileName = "avatar_${currentUser.uid}.jpg"
            val fileRef = storageReference.child("avatars").child(fileName)
            val uploadTask = fileRef.putFile(selectedImageUri!!)
            uploadTask.addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val avatarImageButton: ImageView = findViewById(R.id.avatar)
                    avatarImageButton.setImageURI(selectedImageUri)

                    // Save avatar URL to Firestore
                    val userDoc = chmura.collection("users").document(currentUser.uid)
                    val data = hashMapOf(
                        "avatarUrl" to uri.toString()
                    )
                    userDoc.set(data, SetOptions.merge())

                    Toast.makeText(this, "Zmiana avatara zakończona pomyślnie", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Wystąpił błąd podczas pobierania adresu URL obrazu: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Wystąpił błąd podczas przesyłania zdjęcia: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Brak zalogowanego użytkownika lub nie wybrano zdjęcia", Toast.LENGTH_SHORT).show()
        }
    }
    }
