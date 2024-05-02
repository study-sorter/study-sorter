package com.example.studysorter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.zIndex


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

    val listaSzkola: List<Szkola> by downloadData().collectAsState(initial = emptyList())


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nazwę szkoły lub kierunku") },
            text = {
                var focusSchool by remember { mutableStateOf(false)}
                var focusSemester by remember { mutableStateOf(false)}
                Column {
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nazwa szkoły lub kierunku") },
                        modifier = Modifier.onFocusChanged { focusSchool = it.isFocused }
                    )
                    if (focusSchool){
                        Box {
                            LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                items(listaSzkola) { school ->
                                    HorizontalDivider(color = Color.Gray)
                                    Text(
                                        text = school.id,
                                        modifier = Modifier.clickable {schoolName = school.id}.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = semesterName,
                        onValueChange = { semesterName = it },
                        label = { Text("Nazwa lub numer semestru") },
                        modifier = Modifier.onFocusChanged {
                            focusSemester = it.isFocused
                        }
                    )

                    if (focusSemester){
                        var listaSemTym = emptyList<Semestr>()

                        for (school in listaSzkola){
                            if (school.id == schoolName){
                                listaSemTym = school.listaSemestr
                                break
                            }
                        }
                        Box {
                            LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                items(listaSemTym) { semester ->
                                    HorizontalDivider(color = Color.Gray)
                                    Text(
                                        text = semester.id,
                                        modifier = Modifier.clickable {schoolName = semester.id}.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
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
                        addData(currentUser, schoolName,semesterName,subjectName, chmura)
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

private fun addData(
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

fun downloadData(): Flow<List<Szkola>> = flow {
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
            emit(emptyList())
        }
    } else {
        emit(emptyList())
    }
}
@Composable
fun ExpandableSchoolItem(school: Szkola) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (school.listaSemestr.isNotEmpty()) {
                    expanded = !expanded
                }
            }
    ) {
        Row{
            if (school.listaSemestr.isNotEmpty()){
                Icon(
                    imageVector =if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                    contentDescription =  if (expanded) "rozwinięte" else "zwnięte",
                    modifier = Modifier.padding(top = 10.dp,start = 8.dp)
                )
            }else{
                Box(modifier = Modifier
                    .width(Icons.Filled.ArrowDropDown.defaultWidth + 8.dp)
                    .padding(top = 10.dp, start = 10.dp))
            }
            Text(
                "Szkoła: ${school.id}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(8.dp),
            )
        }
    }

    if (expanded) {
        for (semester in school.listaSemestr) {
            ExpandableSemesterItem(semester)
        }
    }
}

@Composable
fun ExpandableSemesterItem(semester: Semestr) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            .clickable {
                if (semester.listaSubject.isNotEmpty()) {
                    expanded = !expanded
                }
            }
    ) {
        Row{
            if (semester.listaSubject.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                    contentDescription =  if (expanded) "rozwinięte" else "zwnięte",
                    modifier = Modifier.padding(top = 10.dp,start = 8.dp)
                )
            }else{
                Box(modifier = Modifier.width(Icons.Filled.ArrowDropDown.defaultWidth+8.dp))
            }
            Text(
                text = "Semestr: ${semester.id}",
                style = MaterialTheme.typography.bodyLarge ,
                //style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    if (expanded) {
        for (subject in semester.listaSubject) {
            ExpandableSubjectItem(subject)
        }
    }
}

@Composable
fun ExpandableSubjectItem(subject: Subbject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        Row{
            Text(
                text = "Przedmiot: ${subject.id}",
                style = MaterialTheme.typography.bodyMedium ,
                //style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}



/* tu są funkcje dla testowania
@Preview(showBackground = true)
@Composable
fun PreviewlistaSzkol() {

    val listaP = listOf(
        Subbject("przedmiot1),"),
        Subbject("Bob"),
        Subbject("Charlie")
    )
    val listaSe = listOf(
        Semestr("sem1,", emptyList()),
        Semestr("sem2", emptyList()),
        Semestr("sem3",listaP)
    )
    val listaSZ = listOf(
        Szkola("szkola",listaSe),
        Szkola("technikum)", emptyList()),
        Szkola("Charlie", emptyList())
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(1f),
    ) {
        items(listaSZ){school->
            ExpandableSchoolItem(school)
        }
    }

}
@Preview
@Composable
fun Previewdodawanie() {
    val listaP = listOf(
        Subbject("przedmiot1),"),
        Subbject("Bob"),
        Subbject("Charlie")
    )
    val listaSe = listOf(
        Semestr("sem1,", emptyList()),
        Semestr("sem2", emptyList()),
        Semestr("sem3",listaP)
    )
    val listaSZ = listOf(
        Szkola("szkola",listaSe),
        Szkola("technikum)", emptyList()),
        Szkola("Charlie", emptyList())
    )
    var showDialog by remember { mutableStateOf(true) }
    var schoolName by remember { mutableStateOf("") }
    var semesterName by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }
    if (showDialog){
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nazwę szkoły lub kierunku") },
            text = {

                Column {

                    var focusSchool by remember { mutableStateOf(false)}
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        modifier = Modifier
                            .onFocusChanged { focusSchool = it.isFocused },
                        label = { Text("Nazwa szkoły lub kierunku") } ,
                    )
                    if (focusSchool){
                        Box {
                            LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                items(listaSZ) { school ->
                                    HorizontalDivider(color = Color.Gray)
                                    Text(
                                        text = school.id,
                                        modifier = Modifier.clickable {schoolName = school.id}.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
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
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)) {
            Text(text = "szkola $schoolName",style = MaterialTheme.typography.headlineSmall)
            Text(text = "semestr $semesterName",style = MaterialTheme.typography.headlineSmall)
            Text(text = "przedmiot $subjectName",style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showDialog = !showDialog}) {
                Text(text = "pokaz")
            }
        }
}