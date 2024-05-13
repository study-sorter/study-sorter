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
import java.text.SimpleDateFormat

@Composable
fun HomeScreen(innerPadding: PaddingValues) {
    val calendar = remember { mutableStateOf(Calendar.getInstance()) }
    val openDialog = remember { mutableStateOf(false) }
    val eventName = remember { mutableStateOf("") }
    val eventHour = remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val firestoreInstance = FirebaseFirestore.getInstance()
    val events = remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val showEvents = remember { mutableStateOf(false) }

    // Fetch events from Firestore when the HomeScreen is loaded
    LaunchedEffect(key1 = true) {
        firestoreInstance.collection("users").document(currentUser!!).collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedEvents = mutableListOf<Map<String, Any>>()
                for (document in documents) {
                    fetchedEvents.add(document.data)
                }
                events.value = fetchedEvents
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
            Text("Dodaj wydarzenie")
        }

        Button(onClick = { showEvents.value = !showEvents.value }) {
            Text("Pokaz wydarzenia")
        }

        if (showEvents.value) {
            DisplayEvents(events = events.value)
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

@Composable
fun DisplayEvents(events: List<Map<String, Any>>) {
    Column {
        for (event in events) {
            val timestamp = event["date"] as com.google.firebase.Timestamp
            val date = timestamp.toDate()
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateString = formatter.format(date)

            Text("Nazwa: ${event["name"]}, Data: $dateString, Godzina: ${event["hour"]}")
        }
    }
}
