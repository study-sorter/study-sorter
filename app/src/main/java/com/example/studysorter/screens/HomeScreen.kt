package com.example.studysorter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HomeScreen(innerPadding: PaddingValues) {
    val calendar = remember { mutableStateOf(Calendar.getInstance()) }
    val openDialog = remember { mutableStateOf(false) }
    val eventName = remember { mutableStateOf("") }
    val eventHour = remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid // Fetch the current user's ID
    val firestoreInstance = FirebaseFirestore.getInstance() // Get Firestore instance

    // Fetch events from Firestore when the HomeScreen is loaded
    LaunchedEffect(key1 = true) {
        firestoreInstance.collection("users").document(currentUser!!).collection("events")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    println("${document.id} => ${document.data}")
                    // Replace the print statement with your own logic to display the events on the calendar
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(factory = { context ->
            CalendarView(context).apply {
                setOnDateChangeListener(OnDateChangeListener { _, year, month, dayOfMonth ->
                    calendar.value.set(year, month, dayOfMonth)
                })
            }
        })

        Button(onClick = { openDialog.value = true }) {
            Text("Add Event")
        }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = { openDialog.value = false },
                confirmButton = {
                    Button(onClick = {
                        // Write to Firestore
                        val event = hashMapOf(
                            "name" to eventName.value,
                            "date" to calendar.value.time,
                            "hour" to eventHour.value
                        )
                        firestoreInstance.collection("users").document(currentUser!!).collection("events").add(event)
                        openDialog.value = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { openDialog.value = false }) {
                        Text("Cancel")
                    }
                },
                text = {
                    Column {
                        TextField(
                            value = eventName.value,
                            onValueChange = { eventName.value = it },
                            label = { Text("Event Name") }
                        )
                        AndroidView(factory = { context ->
                            CalendarView(context).apply {
                                setOnDateChangeListener(OnDateChangeListener { _, year, month, dayOfMonth ->
                                    calendar.value.set(year, month, dayOfMonth)
                                })
                            }
                        })
                        TextField(
                            value = eventHour.value,
                            onValueChange = { eventHour.value = it },
                            label = { Text("Event Hour") }
                        )
                    }
                }
            )
        }
    }
}



