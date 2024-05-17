package com.example.studysorter.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studysorter.SchoolObject
import com.example.studysorter.Subbject
import com.example.studysorter.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun UlubioneScreen(navController: NavController, innerPadding: PaddingValues) {
    val chmura = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val listaSzkola = SchoolObject.getData()

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding),
    ) {
        Log.d("Show","listaSzkola size: ${listaSzkola.size}")
        items(listaSzkola){school->
            for (sem in school.listaSemestr){
                for (sub in sem.listaSubject){
                    if (sub.ulubione){
                        SubjectItem(subject = sub, navController = navController, pathSem = mutableListOf(school.id,sem.id))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectItem(subject: Subbject, navController: NavController, pathSem: MutableList<String>) {
    var more_options by remember { mutableStateOf(false) }
    val pathSub = mutableListOf(pathSem[0],pathSem[1],subject.id)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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