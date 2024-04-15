package com.example.studysorter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box




@Composable
fun PrzedmiotyScreen() {
    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showDialog by remember { mutableStateOf(false) }
    var schoolName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nazwę szkoły lub kierunku") },
            text = {
                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("Nazwa szkoły lub kierunku") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (currentUser != null) {
                        val schoolMap = hashMapOf("name" to schoolName)
                        chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName)
                            .set(schoolMap)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error writing document", e)
                            }
                    } else {
                        Log.w("Firestore", "No user is currently signed in.")
                    }
                }) {
                    Text("Potwierdź")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier

                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        }

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.Center), // This will center the button vertically
                contentColor = Color.White
            ) {
                Text("+")
            }
        }


    }}

