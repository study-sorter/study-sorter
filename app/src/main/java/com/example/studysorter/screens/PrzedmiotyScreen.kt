package com.example.studysorter.screens

import android.util.Log
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
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.studysorter.SchoolObject
import com.example.studysorter.Semestr
import com.example.studysorter.Subbject
import com.example.studysorter.Szkola
import com.example.studysorter.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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

    Log.d("test", "1")
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
    Log.d("test", "2")

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
    Log.d("test", "3")

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
    Log.d("test", "4")

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
        Log.wtf("test", "dodawanie szkoly rozpoczęte")

        val schoolMap = hashMapOf("name" to school.id)
        val dodawanaSzkola = chmura.document("users/${currentUser.uid}/szkoly/${school.id}")
        chmura.runTransaction { transaction ->
            //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
            if (!transaction.get(dodawanaSzkola).exists()) {
                transaction.set(dodawanaSzkola, schoolMap)
            }
        }
        found = false
        for (sch in listaSzkola) {
            if (sch.id == _school.id) {
                _school = sch
                found = true
                break
            }
        }
        if (!found) {
            listaSzkola.add(_school)
        }
        if (semester != null && semester.id != "") {

            val semesterMap = hashMapOf("name" to semester.id)
            val dodawanaSemestr = dodawanaSzkola.collection("semestry").document(semester.id)

            chmura.runTransaction { transaction ->
                //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
                if (!transaction.get(dodawanaSemestr).exists()) {
                    transaction.set(dodawanaSemestr, semesterMap)
                }
            }
            found = false
            if (semester.id != "") {
                for (sem in _school.listaSemestr) {
                    if (sem.id == _semester!!.id) {
                        _semester = sem
                        found = true
                        break
                    }
                }
                Log.d("test", found.toString())
                if (!found) {
                    _school.listaSemestr.add(_semester!!)
                }
            }


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
                for (sub in _semester!!.listaSubject) {
                    if (sub.id == _subject!!.id) {
                        _subject = sub
                        found = true
                        break
                    }
                }
                if (!found) {
                    _semester.listaSubject.add(_subject!!)
                }
            }
        }
    }
}

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
            ExpandableSemesterItem(semester, navController, path, school)
        }
    }
    if (more_options) {
        more_options = options(path, navController, null, school)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableSemesterItem(
    semester: Semestr,
    navController: NavController,
    path: MutableList<String>,
    school: Szkola
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
            SubjectItem(subject, navController, pathSem, school)
        }
    }
    if (more_options) {
        more_options = options(pathSem, navController, null, school)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectItem(
    subject: Subbject,
    navController: NavController,
    pathSem: MutableList<String>,
    school: Szkola
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
        more_options = options(pathSub, navController, subject, school)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun options(
    path: MutableList<String>,
    navController: NavController,
    subject: Subbject?,
    school: Szkola
): Boolean {
    val TAG = "PrzdemiotyScreen"
    var showBottomSheet by remember { mutableStateOf(true) }
    if (path.size > 0) {
        val listaSzkola = SchoolObject.getData()
        val mycontext = LocalContext.current
        val currentUser = FirebaseAuth.getInstance().currentUser
        val chmura = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val userDoc =
            chmura.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        var showDialog by remember { mutableStateOf(false) }
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
        if (path.size > 1) {
            for (sem in school.listaSemestr) {
                if (sem.id == path[1]) {
                    semestr = sem
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

        if (currentUser != null) {
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
//                    if (path.size == 3) {
                    Option(Icons.Default.Edit, "Edytuj") {
                        showDialog = true
                    }
//                    }
                    if (showDialog) {
                        var szkolaIsError by remember { mutableStateOf(false) }
                        var semestrIsError by remember { mutableStateOf(false) }
                        var subjectIsError by remember { mutableStateOf(false) }

                        AlertDialog(

                            onDismissRequest = { showDialog = false },
                            title = { Text("Edytuj nazwę szkoły lub kierunku") },
                            text = {
                                var focusSchool by remember { mutableStateOf(false) }
                                var focusSemester by remember { mutableStateOf(false) }
                                if (path.size > 0) {
                                    Column {
                                        OutlinedTextField(
                                            value = schoolName,
                                            onValueChange = {
                                                schoolName = it
                                                if (szkolaIsError) {
                                                    szkolaIsError = false
                                                }
                                            },
                                            label = {
                                                Text(
                                                    if (path.size == 1) {
                                                        "Zmień Nazwę wybranej Szkoły/kierunku"
                                                    } else {
                                                        "Zmień folder na inną szkołę/kierunku"
                                                    }
                                                )
                                            },
                                            modifier = Modifier.onFocusChanged {
                                                focusSchool = it.isFocused
                                            },
                                            isError = szkolaIsError,
                                            supportingText = {
                                                if (szkolaIsError) {
                                                    Text(
                                                        text = "nazwa: ${schoolName} już istnieje proszę użyć innej",
                                                        modifier = Modifier.fillMaxWidth(),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        )
                                        if (focusSchool && path.size != 1) {
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
                                        if (path.size > 1) {
                                            OutlinedTextField(
                                                value = semesterName,
                                                onValueChange = {
                                                    semesterName = it
                                                    if (semestrIsError) {
                                                        semestrIsError = false
                                                    }
                                                },
                                                label = {
                                                    Text(
                                                        if (path.size == 1) {
                                                            "Zmień Nazwę lub numer wybranego semestru"
                                                        } else {
                                                            "Zmień folder na inny semestr"
                                                        }
                                                    )
                                                },
                                                modifier = Modifier.onFocusChanged {
                                                    focusSemester = it.isFocused
                                                },
                                                isError = semestrIsError,
                                                supportingText = {
                                                    if (semestrIsError) {
                                                        Text(
                                                            text = "nazwa: ${semesterName} już istnieje proszę użyć innej",
                                                            modifier = Modifier.fillMaxWidth(),
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            )
                                            if (focusSemester && path.size != 2) {
                                                var listaSemTym = if (school.id != schoolName) {
                                                    listaSzkola.find {
                                                        it.id == schoolName

                                                    }?.listaSemestr?.toList()
                                                } else {
                                                    school.listaSemestr
                                                }
                                                if (listaSemTym != null) {
                                                    Box {
                                                        LazyColumn(modifier = Modifier.padding(start = 10.dp)) {
                                                            items(listaSemTym) { semester ->
                                                                HorizontalDivider(color = Color.Gray)
                                                                Text(
                                                                    text = semester.id,
                                                                    modifier = Modifier
                                                                        .clickable {
                                                                            semesterName =
                                                                                semester.id
                                                                            focusSemester =
                                                                                !focusSemester
                                                                        }
                                                                        .padding(8.dp)
                                                                        .fillMaxWidth()
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (path.size > 2) {
                                                OutlinedTextField(
                                                    value = subjectName,
                                                    onValueChange = {
                                                        subjectName = it
                                                        if (subjectIsError) {
                                                            subjectIsError = false
                                                        }
                                                    },
                                                    label = { Text("Zmień nazwę przedmiotu") },
                                                    isError = subjectIsError,
                                                    supportingText = {
                                                        if (subjectIsError) {
                                                            Text(
                                                                text = "nazwa: ${subject} już istnieje proszę użyć innej",
                                                                modifier = Modifier.fillMaxWidth(),
                                                                color = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    var checked = true
                                    val sz = listaSzkola.find {
                                        it.id == schoolName
                                    }
                                    if (sz != null && path.size == 1 && school != sz) {
                                        checked = false
                                        szkolaIsError = true
                                    }
                                    val sem = if (sz != null) {
                                        sz.listaSemestr.find {
                                            it.id == semesterName
                                        }
                                    } else {
                                        null
                                    }
                                    if (sem != null && path.size == 2 && semestr != sem) {
                                        checked = false
                                        semestrIsError = true
                                    }
                                    val sub = if (sem != null) {
                                        sem.listaSubject.find {
                                            it.id == subjectName
                                        }
                                    } else {
                                        null
                                    }
                                    if (sub != null && path.size == 3 && subject != sub) {
                                        checked = false
                                        subjectIsError = true
                                    }
                                    Log.d(TAG, "options: 1 ${sz?.id} ${sem?.id} ${sub?.id}")
                                    Log.d(
                                        TAG,
                                        "options: 2 ${school.id} ${semestr.id} ${subject?.id}"
                                    )
                                    if (checked) {
//                                        Log.d(TAG, "options: ${sz}")
                                        edit(
                                            chmura,
                                            school,
                                            semestr,
                                            subject,
                                            path,
                                            schoolName,
                                            semesterName,
                                            subjectName,
                                            listaSzkola
                                        )
                                        showDialog = false
                                        refreshCurrentFragment(navController)
                                    }
                                    checked = true
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
                                }
                                for (sem in school.listaSemestr) {
                                    for (sub in sem.listaSubject) {
                                        for (file in sub.imageUrls) {
                                            storage.getReferenceFromUrl(file.Url).delete()
                                        }
                                    }
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
                                }
                                for (sub in semestr.listaSubject) {
                                    for (file in sub.imageUrls) {
                                        storage.getReferenceFromUrl(file.Url).delete()
                                    }
                                }
                                school.listaSemestr.remove(semestr)
                            }

                            3 -> {
                                docRef.delete().addOnSuccessListener {
                                }
                                storage.reference.child("users/${currentUser.uid}/${path[0]}/${path[1]}/${path[2]}")
                                    .listAll().addOnSuccessListener { result ->
                                    }
                                for (file in subject!!.imageUrls) {
                                    storage.getReferenceFromUrl(file.Url).delete()
                                }
                                semestr.listaSubject.remove(subject)

                            }

                        }
                        refreshCurrentFragment(navController)

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

private fun refreshCurrentFragment(navController: NavController) {
    val id = navController.currentDestination?.id
    navController.popBackStack(id!!, true)
    navController.navigate(id)
}

fun edit(
    chmura: FirebaseFirestore,
    school: Szkola,
    semestr: Semestr,
    subject: Subbject?,
    path: MutableList<String>,
    schoolName: String,
    semesterName: String,
    subjectName: String,
    listaSzkola: MutableList<Szkola>
) {
    val Uid = FirebaseAuth.getInstance().currentUser?.uid
    val szkolyFolder = FirebaseFirestore.getInstance().collection("users/$Uid/szkoly")
    ProcessLifecycleOwner.get().lifecycleScope.launch {
        if (school.id != schoolName && path.size == 1) {
            school.id = schoolName
            val schoolMap = hashMapOf("name" to schoolName)
            val dodawanaSzkola = szkolyFolder.document("${schoolName}")
            chmura.runTransaction { transaction ->
                //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
                if (!transaction.get(dodawanaSzkola).exists()) {
                    transaction.set(dodawanaSzkola, schoolMap)
                    transaction.delete(szkolyFolder.document("${path[0]}"))
                }
            }.await()
            for (sem in school.listaSemestr) {
                EditSemester(sem, szkolyFolder, schoolName, sem.id, chmura, mutableListOf(path[0],sem.id))
            }
        }
        if (path.size == 2) {
            if (school.id != schoolName || semestr.id != semesterName) {
                EditSemester(semestr, szkolyFolder, schoolName, semesterName, chmura, path)
                semestr.id = semesterName
                var sch = listaSzkola.find {
                    it.id == schoolName
                }
                if (sch == null) {
                    listaSzkola.add(Szkola(schoolName, mutableListOf(semestr)))
                    //w wszytkich tych przypadkach tak naprawdę aktualizujemy już istniejący document
                    chmura.runTransaction { transaction ->
                        transaction.set(
                            szkolyFolder.document("${schoolName}"),
                            mapOf("name" to schoolName)
                        )
                    }
                } else {
                    sch.listaSemestr.add(semestr)
                    sch.listaSemestr.sortBy { it.id }
                }

                school.listaSemestr.remove(semestr)
                school.listaSemestr.sortBy { it.id }
            }
        }
        if (path.size == 3 && subject != null) {
            if (school.id != schoolName || semestr.id != semesterName || subject.id != subjectName) {
                EditSubject(
                    subject,
                    szkolyFolder,
                    schoolName,
                    semesterName,
                    subjectName,
                    chmura,
                    path
                )
                subject.id = subjectName
                val sch = listaSzkola.find {
                    it.id == schoolName
                }
                if (sch == null) {

                    listaSzkola.add(
                        Szkola(
                            schoolName, mutableListOf(
                                Semestr(
                                    semesterName,
                                    mutableListOf(subject)
                                )
                            )
                        )
                    )
                    chmura.runTransaction { transaction ->
                        transaction.set(
                            szkolyFolder.document("${schoolName}"),
                            mapOf("name" to schoolName)
                        )
                        transaction.set(
                            szkolyFolder.document("${schoolName}/semestry/${semesterName}"),
                            mapOf("name" to semesterName)
                        )
                    }.await()
                } else {
                    val sem = sch.listaSemestr.find {
                        it.id == semesterName
                    }
                    if (sem == null) {
                        sch.listaSemestr.add(Semestr(semesterName, mutableListOf(subject)))
                        chmura.runTransaction { transaction ->
                            transaction.set(
                                szkolyFolder.document("${schoolName}/semestry/${semesterName}"),
                                mapOf("name" to semesterName)
                            )
                        }.await()
                    } else {
                        sem.listaSubject.add(subject)
                        sem.listaSubject.sortBy { it.id }
                    }
                }

                semestr.listaSubject.remove(subject)
                semestr.listaSubject.sortBy { it.id }
            }
        }
    }
}

private suspend fun EditSemester(
    sem: Semestr,
    szkolyFolder: CollectionReference,
    schoolName: String,
    semesterName: String,
    chmura: FirebaseFirestore,
    path: MutableList<String>
) {
    val semesterMap = hashMapOf("name" to semesterName)
    val dodawanaSemestr = szkolyFolder.document("${schoolName}/semestry/${semesterName}")

    chmura.runTransaction { transaction ->
        //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
        if (!transaction.get(dodawanaSemestr).exists()) {
            transaction.set(dodawanaSemestr, semesterMap)
        }
        transaction.delete(szkolyFolder.document("${path[0]}/semestry/${path[1]}"))

    }.await()

    for (sub in sem.listaSubject) {
        EditSubject(
            sub,
            szkolyFolder,
            schoolName,
            semesterName,
            sub.id,
            chmura,
            mutableListOf(path[0], path[1], sub.id)
        )
    }
}

private suspend fun EditSubject(
    sub: Subbject,
    szkolyFolder: CollectionReference,
    schoolName: String,
    semesterName: String,
    subjectName: String,
    chmura: FirebaseFirestore,
    path: MutableList<String>
) {
    val TAG = "usuwanie"
    val listaUrlsTemp = mutableListOf<String>()
    for (file in sub.imageUrls) {
        listaUrlsTemp.add(file.Url)
    }
    val subjectMap = hashMapOf(
        "name" to subjectName,
        "ulubione" to sub.ulubione,
        "files" to listaUrlsTemp
    )
    val dodawanaSubject =
        szkolyFolder.document("${schoolName}/semestry/${semesterName}/przedmioty/${subjectName}")
    chmura.runTransaction { transaction ->
        //sprawdzanie czy dokument istnieje jeśli nie to go dodajemy
        if (!transaction.get(dodawanaSubject).exists()) {
            transaction.set(dodawanaSubject, subjectMap)
        }
        transaction.delete(szkolyFolder.document("${path[0]}/semestry/${path[1]}/przedmioty/${path[2]}"))
    }.await()
}

