package com.example.studysorter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.example.studysorter.SchoolObject
import com.example.studysorter.Szkola
import com.example.studysorter.Semestr
import com.example.studysorter.Subbject
import com.google.firebase.auth.FirebaseUser
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.studysorter.navigation.Screens
import com.google.firebase.firestore.DocumentReference
import kotlin.Exception


@Composable
fun PrzedmiotyScreen(navController: NavController, innerPadding: PaddingValues) {
    val mycontext = LocalContext.current

    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showDialog by remember { mutableStateOf(false) }
    var schoolName by remember { mutableStateOf("") }
    var semesterName by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }

//    val listaSzkola: List<Szkola> by pobierzSzkoly().collectAsState(initial = emptyList())
    var listaSzkola: List<Szkola> = SchoolObject.getData()


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
                                        modifier = Modifier
                                            .clickable { schoolName = school.id }
                                            .padding(8.dp)
                                            .fillMaxWidth()
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
                                        modifier = Modifier
                                            .clickable { semesterName = semester.id }
                                            .padding(8.dp)
                                            .fillMaxWidth()
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
                        addData(currentUser, schoolName,semesterName,Subbject(subjectName,false), chmura)
                        //dwie poniżesz linijki służą do aktualizacji danych
                        SchoolObject.DownloadData() //aktualizacja danych w SchoolObject/SchoolRepository
                        listaSzkola = SchoolObject.getData()//aktualizacja danych wyświetlanych
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

        //wyswietlanie danych
        LazyColumn(
//            contentPadding = 9.dp,
            modifier = Modifier
                .padding(innerPadding),
        ) {
            Log.d("Show","listaSzkola size: ${listaSzkola.size}")
            items(listaSzkola){school->
                ExpandableSchoolItem(school, navController)
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
    subjectObject: Subbject,
    chmura: FirebaseFirestore
){
    Log.d("test","addDatafunkcja")
    // w razie jakby było mięcej eleentów niż nazwa lepiej to przenieść do Onclick i do parametru podać tylko hashMap (tak mi się wydaje)
    val schoolMap = hashMapOf("name" to schoolName)
    val semesterMap = hashMapOf("name" to semesterName)
    val subjectMap = hashMapOf(
        "name" to subjectObject.id,
        "ulubione" to subjectObject.ulubione
    )
    if (schoolName.isNotEmpty()){
        val dodawanaSzkola = chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName)
        chmura.runTransaction {transaction ->
            Log.d("test","runTransaction 1")
            //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
            if (!transaction.get(dodawanaSzkola).exists()){
                transaction.set(dodawanaSzkola,schoolMap)
            }
        }
        if(semesterName.isNotEmpty()){
            val dodawanaSemestr = chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName).collection("semestry").document(semesterName)
            chmura.runTransaction {transaction ->
                //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
                if (!transaction.get(dodawanaSemestr).exists()){
                    transaction.set(dodawanaSemestr,semesterMap)
                }
            }
            if (subjectObject.id.isNotEmpty()){
                val dodawanaSubject = chmura.collection("users").document(currentUser.uid).collection("szkoly").document(schoolName).collection("semestry").document(semesterName).collection("przedmioty").document(subjectObject.id)
                chmura.runTransaction {transaction ->
                    //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
                    if (!transaction.get(dodawanaSubject).exists()){
                        transaction.set(dodawanaSubject,subjectMap)
                    }
                }
            }
        }
    }
}

/* tu jest stare pobieranie danych na razie zostawiam wykomentowane
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
}*/
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableSchoolItem(school: Szkola, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var more_options by remember { mutableStateOf(false)}
    val path = mutableListOf(school.id)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onLongClick = {
                    more_options = true
                },
                onClick = {
                    if (school.listaSemestr.isNotEmpty()) {
                        expanded = !expanded
                    }
                }
            )
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
            ExpandableSemesterItem(semester, navController,path)
        }
    }
    if (more_options){
        more_options = options(path,navController,null)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableSemesterItem(semester: Semestr, navController: NavController, path: MutableList<String>) {
    var expanded by remember { mutableStateOf(false) }
    var more_options by remember { mutableStateOf(false)}
    val pathSem = mutableListOf(path[0],semester.id)
    Log.d("Show","listaSubject size: ${semester.listaSubject.size}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            .combinedClickable(
                onLongClick = {
                    more_options = true
                },
                onClick = {
                    if (semester.listaSubject.isNotEmpty()) {
                        expanded = !expanded
                    }
                }
            )
    ) {
        Row {
            if (semester.listaSubject.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                    contentDescription = if (expanded) "rozwinięte" else "zwnięte",
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            } else {
                Box(modifier = Modifier.width(Icons.Filled.ArrowDropDown.defaultWidth + 8.dp))
            }
            Text(
                text = "Semestr: ${semester.id}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    if (expanded) {
        for (subject in semester.listaSubject) {
            SubjectItem(subject, navController,pathSem)
        }
    }
    if (more_options){
        more_options = options(pathSem,navController,null)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectItem(subject: Subbject, navController: NavController,pathSem: MutableList<String>) {
    var more_options by remember { mutableStateOf(false)}
    val pathSub = mutableListOf(pathSem[0],pathSem[1],subject.id)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            .combinedClickable(
                onLongClick = {
                    more_options = true
                },
                onClick = {
                    // Navigate to detail screen on subject click
                    navController.navigate("${Screens.Przedmioty.route}/${subject.id}")
                }
            )

    ) {
        Row {
            Text(
                text = "Przedmiot: ${subject.id}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
    if (more_options){
        more_options = options(pathSub,navController,subject)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun options(
    path: MutableList<String>,
    navController: NavController,
    subject: Subbject?
): Boolean {
    var showBottomSheet by remember { mutableStateOf(true) }
    if (path.size > 0) {
        var listaSzkola = SchoolObject.getData()
        val mycontext = LocalContext.current
        val currentUser = FirebaseAuth.getInstance().currentUser
        val chmura = FirebaseFirestore.getInstance()
        val userDoc = chmura.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        val docRef: DocumentReference
        var showDialog by remember { mutableStateOf(false)}

        docRef = when (path.size) {
            1 ->userDoc
                    .collection("szkoly").document(path[0])
            2->userDoc
                    .collection("szkoly").document(path[0])
                    .collection("semestry").document(path[1])
            3->userDoc
                    .collection("szkoly").document(path[0])
                    .collection("semestry").document(path[1])
                    .collection("przedmioty").document(path[2])
            else->userDoc
        }
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = rememberModalBottomSheetState(true)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(start = 20.dp, bottom = 50.dp)
            ) {
                Option(Icons.Default.Edit, "Edytuj") {
                   showDialog = true
                }
                if (showDialog) {
                    var schoolName by remember { mutableStateOf(path[0]) }
                    var semesterName by remember { mutableStateOf("") }
                    var subjectName by remember { mutableStateOf("") }
                        if (path.size>1) {
                            semesterName = path[1]
                            if (path.size > 2) {
                                subjectName = path[2]
                            }
                        }
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Edytuj nazwę szkoły lub kierunku") },
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
                                                    modifier = Modifier
                                                        .clickable { schoolName = school.id }
                                                        .padding(8.dp)
                                                        .fillMaxWidth()
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
                                                    modifier = Modifier
                                                        .clickable {
                                                            semesterName = semester.id
                                                        }
                                                        .padding(8.dp)
                                                        .fillMaxWidth()
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
                                if (currentUser != null&& path.size == 3) {
                                    if (subjectName != path[2]){
                                        var subjectHolder = subject

                                        subjectHolder!!.id = subjectName
                                        chmura.runTransaction { transaction ->
                                            Log.d("Edit", "start delete")
                                            transaction.delete(docRef)
                                            Log.d("Edit", "end delete")
                                            addData(currentUser,schoolName,semesterName,subjectHolder,chmura)
                                        }
                                    }


                                }
                                showDialog = false

                            }) {
                                Text("Potwierdź")
                            }
                            Log.d("Edit", "confirm button")

                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Anuluj")
                            }
                            Log.d("Edit", "ended dismiss button ")
                        }
                    )
                }
                Log.d("Edit", "ended alert")

                if (path.size == 3) {
                    val value = subject!!.ulubione
                    val icon :ImageVector
                    icon = if (value){
                        Icons.Default.Favorite
                    }else{
                        Icons.Default.FavoriteBorder
                    }
                    Option(icon, "Dodaj do Ulubionych") {
                        chmura.runTransaction { transaction ->
                            transaction.update(docRef, "ulubione", !value)
                        }
                        SchoolObject.DownloadData()
                        refreshCurrentFragment(navController)
                    }
                }
                Option(icon = Icons.Default.Delete, text = "Usuń") {
                    when (path.size) {
                        1 -> {
                            docRef.collection("semestry").get()
                                .addOnSuccessListener { SemSnapshot ->
                                    Toast.makeText(
                                        mycontext,
                                        SemSnapshot.size().toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    for (sem in SemSnapshot.documents) {
                                        sem.reference.collection("przedmioty").get()
                                            .addOnSuccessListener { subSnapshot ->
                                                for (subject in subSnapshot.documents) {
                                                    subject.reference.delete()
                                                }
                                            }
                                        sem.reference.delete()
                                    }
                                }
                            docRef.delete().addOnSuccessListener {
                                refreshCurrentFragment(navController)
                            }
                        }

                        2 -> {
                            docRef.collection("przedmioty").get()
                                .addOnSuccessListener { subSnapshot ->
                                    for (subject in subSnapshot.documents) {
                                        subject.reference.delete()
                                    }
                                }
                            docRef.delete().addOnSuccessListener {
                                refreshCurrentFragment(navController)
                            }
                        }

                        3 -> {
                            docRef.delete().addOnSuccessListener {
                                refreshCurrentFragment(navController)
                                }

                        }

                    }
                }
            }

        }

    }
    return showBottomSheet
}

@Composable
private fun Option(icon: ImageVector, text: String, function: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { function() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Text(text)
    }
}
private fun refreshCurrentFragment(navController: NavController){
    val id = navController.currentDestination?.id
    navController.popBackStack(id!!,true)
    navController.navigate(id)
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

 */