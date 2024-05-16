package com.example.studysorter.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background


@Composable
fun ProfileScreen() {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val userEmail = currentUser?.email ?: ""
    val userNick = currentUser?.displayName ?: ""
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val storageReference = Firebase.storage.reference
                val avatarRef = storageReference.child("avatars/${currentUser?.uid}")
                avatarRef.putFile(it).addOnSuccessListener {
                    avatarRef.downloadUrl.addOnSuccessListener { uri ->
                        selectedImageUri.value = uri
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val storageReference = Firebase.storage.reference
        val avatarRef = storageReference.child("avatars/${currentUser?.uid}")
        avatarRef.downloadUrl.addOnSuccessListener { uri ->
            selectedImageUri.value = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userEmail,
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userNick,
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        selectedImageUri.value?.let { uri ->
            Image(painter = rememberImagePainter(data = uri), contentDescription = "User Avatar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launcher.launch(intent)
        }) {
            Text("Change Avatar")
        }
    }
}


