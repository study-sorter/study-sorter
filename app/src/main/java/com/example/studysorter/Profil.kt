package com.example.studysorter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf

import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.studysorter.screens.ProfileScreen
import com.google.firebase.firestore.SetOptions
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


interface ImageChooser {
    fun chooseImageFromGallery()
}

class ProfileViewModel(private val imageChooser: ImageChooser) : ViewModel() {
    private val chmura = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().reference
    var selectedImageUri: Uri? = null

    // LiveData for user email and nickname
    val userEmail = MutableLiveData<String>()
    val userNick = MutableLiveData<String>()

    init {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userEmail.value = currentUser.email // Set user email

            // Fetch user nickname from Firestore
            val userDoc = chmura.collection("users").document(currentUser.uid)
            userDoc.get().addOnSuccessListener { document ->
                userNick.value = document.getString("nickname") // Set user nickname
            }
        }
    }

    fun chooseImageFromGallery() {
        imageChooser.chooseImageFromGallery()
    }

    fun saveImageUriToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && selectedImageUri != null) {
            val fileName = "avatar_${currentUser.uid}.jpg"
            val fileRef = storageReference.child("avatars").child(fileName)
            val uploadTask = fileRef.putFile(selectedImageUri!!)
            uploadTask.addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Save avatar URL to Firestore
                    val userDoc = chmura.collection("users").document(currentUser.uid)
                    val data = hashMapOf(
                        "avatarUrl" to downloadUri.toString()
                    )
                    userDoc.set(data, SetOptions.merge())
                }.addOnFailureListener { exception ->
                    // Handle error when getting download URL
                    // Toast.makeText(this, "Error getting download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                // Handle error when uploading image
                // Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Toast.makeText(this, "No user logged in or no image selected", Toast.LENGTH_SHORT).show()
        }
    }
}

class Profil : ComponentActivity(), ImageChooser {
    private lateinit var chooseImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ProfileViewModel(this)

        chooseImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.selectedImageUri = result.data?.data
                setContent {
                    //ProfileScreen(viewModel, { viewModel.chooseImageFromGallery() }, { viewModel.saveImageUriToFirebase() })
                }
            }
        }
    }

    override fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        chooseImageLauncher.launch(intent)
    }
}

