package com.example.studysorter.screens

import android.app.TimePickerDialog
import android.widget.CalendarView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(innerPadding: PaddingValues) {
    val selectedDate = remember { mutableStateOf(Calendar.getInstance()) }
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val firestoreInstance = FirebaseFirestore.getInstance()
    val events = remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val showEventsDialog = remember { mutableStateOf(false) }
    val showAddEventDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val showEditEventDialog = remember { mutableStateOf(false) }
    val selectedEvent = remember { mutableStateOf<Map<String, Any>?>(null) }

    // Pobranie wydarzeń z Firestore podczas ładowania ekranu HomeScreen lub zmiany wybranej daty
    LaunchedEffect(key1 = selectedDate.value.timeInMillis) {
        firestoreInstance.collection("users").document(currentUser!!)
            .collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedEvents = mutableListOf<Map<String, Any>>()
                for (document in documents) {
                    val event = document.data
                    event["id"] = document.id // Add document ID to the event data
                    fetchedEvents.add(event)
                }
                events.value = fetchedEvents
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
                // Komunikat dla użytkownika w przypadku niepowodzenia pobrania
                Toast.makeText(context, "Nie udało się pobrać wydarzeń", Toast.LENGTH_SHORT).show()
            }
    }

    val filteredEvents = events.value.filter { event ->
        val timestamp = event["date"] as com.google.firebase.Timestamp
        val date = timestamp.toDate()
        val eventCalendar = Calendar.getInstance().apply { time = date }
        eventCalendar.get(Calendar.MONTH) == selectedDate.value.get(Calendar.MONTH) &&
                eventCalendar.get(Calendar.YEAR) == selectedDate.value.get(Calendar.YEAR)
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
                setOnDateChangeListener { _, year, month, dayOfMonth ->
                    // Aktualizacja wybranej daty po zmianie w kalendarzu
                    selectedDate.value.set(year, month, dayOfMonth)
                }
            }
        })

        Button(onClick = { showEventsDialog.value = true }) {
            Text("Pokaż Wydarzenia")
        }

        Button(onClick = { showAddEventDialog.value = true }) {
            Text("Dodaj Wydarzenie")
        }

        if (showEventsDialog.value) {
            EventDialog(
                events = filteredEvents,
                onDismissRequest = { showEventsDialog.value = false },
                onEventLongPress = { event ->
                    selectedEvent.value = event
                    showEditEventDialog.value = true
                }
            )
        }

        if (showEditEventDialog.value && selectedEvent.value != null) {
            EditOrDeleteEventDialog(
                event = selectedEvent.value!!,
                onDismissRequest = { showEditEventDialog.value = false },
                onDeleteEvent = { event ->
                    deleteEvent(event)
                    showEditEventDialog.value = false
                },
                onEditEvent = { event ->
                    showAddEventDialog.value = true
                    // Przekazujemy wybrane wydarzenie do dialogu dodawania/edycji
                }
            )
        }

        if (showAddEventDialog.value) {
            AddEventDialog(
                selectedDate = selectedDate.value,
                onDialogClose = { showAddEventDialog.value = false },
                eventToEdit = selectedEvent.value
            )
        }
    }
}

@Composable
fun EventDialog(
    events: List<Map<String, Any>>,
    onDismissRequest: () -> Unit,
    onEventLongPress: (Map<String, Any>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            Button(
                onClick = onDismissRequest,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Zamknij")
            }
        },
        title = { Text("Wydarzenia") },
        text = {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { event ->
                    val timestamp = event["date"] as com.google.firebase.Timestamp
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val dateString = formatter.format(date)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { onEventLongPress(event) }
                                )
                            },
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nazwa: ${event["name"]}", style = MaterialTheme.typography.h6)
                            Text("Data: $dateString", style = MaterialTheme.typography.body2)
                            Text("Godzina: ${event["hour"]}", style = MaterialTheme.typography.body2)
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun EditOrDeleteEventDialog(
    event: Map<String, Any>,
    onDismissRequest: () -> Unit,
    onDeleteEvent: (Map<String, Any>) -> Unit,
    onEditEvent: (Map<String, Any>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            Column {
                Button(
                    onClick = {
                        onEditEvent(event)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Edytuj")
                }
                Button(
                    onClick = {
                        onDeleteEvent(event)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Usuń")
                }
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Anuluj")
                }
            }
        },
        title = { Text("Opcje Wydarzenia") },
        text = {
            val timestamp = event["date"] as com.google.firebase.Timestamp
            val date = timestamp.toDate()
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateString = formatter.format(date)
            Column {
                Text("Nazwa: ${event["name"]}", style = MaterialTheme.typography.h6)
                Text("Data: $dateString", style = MaterialTheme.typography.body2)
                Text("Godzina: ${event["hour"]}", style = MaterialTheme.typography.body2)
            }
        }
    )
}

fun deleteEvent(event: Map<String, Any>) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val firestoreInstance = FirebaseFirestore.getInstance()
    val documentId = event["id"] as String

    firestoreInstance.collection("users").document(currentUser!!)
        .collection("events").document(documentId)
        .delete()
        .addOnSuccessListener {
            println("Wydarzenie usunięte!")
        }
        .addOnFailureListener { e ->
            println("Error deleting document: $e")
        }
}

@Composable
fun AddEventDialog(
    selectedDate: Calendar,
    onDialogClose: () -> Unit,
    eventToEdit: Map<String, Any>? = null
) {
    val context = LocalContext.current
    val eventName = remember { mutableStateOf(eventToEdit?.get("name") as? String ?: "") }
    val eventHour = remember { mutableStateOf(eventToEdit?.get("hour") as? String ?: "") }
    AlertDialog(
        onDismissRequest = onDialogClose,
        confirmButton = {
            Button(onClick = {
                if (eventName.value.isNotBlank() && eventHour.value.isNotBlank()) {
                    val event = hashMapOf(
                        "name" to eventName.value,
                        "date" to selectedDate.time,
                        "hour" to eventHour.value
                    )
                    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                    val firestoreInstance = FirebaseFirestore.getInstance()

                    if (eventToEdit != null) {
                        val documentId = eventToEdit["id"] as String
                        firestoreInstance.collection("users")
                            .document(currentUser!!)
                            .collection("events").document(documentId)
                            .set(event)
                    } else {
                        firestoreInstance.collection("users")
                            .document(currentUser!!)
                            .collection("events").add(event)
                    }

                    Toast.makeText(context, "Wydarzenie zapisane pomyślnie", Toast.LENGTH_SHORT).show()
                    onDialogClose()
                } else {
                    Toast.makeText(context, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Potwierdź")
            }
        },
        dismissButton = {
            Button(onClick = onDialogClose) {
                Text("Anuluj")
            }
        },
        text = {
            Column {
                TextField(
                    value = eventName.value,
                    onValueChange = { eventName.value = it },
                    label = { Text("Nazwa Wydarzenia") }
                )
                Button(onClick = {
                    val currentHour = selectedDate.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = selectedDate.get(Calendar.MINUTE)
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selectedDate.set(Calendar.MINUTE, minute)
                            eventHour.value = String.format("%02d:%02d", hourOfDay, minute)
                        },
                        currentHour,
                        currentMinute,
                        true
                    ).show()
                }) {
                    Text("Wybierz Godzinę Wydarzenia")
                }
                Text("Wybrana Godzina: ${eventHour.value}")
            }
        }
    )
}
