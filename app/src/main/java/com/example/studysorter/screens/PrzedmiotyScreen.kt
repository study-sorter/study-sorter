package com.example.studysorter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

data class Szkola(var id: String)
@Composable
fun PrzedmiotyScreen(innerPadding: PaddingValues) {
    val mycontext = LocalContext.current

    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showDialog by remember { mutableStateOf(false) }
    var schoolName by remember { mutableStateOf("") }

    val listaSzkola: List<Szkola> by pobierzSzkoly().collectAsState(initial = emptyList())


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
        LazyColumn(
            modifier = Modifier

                .padding(innerPadding),

        ) {
            items(listaSzkola){school->
//                Toast.makeText(mycontext,school.id,Toast.LENGTH_SHORT).show()
                ExpandableSchoolItem(school)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 7.dp) ,
                contentColor = Color.White
            ) {
                Text("+")
            }
        }

}

fun pobierzSzkoly(): Flow<List<Szkola>> = flow {
    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        try {
            val snapshotSzkola = chmura.collection("users").document(currentUser.uid).collection("szkoly").get().await()
            val listaSzkola = snapshotSzkola.documents.map { documentSzkola ->

                Szkola(documentSzkola.id) // Zakładam, że konstruktor Szkola przyjmuje id jako argument
            }
            emit(listaSzkola)
        } catch (e: Exception) {
            // Obsługa błędów, na przykład emitowanie pustej listy lub błędu
            emit(emptyList<Szkola>())
        }
    } else {
        // Użytkownik nie jest zalogowany, emitowanie pustej listy lub błędu
        emit(emptyList<Szkola>())
    }
}
@Composable
fun ExpandableSchoolItem(school: Szkola) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded }
    ) {
        Column {
            Text(
                text = school.id,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(8.dp)
            )
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = "opsi",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
                // Możesz dodać więcej szczegółów szkoły tutaj
            }
        }
    }
}
@Preview
@Composable
fun Preview() {
    var zalogowany by remember { mutableStateOf(false)}
    FirebaseAuth.getInstance().signInWithEmailAndPassword("test@gmail.com", "123456")
        .addOnSuccessListener {
            zalogowany = true
        }
    if (zalogowany){
        MainScreen()
    }
}