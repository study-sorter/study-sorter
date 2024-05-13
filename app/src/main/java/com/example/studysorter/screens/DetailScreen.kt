package com.example.studysorter.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext

@Composable
fun DetailScreen(subjectId: String?, navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseStorage = FirebaseStorage.getInstance()
    val context = LocalContext.current

    val pickFileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        uri?.let {
            val userId = firebaseAuth.currentUser?.uid
            val fileName = it.lastPathSegment
            val storageReference = firebaseStorage.reference.child("users/$userId/files/$fileName")
            val uploadTask = storageReference.putFile(it)
            uploadTask.addOnSuccessListener {
                // File uploaded successfully
                val fileReference = "users/$userId/files/$fileName"
                val fileData = hashMapOf(
                    "name" to fileName,
                    "path" to fileReference
                )
                firebaseFirestore.collection("users").document(userId!!).collection("files").document(fileName!!).set(fileData)
            }.addOnFailureListener {
                // Handle failure
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (subjectId != null) {
            Text(text = "Subject Details for ID: $subjectId", color = Color.Black)
        } else {
            Text(text = "Invalid Subject ID", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { pickFileLauncher.launch("*/*") }) {
            Text(text = "Upload File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text(text = "Powrót")
        }
    }
}
