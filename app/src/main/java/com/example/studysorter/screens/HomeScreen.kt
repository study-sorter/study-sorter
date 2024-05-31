package com.example.studysorter.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.CalendarView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.Calendar
import java.util.Locale

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
    }.sortedBy { event ->
        val timestamp = event["date"] as com.google.firebase.Timestamp
        timestamp.toDate()
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
                    deleteEvent(event, events.value) { updatedEvents ->
                        events.value = updatedEvents
                        showEditEventDialog.value = false
                    }
                },
                onEditEvent = { event ->
                    selectedEvent.value = event
                    showAddEventDialog.value = true
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
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Zamknij")
            }
        },
        title = {
            Text("Wydarzenia", style = MaterialTheme.typography.headlineSmall)
        },
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
                        elevation = CardDefaults.elevatedCardElevation()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nazwa: ${event["name"]}", style = MaterialTheme.typography.titleMedium)
                            Text("Data: $dateString", style = MaterialTheme.typography.bodyMedium)
                            Text("Godzina: ${event["hour"]}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    )
}

fun deleteEvent(event: Map<String, Any>, events: List<Map<String, Any>>, updateEvents: (List<Map<String, Any>>) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val firestoreInstance = FirebaseFirestore.getInstance()
    val documentId = event["id"] as String

    firestoreInstance.collection("users").document(currentUser!!)
        .collection("events").document(documentId)
        .delete()
        .addOnSuccessListener {
            println("Wydarzenie usunięte!")
            // Aktualizacja stanu listy wydarzeń
            updateEvents(events.filter { it["id"] != documentId })
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
    val eventDate = remember { mutableStateOf(selectedDate) }

    AlertDialog(
        onDismissRequest = onDialogClose,
        confirmButton = {
            TextButton(onClick = {
                if (eventName.value.isNotBlank() && eventHour.value.isNotBlank()) {
                    saveEvent(
                        eventName = eventName.value,
                        eventDate = eventDate.value,
                        eventHour = eventHour.value,
                        eventToEdit = eventToEdit
                    ) {
                        Toast.makeText(context, "Wydarzenie zapisane pomyślnie", Toast.LENGTH_SHORT).show()
                        onDialogClose()
                    }
                } else {
                    Toast.makeText(context, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Potwierdź")
            }
        },
        dismissButton = {
            TextButton(onClick = onDialogClose) {
                Text("Anuluj")
            }
        },
        title = {
            Text(text = if (eventToEdit != null) "Edytuj Wydarzenie" else "Dodaj Wydarzenie", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = eventName.value,
                    onValueChange = { eventName.value = it },
                    label = { Text("Nazwa Wydarzenia") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    showTimePickerDialog(context) { hourOfDay, minute ->
                        eventDate.value.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        eventDate.value.set(Calendar.MINUTE, minute)
                        eventHour.value = String.format("%02d:%02d", hourOfDay, minute)
                    }
                }) {
                    Text("Wybierz Godzinę Wydarzenia")
                }
                Text("Wybrana Godzina: ${eventHour.value}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    showDatePickerDialog(context) { year, month, dayOfMonth ->
                        val newDate = Calendar.getInstance()
                        newDate.set(year, month, dayOfMonth)
                        eventDate.value = newDate // Update the eventDate state
                    }
                }) {
                    Text("Wybierz Datę Wydarzenia")
                }
                Text("Wybrana Data: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(eventDate.value.time)}")
            }
        }
    )
}

// Function to show TimePickerDialog
private fun showTimePickerDialog(context: Context, onTimeSetListener: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    TimePickerDialog(context, { _, hourOfDay, minuteOfDay ->
        onTimeSetListener(hourOfDay, minuteOfDay)
    }, hour, minute, true).show()
}

// Function to show DatePickerDialog
private fun showDatePickerDialog(context: Context, onDateSetListener: (Int, Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    DatePickerDialog(context, { _, year, monthOfYear, dayOfMonth ->
        onDateSetListener(year, monthOfYear, dayOfMonth)
    }, year, month, dayOfMonth).show()
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
        confirmButton = {
            TextButton(onClick = { onEditEvent(event) }) {
                Text("Edytuj")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDeleteEvent(event) }) {
                Text("Usuń")
            }
        },
        title = {
            Text("Opcje Wydarzenia", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Text("Co chcesz zrobić z tym wydarzeniem?", style = MaterialTheme.typography.bodyMedium)
        }
    )
}
fun saveEvent(
    eventName: String,
    eventDate: Calendar,
    eventHour: String,
    eventToEdit: Map<String, Any>?,
    onSuccess: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
    val firestoreInstance = FirebaseFirestore.getInstance()

    val event = hashMapOf(
        "name" to eventName,
        "date" to eventDate.time,
        "hour" to eventHour
    )

    if (eventToEdit != null) {
        val documentId = eventToEdit["id"] as String
        firestoreInstance.collection("users")
            .document(currentUser!!)
            .collection("events").document(documentId)
            .set(event)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    } else {
        firestoreInstance.collection("users")
            .document(currentUser!!)
            .collection("events").add(event)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }
}

