package com.example.studysorter.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studysorter.SchoolObject
import com.example.studysorter.Semestr
import com.example.studysorter.Subbject
import com.example.studysorter.Szkola
import com.example.studysorter.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


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
    var listaSzkola: MutableList<Szkola> = SchoolObject.getData()

    Log.d("test","1")
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nazwę szkoły lub kierunku") },
            text = {
                var focusSchool by remember { mutableStateOf(false) }
                var focusSemester by remember { mutableStateOf(false) }
                Column {
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Nazwa szkoły lub kierunku") },
                        modifier = Modifier.onFocusChanged { focusSchool = it.isFocused }
                    )
                    if (focusSchool) {
                        Box {
                            LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                items(listaSzkola) { school ->
                                    HorizontalDivider(color = Color.Gray)
                                    Text(
                                        text = school.id,
                                        modifier = Modifier
                                            .clickable {
                                                schoolName = school.id
                                                focusSchool = !focusSchool
                                            }
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

                    if (focusSemester) {
                        var listaSemTym = emptyList<Semestr>()

                        for (school in listaSzkola) {
                            if (school.id == schoolName) {
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
                                                focusSemester = !focusSemester
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
                    if (currentUser != null) {
                        addData(
                            currentUser,
                            Szkola(schoolName, mutableListOf()),
                            Semestr(semesterName, mutableListOf()),
                            Subbject(subjectName, false, mutableListOf()),
                            chmura,
                            listaSzkola
                        )
                        refreshCurrentFragment(navController)
                        //dwie poniżesz linijki służą do aktualizacji danych
                        /*SchoolObject.DownloadData() //aktualizacja danych w SchoolObject/SchoolRepository
                        listaSzkola = SchoolObject.getData()//aktualizacja danych wyświetlanych*/
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
    Log.d("test","2")

    //wyswietlanie danych
    LazyColumn(
//            contentPadding = 9.dp,
        modifier = Modifier
            .padding(innerPadding),
    ) {
        Log.d("Show", "listaSzkola size: ${listaSzkola.size}")
        items(listaSzkola) { school ->
            ExpandableSchoolItem(school, navController)
        }
    }
    Log.d("test","3")

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 7.dp),
            contentColor = Color.White
        ) {
            Text("+")
        }
    }
    Log.d("test","4")

}
//przepisałem do funkcji dodawanie danych do firebase dla czytelności

private fun addData(
    currentUser: FirebaseUser,//sprawdzamy przy wywołaniu czy użytkownik jest zalogowany więc nie ma szans że nie będzie
    school: Szkola,
    semester: Semestr?,
    subject: Subbject?,
    chmura: FirebaseFirestore,
    listaSzkola: MutableList<Szkola>
) {
    var found = false
    var _school = school
    var _semester = semester
    var _subject = subject

    // w razie jakby było mięcej eleentów niż nazwa lepiej to przenieść do Onclick i do parametru podać tylko hashMap (tak mi się wydaje)
    if (school.id.isNotEmpty()) {
        Log.wtf("test","dodawanie szkoly rozpoczęte")

        val schoolMap = hashMapOf("name" to school.id)
        val dodawanaSzkola = chmura.document("users/${currentUser.uid}/szkoly/${school.id}")
        chmura.runTransaction { transaction ->
            //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
            if (!transaction.get(dodawanaSzkola).exists()) {
                transaction.set(dodawanaSzkola, schoolMap)
            }
        }
        found = false
        for (sch in listaSzkola){
            if (sch.id == _school.id){
                _school = sch
                found = true
                break
            }
        }
        if(!found){
            listaSzkola.add(_school)
        }
        Log.wtf("test","dodawanie szkoly zakonczone")
        if (semester != null && semester.id != "") {
            Log.wtf("test","dodawanie semst rozpoczęte $_semester")

            val semesterMap = hashMapOf("name" to semester.id)
            val dodawanaSemestr = dodawanaSzkola.collection("semestry").document(semester.id)

            chmura.runTransaction { transaction ->
                //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
                if (!transaction.get(dodawanaSemestr).exists()) {
                    transaction.set(dodawanaSemestr, semesterMap)
                }
            }
            found = false
            if (semester.id != ""){
                for (sem in _school.listaSemestr){
                    if (sem.id == _semester!!.id){
                        _semester = sem
                        found = true
                        break
                    }
                }
                Log.d("test",found.toString())
                if(!found){
                    _school.listaSemestr.add(_semester!!)
                }
            }

            Log.wtf("test","dodawanie semst zakonczone")

            if (subject != null && subject.id != "") {
                val subjectMap = hashMapOf(
                    "name" to subject.id,
                    "ulubione" to subject.ulubione,
                    "files" to subject.imageUrls
                )
                val dodawanaSubject = dodawanaSemestr.collection("przedmioty").document(subject.id)
                chmura.runTransaction { transaction ->
                    //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
                    if (!transaction.get(dodawanaSubject).exists()) {
                        transaction.set(dodawanaSubject, subjectMap)
                    }
                }
                found = false
                    for (sub in _semester!!.listaSubject){
                        if (sub.id == _subject!!.id){
                            _subject = sub
                            found = true
                            break
                        }
                    }
                    if(!found){
                        _semester.listaSubject.add(_subject!!)
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
    var more_options by remember { mutableStateOf(false) }
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
        Row {
            if (school.listaSemestr.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropUp,
                    contentDescription = if (expanded) "rozwinięte" else "zwnięte",
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(Icons.Filled.ArrowDropDown.defaultWidth + 8.dp)
                        .padding(top = 10.dp, start = 10.dp)
                )
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
            ExpandableSemesterItem(semester, navController, path)
        }
    }
    if (more_options) {
        more_options = options(path, navController, null)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableSemesterItem(
    semester: Semestr,
    navController: NavController,
    path: MutableList<String>
) {
    var expanded by remember { mutableStateOf(false) }
    var more_options by remember { mutableStateOf(false) }
    val pathSem = mutableListOf(path[0], semester.id)
    Log.d("Show", "listaSubject size: ${semester.listaSubject.size}")

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
            SubjectItem(subject, navController, pathSem)
        }
    }
    if (more_options) {
        more_options = options(pathSem, navController, null)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectItem(
    subject: Subbject,
    navController: NavController,
    pathSem: MutableList<String>
) {
    var more_options by remember { mutableStateOf(false) }
    val pathSub = mutableListOf(pathSem[0], pathSem[1], subject.id)
    val subjectPath = "szkoly-${pathSub[0]}-semestry-${pathSub[1]}-przedmioty-${pathSub[2]}"
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
                    navController.navigate("${Screens.Przedmioty.route}/${subjectPath}")
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
    if (more_options) {
        more_options = options(pathSub, navController, subject)
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
        val listaSzkola = SchoolObject.getData()
        val mycontext = LocalContext.current
        val currentUser = FirebaseAuth.getInstance().currentUser
        val chmura = FirebaseFirestore.getInstance()
        val userDoc =
            chmura.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        var showDialog by remember { mutableStateOf(false) }
        var school = Szkola("", mutableListOf())
        var semestr = Semestr("", mutableListOf())
        var schoolName by remember { mutableStateOf(path[0]) }
        var semesterName by remember { mutableStateOf("") }
        var subjectName by remember { mutableStateOf("") }
        if (path.size > 1) {
            semesterName = path[1]
            if (path.size > 2) {
                subjectName = path[2]
            }
        }
        for (sch in listaSzkola) {
            if (sch.id == schoolName){
                school = sch
                for (sem in sch.listaSemestr){
                    if (sem.id == semesterName){
                        semestr = sem
                    }
                }
            }
        }
        val docRef = when (path.size) {
            1 -> userDoc
                .collection("szkoly").document(path[0])

            2 -> userDoc
                .collection("szkoly").document(path[0])
                .collection("semestry").document(path[1])

            3 -> userDoc
                .collection("szkoly").document(path[0])
                .collection("semestry").document(path[1])
                .collection("przedmioty").document(path[2])

            else -> userDoc
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
                if (path.size == 3){
                    Option(Icons.Default.Edit, "Edytuj") {
                        showDialog = true
                    }
                }
                if (showDialog) {


                    AlertDialog(

                        onDismissRequest = { showDialog = false },
                        title = { Text("Edytuj nazwę szkoły lub kierunku") },
                        text = {
                            var focusSchool by remember { mutableStateOf(false) }
                            var focusSemester by remember { mutableStateOf(false) }
                            if (path.size > 0){
                                Column {
                                    /*OutlinedTextField(
                                        value = schoolName,
                                        onValueChange = { schoolName = it },
                                        label = { Text("Nazwa szkoły lub kierunku") },
                                        modifier = Modifier.onFocusChanged {
                                            focusSchool = it.isFocused
                                        }
                                    )
                                    if (focusSchool) {
                                        Box {
                                            LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                                items(listaSzkola) { school ->
                                                    HorizontalDivider(color = Color.Gray)
                                                    Text(
                                                        text = school.id,
                                                        modifier = Modifier
                                                            .clickable {
                                                                schoolName = school.id
                                                                focusSchool = !focusSchool
                                                            }
                                                            .padding(8.dp)
                                                            .fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }*/
                                    if (path.size > 1){
                                        /*OutlinedTextField(
                                            value = semesterName,
                                            onValueChange = { semesterName = it },
                                            label = { Text("Nazwa lub numer semestru") },
                                            modifier = Modifier.onFocusChanged {
                                                focusSemester = it.isFocused
                                            }
                                        )
                                        if (focusSemester) {
                                            var listaSemTym = school.listaSemestr
                                            Box {
                                                LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                                    items(listaSemTym) { semester ->
                                                        HorizontalDivider(color = Color.Gray)
                                                        Text(
                                                            text = semester.id,
                                                            modifier = Modifier
                                                                .clickable {
                                                                    semesterName = semester.id
                                                                    focusSemester = !focusSemester
                                                                }
                                                                .padding(8.dp)
                                                                .fillMaxWidth()
                                                        )
                                                    }
                                                }
                                            }
                                        }*/
                                        if (path.size>2){
                                            OutlinedTextField(
                                                value = subjectName,
                                                onValueChange = { subjectName = it },
                                                label = { Text("Nazwa Przedmiotu") }
                                            )
                                        }
                                    }

                                }

                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                Log.d("Edit", "confirm button")
                                if (path.size > 0&& currentUser != null){
                                    school.id = schoolName
                                    if (path.size > 1 ){
                                        semestr.id = semesterName
                                        if (path.size> 2&& subject != null){
                                            subject.id = subjectName
                                        }
                                    }

                                    Log.d("dodawanie","dodaje do ${school.id} ${school.listaSemestr.size}")
                                    addData(currentUser,
                                        school,
                                        Semestr("", mutableListOf()),
                                        Subbject("",false, mutableListOf()), chmura,listaSzkola)
                                    for (sem in school.listaSemestr){
                                        Log.d("dodawanie","     dodaje do ${school.id} -${sem.id}")
                                        addData(currentUser,
                                            school,
                                            sem,
                                            Subbject("",false, mutableListOf()),
                                            chmura,listaSzkola)
                                        for (sub in sem.listaSubject){
                                            Log.d("dodawanie","         dodaje do ${school.id} -${sem.id} - ${sub.id}")
                                            addData(currentUser,school,sem,sub,chmura,listaSzkola)
                                        }
                                    }

                                    chmura.runTransaction{ transaction ->

                                        Log.d("usuwanie","         dodaje do ${school.id}")
                                        transaction.delete(docRef)
                                    }
                                }
                                /*if (currentUser != null && path.size == 3) {
                                    var subjectHolder = subject

                                    subjectHolder!!.id = subjectName
                                    chmura.runTransaction { transaction ->
                                        Log.d("Edit", "start delete")
                                        transaction.delete(docRef)
                                        Log.d("Edit", "end delete")
                                        addData(
                                            currentUser,
                                            school,
                                            semestr,
                                            subjectHolder,
                                            chmura,
                                            listaSzkola
                                        )
                                        Log.d("Edit", "end delete2")
                                    }
                                }*/
                                
                                showDialog = false
                                refreshCurrentFragment(navController)

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

                if (path.size == 3) {
                    val value = subject!!.ulubione
                    val icon: ImageVector
                    icon = if (value) {
                        Icons.Default.Favorite
                    } else {
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
                            listaSzkola.remove(school)
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
                            school.listaSemestr.remove(semestr)
                        }

                        3 -> {
                            docRef.delete().addOnSuccessListener {
                                refreshCurrentFragment(navController)
                            }
                            semestr.listaSubject.remove(subject)

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