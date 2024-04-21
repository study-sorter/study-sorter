package com.example.studysorter.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

data class Szkola(var id: String,var listaSemestr:List<Semestr>)
data class Semestr(var id:String,var listaSubject: List<Subbject>)
data class Subbject(var id:String) //jest Subbject bo koliduje z jakąś gotow klasą
@Composable
fun PrzedmiotyScreen(innerPadding: PaddingValues) {
    val mycontext = LocalContext.current

    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showDialog by remember { mutableStateOf(false) }
    var schoolName by remember { mutableStateOf("") }
    var semesterName by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }

    val listaSzkola: List<Szkola> by pobierzSzkoly().collectAsState(initial = emptyList())


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nazwę szkoły lub kierunku") },
            text = {
                Column {
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nazwa szkoły lub kierunku") }
                    )
                    OutlinedTextField(
                        value = semesterName,
                        onValueChange = { semesterName = it },
                        label = { Text("Nazwa lub numer semestru") }
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Nazwa Przedmiotu") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (currentUser != null) {
                        dodajDane(currentUser, schoolName,semesterName,subjectName, chmura)
                    } else {
                        Log.w("Firestore", "No user is currently signed in.")
                    }
                    showDialog = false
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
//przepisałem do funkcji dodawanie danych do firebase dla czytelności

private fun dodajDane(
    currentUser: FirebaseUser,//sprawdzamy przy wywołaniu czy użytkownik jest zalogowany więc nie ma szans że nie będzie
    schoolName: String,
    semesterName: String,
    subjectName: String,
    chmura: FirebaseFirestore
){

        // w razie jakby było mięcej eleentów niż nazwa lepiej to przenieść do Onclick i do parametru podać tylko hashMap (tak mi się wydaje)
        val schoolMap = hashMapOf("name" to schoolName)
        val semesterMap = hashMapOf("name" to semesterName)
        val subjectMap = hashMapOf("name" to subjectName)
        val dodawanaSzkola = chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName)
    chmura.runTransaction {transaction ->
        //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
            if (!transaction.get(dodawanaSzkola).exists()){
                transaction.set(dodawanaSzkola,schoolMap)
            }

    }
    val dodawanaSemestr = chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName).collection("semestry").document(semesterName)
    chmura.runTransaction {transaction ->
        //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
        if (!transaction.get(dodawanaSemestr).exists()){
            transaction.set(dodawanaSemestr,semesterMap)
        }
    }
    val dodawanaSubject = chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName).collection("semestry").document(semesterName).collection("przedmioty").document(subjectName)
    chmura.runTransaction {transaction ->
        //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
        if (!transaction.get(dodawanaSubject).exists()){
            transaction.set(dodawanaSubject,subjectMap)
        }
    }
}

fun pobierzSzkoly(): Flow<List<Szkola>> = flow {
    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser != null) {
        try {
            val listaSzkola = chmura.collection("users").document(currentUser.uid).collection("szkoly").get().await().documents.map { documentSzkola ->
                Szkola(documentSzkola.id, listaSemestr = documentSzkola.reference.collection("semestry").get().await()
                    .documents.map { documentSemestr ->
                        Semestr(documentSemestr.id, listaSubject = documentSemestr.reference.collection("przedmioty").get().await()
                            .documents.map { documentPrzedmiot ->
                                Subbject(documentPrzedmiot.id)
                            })
                    })
            }
            emit(listaSzkola)

        } catch (e: Exception) {
            emit(emptyList<Szkola>())
        }
    } else {
        emit(emptyList<Szkola>())
    }
}
@Composable
fun ExpandableSchoolItem(school: Szkola) {
    for (semester in school.listaSemestr) {
        for (subject in semester.listaSubject) {
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { expanded = !expanded }
            ) {
                Column {
                    Text(
                        text = "Przedmiot: ${subject.id}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Text(
                                text = "semestr: ${semester.id}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                text = "szkola: ${school.id}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
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